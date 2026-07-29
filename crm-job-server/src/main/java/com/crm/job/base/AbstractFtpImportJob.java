package com.crm.job.base;

import com.crm.common.utils.DateUtils;
import com.crm.job.utils.SftpUtil;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * FTP/SFTP 文件下载入库抽象基类
 * <p>
 * 统一流程：解析XXL参数 → 查sys_param拿SFTP配置 → 下载文件 → 逐行解析 → 批量入库 → 清理本地文件
 * <p>
 * <b>子类必须实现：</b>
 * <ul>
 *   <li>{@link #getJobName()}       — XXL-Job Handler 名称</li>
 *   <li>{@link #getConfigType()}    — sys_param 配置类型</li>
 *   <li>{@link #getFileDateType()}  — 文件日期类型：D-日文件(yyyyMMdd)、M-月文件(yyyyMM)</li>
 *   <li>{@link #getFileName(String)}— 文件名（如 DWD_CUST_BASE_INFO_D_20260701.txt）</li>
 *   <li>{@link #getSeparator()}     — 字段分隔符（如 "\\x1b"）</li>
 *   <li>{@link #parseRow(String[])} — 字段映射：列数组 → 实体</li>
 *   <li>{@link #doInsert(List)}     — 批量入库</li>
 * </ul>
 * <p>
 * <b>可选覆盖：</b>
 * <ul>
 *   <li>{@link #beforeImport()}     — 入库前（如备份原表）</li>
 *   <li>{@link #afterImport(int)}   — 入库后（如统计通知）</li>
 *   <li>{@link #getDefaultDaysBefore()} — 默认回溯天数，默认1</li>
 *   <li>{@link #getBatchSize()}     — 批量大小，默认500</li>
 *   <li>{@link #preProcess(T)}      — 入库前逐条处理（如设置默认字段）</li>
 * </ul>
 * <p>
 * <b>XXL-Job 参数说明：</b>
 * <ul>
 *   <li>只传日期字符串，格式由 {@link #getFileDateType()} 决定：</li>
 *   <li>D 类型：yyyyMMdd，例如 20260701</li>
 *   <li>M 类型：yyyyMM，例如 202607</li>
 *   <li>不传参则使用 {@link #getDefaultDaysBefore()} 回溯天数计算默认日期</li>
 * </ul>
 *
 * @param <T> 入库实体类型
 * @author crm
 */
@Slf4j
public abstract class AbstractFtpImportJob<T> {

    /** 日文件类型 */
    protected static final String FILE_DATE_TYPE_DAILY = "D";
    /** 月文件类型 */
    protected static final String FILE_DATE_TYPE_MONTHLY = "M";

    @Resource
    protected SftpUtil sftpUtil;

    /** 当前处理的日期字符串（子类可在 afterImport 中读取） */
    protected String currentDateStr;

    // ==================== 子类必须实现 ====================

    /** XXL-Job Handler 名称 */
    protected abstract String getJobName();
    /** sys_param 配置类型 */
    protected abstract String getConfigType();
    /**
     * 文件日期类型
     * @return {@link #FILE_DATE_TYPE_DAILY}(D) 表示日文件，日期格式 yyyyMMdd；
     *         {@link #FILE_DATE_TYPE_MONTHLY}(M) 表示月文件，日期格式 yyyyMM
     */
    protected abstract String getFileDateType();
    /** 字段分隔符 */
    protected abstract String getSeparator();
    /** 文件名（dateStr 格式由 getFileDateType() 决定：D→yyyyMMdd, M→yyyyMM） */
    protected abstract String getFileName(String dateStr);
    /** 字段映射 */
    protected abstract T parseRow(String[] cols);
    /** 批量入库 */
    protected abstract int doInsert(List<T> batch);

    // ==================== 可选覆盖 ====================

    /**
     * 默认回溯天数。不传参时，取 N 天前的日期作为默认日期
     */
    protected int getDefaultDaysBefore() { return 1; }
    protected int getBatchSize() { return 500; }
    protected void beforeImport() {}
    protected void afterImport(int totalCount) {}
    /** 入库前逐条处理（如补默认值），默认不处理 */
    protected void preProcess(T entity) {}

    // ==================== 统一入口（子类 @XxlJob 方法调用） ====================

    protected void execute() {
        String jobName = getJobName();
        log.info("【{}】开始执行", jobName);
        XxlJobHelper.log("【" + jobName + "】开始执行");

        // 1. 解析参数：只传日期字符串，格式由 getFileDateType() 决定
        String dateStr;
        String jobParam = XxlJobHelper.getJobParam();
        if (jobParam != null && !jobParam.trim().isEmpty()) {
            dateStr = jobParam.trim();
            // 校验格式
            if (FILE_DATE_TYPE_DAILY.equals(getFileDateType()) && dateStr.length() != 8) {
                XxlJobHelper.handleFail("日文件参数格式错误，应为 yyyyMMdd，实际传入: " + dateStr);
                return;
            }
            if (FILE_DATE_TYPE_MONTHLY.equals(getFileDateType()) && dateStr.length() != 6) {
                XxlJobHelper.handleFail("月文件参数格式错误，应为 yyyyMM，实际传入: " + dateStr);
                return;
            }
        } else {
            // 未传参，回溯 N 天
            dateStr = DateUtils.getBeforeNDays(getDefaultDaysBefore());
            if (FILE_DATE_TYPE_MONTHLY.equals(getFileDateType())) {
                // yyyyMMdd → yyyyMM
                dateStr = dateStr.substring(0, 6);
            }
        }
        this.currentDateStr = dateStr;

        String cfgType = getConfigType();
        String fileName = getFileName(dateStr);
        log.info("【{}】参数: configType={}, date={}, file={}", jobName, cfgType, dateStr, fileName);
        XxlJobHelper.log("配置: " + cfgType + ", 日期: " + dateStr + ", 文件: " + fileName);

        String localFile = null;
        try {
            // 2. 下载
            localFile = download(cfgType, dateStr, fileName);
            if (localFile == null) {
                XxlJobHelper.handleFail("文件下载失败");
                return;
            }

            // 3. 入库前
            beforeImport();

            // 4. 解析 + 批量入库
            int total = processFile(localFile);
            log.info("【{}】完成: {} 条", jobName, total);
            XxlJobHelper.handleSuccess("完成: " + total + " 条");

            // 5. 入库后
            afterImport(total);
        } catch (Exception e) {
            log.error("【{}】执行异常", jobName, e);
            XxlJobHelper.handleFail("执行异常: " + e.getMessage());
        } finally {
            if (localFile != null) {
                new File(localFile).delete();
            }
        }
    }

    // ==================== 内部方法 ====================

    /** 下载文件到本地 */
    protected String download(String configType, String dateStr, String fileName) {
        try {
            String localPath = "/tmp/sftp_download/" + dateStr + "/" + fileName;
            new File(localPath).getParentFile().mkdirs();
            sftpUtil.downloadFile(configType, getRemotePath(configType, dateStr, fileName), localPath);
            if (!new File(localPath).exists()) {
                log.error("【{}】下载后文件不存在: {}", getJobName(), localPath);
                return null;
            }
            log.info("【{}】下载成功: {}", getJobName(), localPath);
            return localPath;
        } catch (Exception e) {
            log.error("【{}】下载失败", getJobName(), e);
            return null;
        }
    }

    /** 构建远程文件路径（可从sys_param/sftpUtil获取根目录拼接），子类可覆盖 */
    protected String getRemotePath(String configType, String dateStr, String fileName) {
        // SftpUtil内部拼接根目录
        return dateStr + "/" + fileName;
    }

    /**
     * 逐行解析（使用原始行字符串，子类可覆盖以使用 GroupFileParser.parseLine 等方式）
     * 默认实现：split 后调 parseRow(String[] cols)
     */
    protected T parseRowFromLine(String line) {
        return parseRow(line.split(getSeparator(), -1));
    }

    /** 解析文件并批量入库（子类可覆盖以使用自定义解析方式） */
    protected int processFile(String filePath) {
        int total = 0, batchSize = getBatchSize();
        List<T> batch = new ArrayList<>(batchSize);

        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(Files.newInputStream(Paths.get(filePath)), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    T entity = parseRowFromLine(line);
                    if (entity != null) {
                        preProcess(entity);
                        batch.add(entity);
                        if (batch.size() >= batchSize) {
                            total += doInsert(batch);
                            batch.clear();
                        }
                    }
                } catch (Exception e) {
                    log.warn("【{}】解析行失败: {}", getJobName(),
                        line.length() > 100 ? line.substring(0, 100) : line, e);
                }
            }
            if (!batch.isEmpty()) {
                total += doInsert(batch);
            }
        } catch (IOException e) {
            log.error("【{}】读取文件失败", getJobName(), e);
        }
        return total;
    }
}
