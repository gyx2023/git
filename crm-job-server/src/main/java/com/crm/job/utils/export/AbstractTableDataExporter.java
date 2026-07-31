package com.crm.job.utils.export;

import com.crm.common.utils.ExportToolsUtils;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.dao.sys.SysOperLog;
import com.crm.system.service.sys.ISysOperLogService;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 表数据导出模板（管道符 | 分隔的 TXT 文件 → SFTP 上传）
 * <p>
 * 子类只需实现 4 个抽象方法定义数据源和列映射，基类自动处理：
 * <ul>
 *   <li>创建临时文件</li>
 *   <li>写入表头（可选）</li>
 *   <li>写入数据行（| 分隔，自动调用 ExportToolsUtils.formatField）</li>
 *   <li>上传至 SFTP（SftpUtil.uploadFile）</li>
 *   <li>清理临时文件</li>
 * </ul>
 * </p>
 *
 * @param <T> 数据行类型（DO 或 VO）
 */
@Slf4j
public abstract class AbstractTableDataExporter<T> extends TextFileExporter<TextFileExportContext> {

    protected static final String DEFAULT_CONFIG_TYPE = "topp_tcomm_sys_import";
    protected static final String SEPARATOR = "|";

    protected SftpUtil sftpUtil;

    @Resource
    public void setSftpUtil(SftpUtil sftpUtil) {
        this.sftpUtil = sftpUtil;
    }

    @Resource
    private ISysOperLogService sysOperLogService;

    // ========== 子类必须实现的抽象方法 ==========

    /** 文件名前缀，如 "topp_order", "sys_users" */
    protected abstract String getFileNamePrefix();

    /**
     * 表头数组（返回 null 或空数组表示不写表头）
     * 注意：表头数量必须与 getColumns() 返回的数组长度一致
     */
    protected abstract String[] getHeaders();

    /** 将一条记录转换为字段值数组（按导出列顺序），基类自动调用 ExportToolsUtils.formatField */
    protected abstract Object[] getColumns(T record);

    // ========== ID 范围分页导出（子类必须实现） ==========

    /** 查询总记录数（带现有过滤条件） */
    protected abstract long getTotalCount();

    /** 获取最小主键 ID */
    protected abstract Long getIdMin();

    /** 获取最大主键 ID */
    protected abstract Long getIdMax();

    /**
     * 按主键 ID 范围查询数据
     * @param startId 起始 ID（含）
     * @param endId   结束 ID（不含）
     * @return ID 在 [startId, endId) 范围内的数据，按 ID 升序
     */
    protected abstract List<T> queryByRange(long startId, long endId);

    /**
     * 查询导出数据
     * @deprecated 已废弃，改用 getTotalCount()/getIdMin()/getIdMax()/queryByRange()
     */
    @Deprecated
    protected List<T> queryData() {
        throw new UnsupportedOperationException(
                "queryData() 已废弃，请实现 getTotalCount/getIdMin/getIdMax/queryByRange");
    }

    // ========== 子类可覆写的配置方法 ==========

    /** SFTP 配置类型，默认 "topp_tcomm_sys_import" */
    protected String getConfigType() {
        return DEFAULT_CONFIG_TYPE;
    }

    /** 是否支持 XXL-Job 参数覆盖（configType:yyyyMMdd） */
    protected boolean isParamSupported() {
        return false;
    }

    /** 是否需要校验文件是否存在且非空（简单导出默认需要） */
    protected boolean isFileValidationNeeded() {
        return true;
    }

    // ========== 模板方法实现 ==========

    /**
     * 构建导出上下文并执行导出
     * 子类的 @XxlJob 方法调用此方法
     *
     * @param dateStr    导出日期 yyyyMMdd，null 则使用当天
     * @param configType SFTP 配置类型，null 则使用 getConfigType()
     */
    protected void executeExport(String dateStr, String configType) {
        String date = dateStr != null ? dateStr : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String cfg = configType != null ? configType : getConfigType();
        String prefix = getFileNamePrefix();

        TextFileExportContext ctx = new TextFileExportContext();
        ctx.setFileName(prefix + "_" + date + ".txt");
        ctx.setExportDate(date);
        ctx.setFilePath(System.getProperty("java.io.tmpdir"));
        ctx.setFileType(".txt");

        execute(ctx);
    }

    @Override
    protected TextFileExportContext createFile(TextFileExportContext ctx) {
        try {
            // createTempFile 会加随机后缀，所以先用完整文件名做前缀
            String baseName = ctx.getFileName().replace(".txt", "");
            File tempFile = File.createTempFile(baseName + "_", ".txt");
            ctx.setLocalFile(tempFile);
            log.info("创建临时文件: {}", tempFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("创建临时文件失败", e);
        }
        return ctx;
    }

    @Override
    protected void writeHeader(TextFileExportContext ctx, BufferedWriter writer) throws IOException {
        String[] headers = getHeaders();
        if (headers != null && headers.length > 0) {
            StringBuilder row = new StringBuilder();
            for (int i = 0; i < headers.length; i++) {
                if (i > 0) {
                    row.append(SEPARATOR);
                }
                row.append(headers[i]);
            }
            row.append('\n');
            writer.write(row.toString());
        }
    }

    /**
     * 导出数据行数超过此阈值时使用 ID 范围分桶写入，避免全量加载
     */
    private static final long RANGE_BUCKET_PAGE_SIZE = 5000;

    @Override
    protected void writeBody(TextFileExportContext ctx, BufferedWriter writer) throws IOException {
        long minId = getIdMin();
        long maxId = getIdMax();

        if (minId <= 0 || maxId <= minId) {
            log.warn("ID范围无效或无数据，表: {}, minId={}, maxId={}", getFileNamePrefix(), minId, maxId);
            XxlJobHelper.log("ID范围无效: minId={}, maxId={}", minId, maxId);
            return;
        }

        long totalCount = getTotalCount();
        if (totalCount <= 0) {
            log.warn("查询数据为空，表: {}", getFileNamePrefix());
            XxlJobHelper.log("查询数据为空");
            return;
        }

        // 按 COUNT(*) 算桶数，把 ID 范围切成 N 段
        long bucketCount = (totalCount + RANGE_BUCKET_PAGE_SIZE - 1) / RANGE_BUCKET_PAGE_SIZE;
        long rangeStep = (maxId - minId + 1) / bucketCount;
        if (rangeStep <= 0) {
            rangeStep = 1;
        }

        long totalRows = 0;
        for (long i = 0; i < bucketCount; i++) {
            long startId = minId + i * rangeStep;
            long endId = (i == bucketCount - 1) ? maxId + 1 : startId + rangeStep;

            List<T> records = queryByRange(startId, endId);
            if (records == null || records.isEmpty()) {
                continue;
            }

            for (T record : records) {
                Object[] columns = getColumns(record);
                StringBuilder row = new StringBuilder();
                for (int j = 0; j < columns.length; j++) {
                    if (j > 0) {
                        row.append(SEPARATOR);
                    }
                    row.append(ExportToolsUtils.formatField(columns[j]));
                }
                row.append('\n');
                writer.write(row.toString());
                totalRows++;
            }
            writer.flush();
        }

        ctx.setTotalRows(totalRows);
        log.info("数据写入完成，共 {} 行", totalRows);
        XxlJobHelper.log("数据写入完成，共 {} 行", totalRows);
    }

    @Override
    protected void finishWriteFileInternal(TextFileExportContext ctx, SummaryHolder summary) {
        // 文件校验（可选）
        if (isFileValidationNeeded()) {
            if (!ctx.getLocalFile().exists() || ctx.getLocalFile().length() == 0) {
                log.error("导出文件为空或不存在: {}", ctx.getLocalFile().getAbsolutePath());
                XxlJobHelper.log("导出文件为空");
                return;
            }
        }

        // 上传 SFTP
        String configType = getConfigType();
        // 远程文件名带日期，与旧版各独立类行为一致：{prefix}_{yyyyMMdd}.txt
        String remoteFileName = getFileNamePrefix() + "_" + ctx.getExportDate() + ".txt";
        sftpUtil.uploadFile(configType, ctx.getLocalFile().getAbsolutePath(), remoteFileName, ctx.getExportDate());
        log.info("文件上传成功: {}", remoteFileName);
        XxlJobHelper.log("文件上传成功: {}", remoteFileName);
    }

    /**
     * 操作日志记录：覆写基类钩子，在导出生命周期结束后记录 SysOperLog
     * 所有继承本类的导出任务（如 SysCodeExport）都会自动记录操作日志
     */
    @Override
    protected void recordOperLog(TextFileExportContext context, long startTime, String errorMsg) {
        try {
            SysOperLog operLog = new SysOperLog();
            operLog.setTitle("表数据导出任务-" + getFileNamePrefix());
            operLog.setRequestMethod("JOB");
            operLog.setOperatorType(2);
            operLog.setOperName("system");
            operLog.setOperParam("fileName=" + context.getFileName() + ", date=" + context.getExportDate());
            operLog.setStatus(errorMsg == null ? 0 : 1);
            operLog.setJsonResult("导出行数=" + context.getTotalRows());
            operLog.setErrorMsg(errorMsg);
            operLog.setRequestTime(System.currentTimeMillis() - startTime);
            sysOperLogService.insertOperlog(operLog);
        } catch (Exception e) {
            log.error("记录操作日志异常, 表: {}", getFileNamePrefix(), e);
        }
    }
}
