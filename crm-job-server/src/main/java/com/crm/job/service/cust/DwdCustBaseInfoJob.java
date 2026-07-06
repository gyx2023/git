package com.crm.job.service.cust;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.crm.common.utils.DateUtils;
import com.crm.job.utils.GroupFileParser;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.dao.SysParamDo;
import com.crm.system.domain.dao.FtpCustBaseInfoDo;
import com.crm.system.mapper.param.SysParamMapper;
import com.crm.system.service.cust.IFtpCustBaseInfoService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * 集团客户基础信息同步定时任务
 * 从SFTP读取客户基础信息文件并导入到ftp_cust_base_info表
 *
 * @author crm
 */
@Slf4j
@Component
public class DwdCustBaseInfoJob {

    /**
     * 批量处理大小
     */
    private static final int DEFAULT_BATCH_SIZE = 500;

    /**
     * 文件分隔符（\x1b，即ASCII码27的ESC字符）
     */
    private static final String FILE_SEPARATOR = "\\x1b";

    /**
     * 文件名前缀
     */
    private static final String FILE_NAME_PREFIX = "DWD_CUST_BASE_INFO_D_";

    /**
     * 文件名后缀
     */
    private static final String FILE_EXTENSION = ".txt";

    /**
     * XXL传递配置类型参数
     */
    private static final String CONFIG_TYPE = "configType";

    /**
     * XXL传递日期参数
     */
    private static final String TARGET_DATE = "targetDate";

    /**
     * XXL传递T-x账期参数
     */
    private static final String DEFAULT_DAYS_BEFORE_STR = "defaultDaysBefore";

    /**
     * 默认提前天数（T-1）
     */
    private static final int DEFAULT_DAYS_BEFORE = 1;

    /**
     * 日期格式
     */
    private static final String SIMPLE_DATE_FORMAT = "yyyyMMdd";

    /**
     * SFTP配置类型
     */
    private static final String SFTP_CONFIG_TYPE = "dwd_cust_base_info_new";
    private static final String SFTP_HOST = "sftp.host";
    private static final String SFTP_PORT = "sftp.port";
    private static final String SFTP_USER = "sftp.user";
    private static final String SFTP_PASSWORD = "sftp.password";
    private static final String SFTP_DATA_ROOT_PATH = "sftp.dataRootPath";
    private static final String SFTP_DATA_LOCAL_PATH = "sftp.dataLocalPath";

    /**
     * 本地临时目录
     */
    private static String LOCAL_TEMP_DIR = "/tmp/sftp_download/";

    /**
     * 默认缓冲区大小（8KB）
     */
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * 客户所属系统(NOLD/OLD)
     */
    private static final String SRC_TABLE = "NOLD";

    @Resource
    private SftpUtil sftpUtil;

    @Resource
    private GroupFileParser groupFileParser;

    @Resource
    private SysParamMapper sysParamMapper;

    @Resource
    private IFtpCustBaseInfoService ftpCustBaseInfoService;

    /**
     * 集团客户基础信息同步定时任务
     * 在xxl-job调度中心配置JobHandler为：dwdCustBaseInfoJobHandler
     * 支持传入参数格式：
     * 1. 仅配置类型：configType - 指定配置类型，使用当前日期 - 例：dwd_cust_base_info_new
     * 2. 单日期参数：targetDate - 指定日期 - 例：20260618
     * 3. T-x账期：defaultDaysBefore： - 指定读取T-x账期，且传递账期不为负数 - 例：1
     * 参数样例：{"configType": "dwd_cust_base_info_new", "targetDate": "20260618", defaultDaysBefore: "1"}
     */
    @XxlJob("dwdCustBaseInfoJobHandler")
    public void execute() {
        log.info("开始执行集团客户基础信息同步定时任务");
        XxlJobHelper.log("开始执行集团客户基础信息同步定时任务");

        // 任务参数
        String sftpConfigType = SFTP_CONFIG_TYPE; // 默认配置类型
        Date targetDate = DateUtils.parseDate(DateUtils.getBeforeNDays(DEFAULT_DAYS_BEFORE)); // 默认T-1账期
        int defaultDaysBefore = DEFAULT_DAYS_BEFORE;// 默认T-x账期

        // 获取定时任务传入参数
        String jobStr = XxlJobHelper.getJobParam();
        if (StringUtils.isNotBlank(jobStr)) {
            JSONObject json = JSONUtil.parseObj(jobStr);

            // 指定配置类型
            String sftpConfigTypeStr = json.getStr(CONFIG_TYPE, "");
            if (StringUtils.isNotBlank(sftpConfigTypeStr)) {
                sftpConfigType = sftpConfigTypeStr;
                log.info("使用指定的配置类型: {}", sftpConfigType);
            }

            // 指定日期
            String targetDateStr = json.getStr(TARGET_DATE, "");
            // 指定获取T-x日期
            int defaultDaysBeforeInt = json.getInt(DEFAULT_DAYS_BEFORE_STR, -1);
            if (StringUtils.isNotBlank(targetDateStr)) {
                try {
                    targetDate = DateUtils.dateTime(SIMPLE_DATE_FORMAT, targetDateStr);
                    log.info("成功解析日期参数: {}", DateUtils.parseDateToStr(SIMPLE_DATE_FORMAT, targetDate));
                } catch (Exception e) {
                    log.warn("日期参数格式错误，使用当前T-1日期。参数: {}, 错误: {}", targetDateStr, e.getMessage());
                    XxlJobHelper.log("日期参数格式错误，使用当前T-1日期");
                }
            } else if (defaultDaysBeforeInt > -1) {
                try {
                    defaultDaysBefore = defaultDaysBeforeInt;
                    targetDate = DateUtils.parseDate(DateUtils.getBeforeNDays(defaultDaysBefore)); // 传参T-x账期
                    log.info("成功解析T-" + defaultDaysBeforeInt + "日期参数: {}", DateUtils.parseDateToStr(SIMPLE_DATE_FORMAT, targetDate));
                } catch (Exception e) {
                    log.warn("日期参数格式错误，使用当前日期。参数: {}, 错误: {}", defaultDaysBeforeInt, e.getMessage());
                    XxlJobHelper.log("解析T-x参数错误，使用当前T-1日期");
                }
            }
        }

        log.info("数据参数：1.配置类型：{}, 2.日期参数：{}, 3.T-x 账期：{}",
            sftpConfigType, DateUtils.parseDateToStr(SIMPLE_DATE_FORMAT, targetDate), defaultDaysBefore);
        XxlJobHelper.log("数据参数：1.配置类型：{}, 2.日期参数：{}, 3.T-x 账期：{}",
            sftpConfigType, DateUtils.parseDateToStr(SIMPLE_DATE_FORMAT, targetDate), defaultDaysBefore);

        String localFilePath = null;
        try {
            // 获取FTP配置参数
            Map<String, String> paramMap = getFtpConfigParams(sftpConfigType);
            if (paramMap == null || paramMap.isEmpty()) {
                String errorMsg = "未找到FTP配置参数，配置类型: " + sftpConfigType;
                log.error(errorMsg);
                XxlJobHelper.handleFail(errorMsg);
                return;
            }

            String ftpHost = paramMap.get(SFTP_HOST);
            String ftpPort = paramMap.get(SFTP_PORT);
            String ftpDirectory = paramMap.get(SFTP_DATA_ROOT_PATH);
            String ftpLocal = paramMap.get(SFTP_DATA_LOCAL_PATH);

            // 目标FTP文件路径
            String dateStr = DateUtils.parseDateToStr(SIMPLE_DATE_FORMAT, targetDate);
            String fileName = FILE_NAME_PREFIX + dateStr + FILE_EXTENSION;
            String remoteFilePath = ftpDirectory + File.separator + dateStr + File.separator + fileName;

            // 本地基础路径
            LOCAL_TEMP_DIR = ftpLocal + File.separator + dateStr + File.separator;

            log.info("FTP配置 - Host: {}, Port: {}, Directory: {}, LocalDirectory: {}",
                ftpHost, ftpPort, remoteFilePath, LOCAL_TEMP_DIR);
            XxlJobHelper.log("FTP配置 - Host: {}, Port: {}, Directory: {}, LocalDirectory: {}",
                ftpHost, ftpPort, remoteFilePath, LOCAL_TEMP_DIR);

            // 1. 下载文件到本地临时目录
            localFilePath = downloadFileFromSftp(sftpConfigType, remoteFilePath, dateStr);
            if (localFilePath == null || localFilePath.isEmpty()) {
                String errorMsg = "文件下载失败: " + remoteFilePath;
                log.error(errorMsg);
                XxlJobHelper.handleFail(errorMsg);
                return;
            }
            log.info("文件下载成功，本地路径: {}", localFilePath);
            XxlJobHelper.log("文件下载成功，本地路径: {}", localFilePath);

            // 2. 备份原表数据
            log.info("开始备份原表数据");
            XxlJobHelper.log("开始备份原表数据");
            ftpCustBaseInfoService.backUpFtpCustBaseInfo();
            log.info("原表数据备份完成");

            // 3. 使用批处理器处理本地大文件
            log.info("开始解析本地文件并批量入库，每批处理 {} 条数据", DEFAULT_BATCH_SIZE);
            XxlJobHelper.log("开始解析本地文件并批量入库，每批处理 {} 条数据", DEFAULT_BATCH_SIZE);

            // 统计变量
            final int[] totalSuccess = {0};
            final int[] totalRows = {0};

            // 使用本地文件批处理器处理大文件
            processLocalFileInBatches(localFilePath, DEFAULT_BATCH_SIZE, batchData -> {
                try {
                    // 如果批次为空，继续处理下一批
                    if (batchData.isEmpty()) {
                        return true;
                    }

                    // 记录总行数
                    totalRows[0] += batchData.size();

                    // 将批次数据转换为字符串
                    StringBuilder txtContent = new StringBuilder();
                    for (String line : batchData) {
                        txtContent.append(line).append("\n");
                    }

                    // 解析文本数据
                    List<FtpCustBaseInfoDo> ftpCustBaseInfoList = groupFileParser.parseFile(
                        txtContent.toString(), FtpCustBaseInfoDo.class, FILE_SEPARATOR);

                    if (!ftpCustBaseInfoList.isEmpty()) {
                        // 设置源表标识
                        ftpCustBaseInfoList.forEach(item -> {
                            if (StringUtils.isBlank(item.getSrcTable())) {
                                item.setSrcTable(SRC_TABLE);
                            }
                        });

                        // 批量插入数据库
                        int success = ftpCustBaseInfoService.batchInsert(ftpCustBaseInfoList);
                        totalSuccess[0] += success;

                        log.info("批次处理完成，当前已处理 {} 条记录，成功导入 {} 条",
                            totalRows[0], totalSuccess[0]);
                        XxlJobHelper.log("批次处理完成，当前已处理 {} 条记录，成功导入 {} 条",
                            totalRows[0], totalSuccess[0]);
                    }

                    return true; // 继续处理下一批数据
                } catch (Exception e) {
                    log.error("批次处理失败: {}", e.getMessage(), e);
                    XxlJobHelper.log("批次处理失败: {}", e.getMessage());
                    return true; // 发生异常时继续处理
                }
            });

            // 验证是否有数据被处理
            if (totalRows[0] == 0) {
                log.warn("文件中没有有效数据需要导入");
                XxlJobHelper.handleSuccess("文件中没有有效数据需要导入");
                return;
            }

            // 4. 任务完成，记录结果
            String successMsg = String.format("集团客户基础信息同步执行完成，成功导入: %d 条，失败: %d 条",
                totalSuccess[0], totalRows[0] - totalSuccess[0]);
            log.info(successMsg);
            XxlJobHelper.handleSuccess(successMsg);

        } catch (Exception e) {
            // 5. 处理异常
            String errorMsg = "集团客户基础信息同步任务执行失败: " + e.getMessage();
            log.error(errorMsg, e);
            XxlJobHelper.log("任务执行失败: {}", e.getMessage());
            XxlJobHelper.handleFail(errorMsg);
        } finally {
            if (StringUtils.isNotBlank(localFilePath)) {
                // 6. 删除本地临时文件
                deleteLocalFile(localFilePath);
            }
        }
    }

    /**
     * 从SFTP下载文件到本地临时目录
     *
     * @param configType     SFTP配置类型
     * @param remoteFilePath 远程文件路径
     * @param dateStr        日期字符串（用于生成唯一文件名）
     * @return 本地文件路径，如果下载失败返回null
     */
    private String downloadFileFromSftp(String configType, String remoteFilePath, String dateStr) {
        File tempDir = new File(LOCAL_TEMP_DIR);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
            log.info("创建本地临时目录: {}", LOCAL_TEMP_DIR);
        }

        // 生成本地文件路径
        String fileName = remoteFilePath.substring(remoteFilePath.lastIndexOf("/") + 1);
        String localFilePath = LOCAL_TEMP_DIR + fileName;

        try {
            log.info("开始从SFTP下载文件，远程路径: {}, 本地路径: {}", remoteFilePath, localFilePath);
            XxlJobHelper.log("开始从SFTP下载文件，远程路径: {}, 本地路径: {}", remoteFilePath, localFilePath);

            // 使用SftpUtil下载文件
            sftpUtil.downloadFile(configType, remoteFilePath, localFilePath);

            // 验证文件是否下载成功
            File localFile = new File(localFilePath);
            if (!localFile.exists()) {
                log.error("文件下载失败，本地文件不存在: {}", localFilePath);
                return null;
            }

            long fileSize = localFile.length();
            log.info("文件下载成功，文件大小: {} bytes", fileSize);
            XxlJobHelper.log("文件下载成功，文件大小: {} bytes", fileSize);

            return localFilePath;
        } catch (Exception e) {
            log.error("从SFTP下载文件到本地临时目录错误: {}", e.getMessage(), e);
            XxlJobHelper.log("从SFTP下载文件到本地临时目录错误: {}", e.getMessage());
            // 删除可能存在的部分下载文件
            deleteLocalFile(localFilePath);
            return null;
        }
    }

    /**
     * 处理本地文件，按批次读取
     *
     * @param localFilePath  本地文件路径
     * @param batchSize      每批处理的行数
     * @param batchProcessor 批处理器
     */
    private void processLocalFileInBatches(String localFilePath, int batchSize, SftpUtil.BatchProcessor batchProcessor) {
        BufferedReader br = null;
        try (FileReader fr = new FileReader(localFilePath)) {
            if (fr == null) {
                throw new RuntimeException("文件不存在: " + localFilePath);
            }

            br = new BufferedReader(fr, DEFAULT_BUFFER_SIZE);

            List<String> batch = new ArrayList<>(batchSize);
            String line;
            long totalLines = 0;
            long startTime = System.currentTimeMillis();

            while ((line = br.readLine()) != null) {
                batch.add(line);
                totalLines++;

                // 当批次达到指定大小时，进行处理
                if (batch.size() >= batchSize) {
                    log.info("处理批次，当前累计 {} 行", totalLines);
                    boolean continueProcessing = batchProcessor.processBatch(new ArrayList<>(batch));
                    batch.clear();

                    if (!continueProcessing) {
                        log.info("用户中断文件处理，已处理 {} 行", totalLines);
                        break;
                    }
                }
            }

            // 处理最后一批不足batchSize的数据
            if (!batch.isEmpty()) {
                log.info("处理最后一批数据，共 {} 行", batch.size());
                batchProcessor.processBatch(batch);
                batch.clear();
            }

            // 计算总处理时间和平均速度
            long endTime = System.currentTimeMillis();
            double totalSeconds = (endTime - startTime) / 1000.0;
            double avgSpeed = totalSeconds > 0 ? totalLines / totalSeconds : 0;

            log.info("本地文件处理完成，共处理 {} 行数据, 总耗时: {}秒, 平均速度: {}行/秒",
                totalLines, totalSeconds, avgSpeed);
        } catch (Exception e) {
            log.error("处理本地文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("处理本地文件失败", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    log.error("关闭文件阅读器失败: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 删除本地临时文件
     *
     * @param filePath 文件路径
     */
    private void deleteLocalFile(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            try {
                File file = new File(filePath);
                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (deleted) {
                        log.info("本地临时文件已删除: {}", filePath);
                    } else {
                        log.warn("本地临时文件删除失败: {}", filePath);
                    }
                }
            } catch (Exception e) {
                log.error("删除本地临时文件失败: {}, 错误: {}", filePath, e.getMessage());
            }
        }
    }

    /**
     * 获取FTP配置参数
     *
     * @param configType 配置类型
     * @return FTP配置参数Map
     */
    private Map<String, String> getFtpConfigParams(String configType) {
        try {
            // 查询该配置类型下的所有参数
            List<SysParamDo> paramList = sysParamMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysParamDo>()
                    .eq(SysParamDo::getType, configType)
            );

            if (paramList == null || paramList.isEmpty()) {
                log.warn("未找到配置类型 {} 的参数", configType);
                return null;
            }

            // 转换为Map
            Map<String, String> paramMap = new HashMap<>();
            for (SysParamDo param : paramList) {
                paramMap.put(param.getCode(), param.getValue());
            }

            return paramMap;
        } catch (Exception e) {
            log.error("获取FTP配置参数失败，配置类型: {}", configType, e);
            return null;
        }
    }
}




