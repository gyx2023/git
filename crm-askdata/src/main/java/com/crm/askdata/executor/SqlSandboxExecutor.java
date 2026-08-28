package com.crm.askdata.executor;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.crm.askdata.router.RoutingStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.*;

/**
 * SQL 沙箱执行器
 * <p>
 * 对 Agent 生成的 SQL 进行安全校验后，通过 JdbcTemplate 执行。
 * 校验规则：
 * 1. 必须是 SELECT 语句（禁止 DDL/DML）
 * 2. 查询的表必须在白名单内（meta_metric.sourceTable 已注册的表）
 * 3. 禁止危险函数（INTO OUTFILE、LOAD_FILE、SLEEP 等）
 * 4. 结果行数限制（默认最多 1000 行）
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SqlSandboxExecutor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int MAX_RESULT_ROWS = 1000;

    /** 禁止的关键字（大小写不敏感匹配） */
    private static final Set<String> FORBIDDEN_KEYWORDS = new HashSet<>(Arrays.asList(
        "INTO OUTFILE", "LOAD_FILE", "SLEEP(", "BENCHMARK(", "INTO DUMPFILE",
        "PG_SLEEP", "WAITFOR DELAY"
    ));

    /**
     * 校验并执行 SQL
     *
     * @param sql            Agent 生成的 SQL
     * @param allowedTables  允许查询的表名集合（来自 meta_metric.sourceTable）
     * @param datasourceName 数据源名称（如 master / slave_1）
     * @param traceId        链路追踪 ID
     * @return 查询结果行列表
     * @throws IllegalArgumentException SQL 校验不通过时抛出
     */
    public List<Map<String, Object>> execute(String sql, Set<String> allowedTables,
                                              String datasourceName, String traceId) {
        // 1. 危险关键字检查（快速过滤，避免 JSqlParser 解析恶意 SQL 时报错）
        String sqlUpper = sql.toUpperCase();
        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (sqlUpper.contains(keyword)) {
                log.warn("traceId={} SQL contains forbidden keyword '{}', sql={}", traceId, keyword, sql);
                throw new IllegalArgumentException("SQL 安全校验不通过");
            }
        }

        // 2. JSqlParser AST 解析
        Statement stmt;
        try {
            stmt = CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            log.warn("traceId={} failed to parse SQL, sql={}", traceId, sql, e);
            throw new IllegalArgumentException("SQL 语法解析失败");
        }

        // 3. 必须是 SELECT 语句
        if (!(stmt instanceof Select)) {
            log.warn("traceId={} SQL is not SELECT, type={}", traceId, stmt.getClass().getSimpleName());
            throw new IllegalArgumentException("只允许执行 SELECT 查询");
        }

        // 4. 提取表名，校验白名单
        TablesNamesFinder tablesFinder = new TablesNamesFinder();
        List<String> tableNames = tablesFinder.getTableList(stmt);
        if (tableNames.isEmpty()) {
            log.warn("traceId={} SQL has no table reference", traceId);
            throw new IllegalArgumentException("SQL 格式异常");
        }

        List<String> allowedList = allowedTables != null ? new ArrayList<>(allowedTables) : Collections.emptyList();
        for (String table : tableNames) {
            boolean matched = allowedList.stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(table));
            if (!matched) {
                log.warn("traceId={} table '{}' not in whitelist {}, sql={}",
                    traceId, table, allowedList, sql);
                throw new IllegalArgumentException("查询的数据库表不在允许范围内");
            }
        }

        // 5. 执行 SQL
        try {
            return doExecute(sql, datasourceName, traceId);
        } catch (Exception e) {
            log.error("traceId={} SQL execution failed: sql={}, error={}", traceId, sql, e.getMessage());
            throw new RuntimeException("SQL 执行失败", e);
        }
    }

    /**
     * 通过 JdbcTemplate 执行 SQL
     */
    private List<Map<String, Object>> doExecute(String sql, String datasourceName, String traceId) {
        // 限制行数：无 LIMIT 时追加
        String limitedSql = appendLimitIfMissing(sql);

        // 切换数据源
        DynamicRoutingDataSource ds = com.crm.common.utils.spring.SpringUtils
            .getBean(DynamicRoutingDataSource.class);
        DataSource targetDs = ds.getDataSource(datasourceName != null ? datasourceName : "master");
        if (targetDs == null) {
            throw new IllegalArgumentException("数据源不存在：" + datasourceName);
        }

        JdbcTemplate jdbc = new JdbcTemplate(targetDs);
        List<Map<String, Object>> rows = jdbc.queryForList(limitedSql);

        log.info("traceId={} SQL executed, returned {} rows, datasource={}", traceId, rows.size(), datasourceName);
        return rows;
    }

    /**
     * 如果 SQL 没有 LIMIT 子句，追加默认行数限制
     */
    private String appendLimitIfMissing(String sql) {
        String trimmed = sql.trim();
        // 简单检查：末尾是否有 LIMIT（不处理子查询中的 LIMIT）
        if (trimmed.toUpperCase().matches(".*\\bLIMIT\\s+\\d+\\s*;?\\s*$")) {
            return trimmed;
        }
        // 去掉末尾分号
        if (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }
        return trimmed + " LIMIT " + MAX_RESULT_ROWS;
    }
}
