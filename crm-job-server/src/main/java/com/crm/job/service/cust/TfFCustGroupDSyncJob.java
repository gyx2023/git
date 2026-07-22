package com.crm.job.service.cust;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.crm.system.enums.CustStateEnum;
import com.crm.common.utils.DateUtils;
import com.crm.job.utils.GroupFileParser;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.dao.sys.SysArea;
import com.crm.system.domain.dao.ZqGrpGroupInfo;
import com.crm.system.domain.dao.TfFCustGroupDDo;
import com.crm.system.domain.dao.SysParamDo;
import com.crm.system.domain.dao.ZqGrpGroupLog;
import com.crm.system.mapper.sys.SysAreaMapper;
import com.crm.system.mapper.ZqGrpGroupInfoMapper;
import com.crm.system.mapper.ZqGrpGroupLogMapper;
import com.crm.system.mapper.param.SysParamMapper;
import com.crm.system.service.TfFCustGroupDService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static com.crm.system.utils.Constants.JF_COMMON_FTP;

/**
 * 集团实体客户全量日数据同步定时任务
 * 从SFTP读取客户集团全量日信息文件并导入到tf_f_cust_group_d表
 *
 * @author crm
 */
@Slf4j
@Component
public class TfFCustGroupDSyncJob {

    /**
     * 批量处理大小
     */
    private static final int DEFAULT_BATCH_SIZE = 500;
    /**
     * 集团级别标识
     */
    private static final String GRP_LEVEL_IDENTIFIER = "Y";
    /**
     * 生成集团编码的最大重试次数
     */
    private static final int GENERATE_GRP_CODE_MAX_ATTEMPTS = 10;
    /**
     * 集团编码长度
     */
    private static final int GRP_CODE_LENGTH = 15;
    /**
     * 默认提前天数
     */
    private static final int DEFAULT_DAYS_BEFORE = 1;
    /**
     * 布尔字符串true
     */
    private static final String BOOLEAN_STRING_TRUE = "true";
    /**
     * 文件分隔符
     */
    private static final String FILE_SEPARATOR = "\\x1b";
    /**
     * 远程目录
     */
    private static final String DATA_ROOT_PATH = "sftp.dataRootPath";
    /**
     * 远程目录
     */
    private static final String DATA_LOCAL_PATH = "sftp.dataLocalPath";
    /**
     * 文件名前缀
     */
    private static final String FILE_NAME = "/DWD_D_CUS_ZQZT_CUST_GROUP_";
    /**
     * 文件名类型后缀
     */
    private static final String FIELD_EXTENSION = ".txt";
    /**
     * XXL传递配置类型
     */
    private static final String CONFIG_TYPE = "configType";
    /**
     * XXL传递日期参数
     */
    private static final String TARGET_DATE = "targetDate";
    /**
     * XXL传递是否入库参数
     */
    private static final String IS_IN_GRP = "isInGrp";
    /**
     * XXL传递T-x账期参数
     */
    private static final String DEFAULT_DAYS_BEFORE_STR = "defaultDaysBefore";
    /**
     * 本地临时目录
     */
    private static String LOCAL_TEMP_DIR = "/tmp/sftp_download/";
    /**
     * 默认缓冲区大小（8KB）
     */
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    /**
     * 变更类型
     */
    private static final String LOG_UPDATE_TYPE = "T1";
    /**
     * 变更类型 - 变更前
     */
    private static final String BEFORE_LOG_DATA_TYPE = "1";
    /**
     * 变更类型 - 变更前
     */
    private static final String AFTER_LOG_DATA_TYPE = "2";

    private final Map<String, String> cityMap = new HashMap<>();

    private volatile boolean cityListInitialized = false;

    private static final Map<String, String> custStateMap = new HashMap<>();

    private static final String FORMATTER = "yyyy-MM-dd HH:mm:ss";

    private static final String SIMPLE_DATE_FORMAT = "yyyyMMdd";

    @Resource
    private SftpUtil sftpUtil;

    @Resource
    private GroupFileParser groupFileParser;

    @Resource
    private SysParamMapper sysParamMapper;

    @Resource
    private TfFCustGroupDService dwdDCusZqztCustGrouService;

    @Resource
    private SysAreaMapper sysAreaMapper;

    @Resource
    private ZqGrpGroupInfoMapper zqGrpGroupInfoMapper;

    @Resource
    private ZqGrpGroupLogMapper zqGrpGroupLogMapper;


    /**
     * 集团实体客户全量日数据同步定时任务
     * 在xxl-job调度中心配置JobHandler为：tfFCustGroupDSyncJobHandler
     * 支持传入参数格式：
     * 1. 仅配置类型：configType: - 指定配置类型，使用当前日期 - 例：grp_group_info_syn
     * 2. 单日期参数：targetDate：yyyyMMdd - 指定日期 - 例：20260228
     * 3. 否入库zqzt_grp_group_info表：isInGrp：isInGrp - 指定是否入库 - 例：true
     * 4. T-x账期：defaultDaysBefore： - 指定读取T-x账期，且传递账期不为负数 - 例：1
     * 参数样例：{"configType": "zqzt_cust_group_syn", "targetDate": "20260228", "isInGrp": "true", defaultDaysBefore: "1"}
     */
    @XxlJob("tfFCustGroupDSyncJobHandler")
    public void execute() {
        log.info("开始执行集团实体客户全量日数据同步定时任务");
        XxlJobHelper.log("开始执行集团实体客户全量日数据同步定时任务");

        // 初始化地市列表（仅在首次执行时加载）
        if (!cityListInitialized) {
            synchronized (this) {
                if (!cityListInitialized) {
                    initJobData();
                    cityListInitialized = true;
                }
            }
        }

        // 任务参数
        String sftpConfigType = JF_COMMON_FTP; // 默认配置类型
        Date targetDate = DateUtils.parseDate(DateUtils.getBeforeNDays(DEFAULT_DAYS_BEFORE)); // 默认T-2账期
        boolean isInGrp = false; // 默认是否入库类型
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
                    log.warn("日期参数格式错误，使用当前日期。参数: {}, 错误: {}", targetDateStr, e.getMessage());
                    XxlJobHelper.log("日期参数格式错误，使用当前日期");
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

            // 指定是否入库类型
            String isInGrpStr = json.getStr(IS_IN_GRP, "");
            if (StringUtils.isNotBlank(isInGrpStr)) {
                isInGrp = BOOLEAN_STRING_TRUE.equals(isInGrpStr);
                log.info("使用指定的否入库类型: {}", isInGrp);
            }
        }
        log.info("数据参数：1.配置类型：{}, 2.日期参数：{}, 3.是否入库：{}, T-x账期:{}",
            sftpConfigType, DateUtils.parseDateToStr(SIMPLE_DATE_FORMAT, targetDate), isInGrp, defaultDaysBefore);


        String localFilePath = null;
        try {
            String dateStr = DateUtils.parseDateToStr(SIMPLE_DATE_FORMAT, targetDate);
            // 生成目标文件路径：/opt/home/bi/dataout/ZQ/yyyyMMdd/DWD_D_CUS_ZQZT_CUST_GROUP_yyyyMMdd.txt
            SysParamDo grpCustGroupSyn = sysParamMapper.selectOne(new LambdaQueryWrapper<SysParamDo>()
                .eq(SysParamDo::getType, sftpConfigType)
                .eq(SysParamDo::getCode, DATA_ROOT_PATH));

            // 获取基础路径
            String basePath = grpCustGroupSyn.getValue(); // 例如 "/opt/home/bi/dataout/ZQ/"

            // 生成本地目标文件路径：/opt/home/bi/dataout/ZQ/yyyyMMdd/DWD_D_CUS_ZQZT_CUST_GROUP_yyyyMMdd.txt
            SysParamDo localPathSyn = sysParamMapper.selectOne(new LambdaQueryWrapper<SysParamDo>()
                .eq(SysParamDo::getType, sftpConfigType)
                .eq(SysParamDo::getCode, DATA_LOCAL_PATH)
            );

            // 获取本地基础路径
            LOCAL_TEMP_DIR = localPathSyn.getValue() + dateStr + File.separator; // 例如 "/opt/home/bi/dataout/ZQ/"

            // 构建文件路径
            String remoteFilePath = basePath + dateStr + FILE_NAME + dateStr + FIELD_EXTENSION;

            log.info("使用配置类型 '{}' 生成的远程文件路径: {}", sftpConfigType, remoteFilePath);

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

            // 2. 清空tf_f_cust_group_d表数据
            dwdDCusZqztCustGrouService.truncateTable();

            // 3. 使用批处理器处理大文件，每批处理100行数据以优化内存使用
            log.info("开始使用批处理器处理大文件，每批处理 {} 行数据", DEFAULT_BATCH_SIZE);
            XxlJobHelper.log("开始使用批处理器处理大文件，每批处理 {} 行数据", DEFAULT_BATCH_SIZE);

            // 统计变量
            final int[] totalSuccess = {0};
            final int[] totalRows = {0};

            // 使用指定的SFTP配置类型和批处理器处理文件，配置更大的缓冲区
            boolean finalIsInGrp = isInGrp;
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

                    // 解析文本数据 - 使用通用解析方法
                    List<TfFCustGroupDDo> tfFCustGroupDList = groupFileParser.parseFile(txtContent.toString(), TfFCustGroupDDo.class, FILE_SEPARATOR);

                    if (!tfFCustGroupDList.isEmpty()) {

                        tfFCustGroupDList.forEach(p -> {
                            if (StringUtils.isNotBlank(p.getEparchyCode())) {
                                p.setEparchyCode(cityMap.get(p.getEparchyCode()) == null ? p.getEparchyCode() : cityMap.get(p.getEparchyCode()));
                            }
                            if (StringUtils.isNotBlank(p.getBCityCode())) {
                                p.setBCityCode(cityMap.get(p.getBCityCode()) == null ? p.getBCityCode() : cityMap.get(p.getBCityCode()));
                            }
                        });
                        // 批量插入数据库
                        int success = dwdDCusZqztCustGrouService.batchInsert(tfFCustGroupDList);
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
                    // 发生异常时的处理
                    return true; // 可以根据业务需求决定是否继续处理，这里选择继续
                }
            });

            if (finalIsInGrp) {
                String createDate = DateUtils.parseDateToStr(FORMATTER, targetDate);
                // 获取未存入zqzt_grp_group_info表数据
                List<TfFCustGroupDDo> tfFCustGroupDList = dwdDCusZqztCustGrouService.getTfFCustGroupDList(createDate);
                // 处理zqzt_grp_group_info表的逻辑
                processGrpGroupInfo(tfFCustGroupDList, createDate);
            }

            // 验证是否有数据被处理
            if (totalRows[0] == 0) {
                log.warn("文件中没有有效数据需要导入");
                XxlJobHelper.handleSuccess("文件中没有有效数据需要导入");
                return;
            }

            // 4. 任务完成，记录结果
            String successMsg = String.format("集团实体客户全量日数据入库执行完成，成功导入: %d 条，失败: %d 条",
                totalSuccess[0], totalRows[0] - totalSuccess[0]);
            log.info(successMsg);
            XxlJobHelper.handleSuccess(successMsg);

        } catch (Exception e) {
            // 5. 处理异常
            String errorMsg = "集团实体客户全量日数据入库任务执行失败: " + e.getMessage();
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

    private void initJobData() {
        for (SysArea sysArea : sysAreaMapper.selectList()) {
            if (StringUtils.isNotBlank(sysArea.getJtCode())) {
                cityMap.put(sysArea.getJtCode(), String.valueOf(sysArea.getId()));
            }
        }
        custStateMap.put(CustStateEnum.CUST_STATE_0.getCode(), CustStateEnum.CUST_STATE_0.getLabel());
        custStateMap.put(CustStateEnum.CUST_STATE_1.getCode(), CustStateEnum.CUST_STATE_1.getLabel());
        custStateMap.put(CustStateEnum.CUST_STATE_2.getCode(), CustStateEnum.CUST_STATE_2.getLabel());
        custStateMap.put(CustStateEnum.CUST_STATE_3.getCode(), CustStateEnum.CUST_STATE_3.getLabel());
        custStateMap.put(CustStateEnum.CUST_STATE_4.getCode(), CustStateEnum.CUST_STATE_4.getLabel());
    }

    /**
     * 处理zqzt_grp_group_info表的逻辑
     * 获取全量未存入zqzt_grp_group_info表数据，并存入zqzt_grp_group_info表
     *
     * @param tfFCustGroupDList 客户集团全量日列表
     * @param createDate        创建时间
     */
    private void processGrpGroupInfo(List<TfFCustGroupDDo> tfFCustGroupDList, String createDate) {
        log.info("-----------------存入政企集团客户信息开始-----------------");
        XxlJobHelper.log("-----------------存入政企集团客户信息开始-----------------");

        List<ZqGrpGroupInfo> groupInfoList = new ArrayList<>();
        int batchSize = DEFAULT_BATCH_SIZE; // 每批处理数量
        int count = 0;
        int totalProcessed = 0; // 总处理记录数
        int totalSuccess = 0; // 总成功导入数
        int batchCount = 0; // 批次计数
        int successBatchCount = 0; // 成功批次计数
        int failBatchCount = 0; // 失败批次计数

        for (TfFCustGroupDDo custGroup : tfFCustGroupDList) {
            // 获取zqGrpGroup信息
            ZqGrpGroupInfo newGroup = new ZqGrpGroupInfo();
            newGroup.setGrpName(custGroup.getCustName());
            newGroup.setGrpCode(generateUniqueGrpCode());
            newGroup.setHqGrpCode(custGroup.getGroupId());
            newGroup.setCustId(custGroup.getProvinceCustId());
            newGroup.setGrpStatus(custStateMap.get(custGroup.getCustState()));
            newGroup.setAreaId(custGroup.getEparchyCode());
            newGroup.setCountyId(custGroup.getBCityCode());
            newGroup.setIndustryMajorKind(custGroup.getFirstCallingTypeCode());
            newGroup.setIndustrySubKind(custGroup.getSecondCallingTypeCode());
            newGroup.setIndustryDetailKind(custGroup.getThirdCallingTypeCode());
            newGroup.setGrpCustType(custGroup.getCustClassType());
            newGroup.setGrpLevel(CustStateEnum.CUST_STATE_2.getCode().equals(custGroup.getCustState()) ? GRP_LEVEL_IDENTIFIER : null);
            //资料创建人
            newGroup.setGrpCreator(custGroup.getCreateOaUserCode());

            // 转换字符串日期为Date类型
            if (StringUtils.isNotBlank(custGroup.getCreateDate())) {
                try {
                    Date grpCreateTime = DateUtils.dateTime(FORMATTER, custGroup.getCreateDate());
                    newGroup.setGrpCreateTime(grpCreateTime);
                } catch (Exception e) {
                    log.info("日期转换失败，createDate:{}, error:{}", custGroup.getCreateDate(), e.getMessage());
                }
            }
            newGroup.setCreateTime(DateUtils.getNowDate());
            newGroup.setUpdateTime(DateUtils.getNowDate());

            // 保存zqGrpGroup信息到List
            groupInfoList.add(newGroup);

            count++;
            totalProcessed++;

            // 每500条执行一次批量插入
            if (count % batchSize == 0) {
                batchCount++;
                int currentBatchSize = batchSize;
                log.info("第 {} 批次开始批量插入政企集团客户信息，本次处理 {} 条", batchCount, currentBatchSize);
                XxlJobHelper.log("第 {} 批次开始批量插入政企集团客户信息，本次处理 {} 条", batchCount, currentBatchSize);

                try {
                    boolean batchSuccess = zqGrpGroupInfoMapper.insertBatch(groupInfoList);
                    if (batchSuccess) {
                        // 存入
                        successBatchCount++;
                        totalSuccess += currentBatchSize; // 假设全部成功
                        log.info("批次处理完成，当前已处理 {} 条记录，成功导入 {} 条", totalProcessed, totalSuccess);
                        XxlJobHelper.log("批次处理完成，当前已处理 {} 条记录，成功导入 {} 条", totalProcessed, totalSuccess);

                        // 批量存入日志表（新增-变更后）
                        batchInsertGroupLogForNewRecords(groupInfoList);
                    } else {
                        // 批量插入失败
                        failBatchCount++;
                        log.error("第 {} 批次批量插入失败，本次处理 {} 条", batchCount, currentBatchSize);
                        XxlJobHelper.log("第 {} 批次批量插入失败，本次处理 {} 条", batchCount, currentBatchSize);
                    }
                } catch (Exception e) {
                    failBatchCount++;
                    log.error("第 {} 批次批量插入异常，错误信息:{}", batchCount, e.getMessage(), e);
                    XxlJobHelper.log("第 {} 批次批量插入异常，错误信息:{}", batchCount, e.getMessage());
                }

                groupInfoList.clear(); // 清空list，准备下一批
            }
        }

        // 处理剩余不足500条的数据
        if (!groupInfoList.isEmpty()) {
            batchCount++;
            int lastBatchSize = groupInfoList.size();
            log.info("第 {} 批次(最后批次)开始批量插入政企集团客户信息，本次处理剩余 {} 条", batchCount, lastBatchSize);
            XxlJobHelper.log("第 {} 批次(最后批次)开始批量插入政企集团客户信息，本次处理剩余 {} 条", batchCount, lastBatchSize);

            try {
                boolean batchSuccess = zqGrpGroupInfoMapper.insertBatch(groupInfoList);
                if (batchSuccess) {
                    successBatchCount++;
                    totalSuccess += lastBatchSize; // 假设全部成功
                    log.info("最后批次处理完成，本次处理 {} 条记录，成功导入 {} 条", lastBatchSize, lastBatchSize);
                    XxlJobHelper.log("最后批次处理完成，本次处理 {} 条记录，成功导入 {} 条", lastBatchSize, lastBatchSize);

                    // 批量存入日志表（新增-变更后）
                    batchInsertGroupLogForNewRecords(groupInfoList);
                } else {
                    // 批量插入失败
                    failBatchCount++;
                    log.error("最后批次批量插入失败，本次处理 {} 条", lastBatchSize);
                    XxlJobHelper.log("最后批次批量插入失败，本次处理 {} 条", lastBatchSize);
                }
            } catch (Exception e) {
                failBatchCount++;
                log.error("最后批次批量插入异常，错误信息:{}", e.getMessage(), e);
                XxlJobHelper.log("最后批次批量插入异常，错误信息:{}", e.getMessage());
            }
        }

        // 统计总结
        String successMsg = String.format("政企集团客户信息表总计处理完成，累计处理 %d 条记录，成功导入 %d 条", totalProcessed, totalSuccess);
        log.info(successMsg);
        XxlJobHelper.handleSuccess(successMsg);

        // 添加处理统计
        log.info("数据处理统计：处理总条数={}，总批次={}，成功批次={}，失败批次={}，成功导入数={}",
            totalProcessed, batchCount, successBatchCount, failBatchCount, totalSuccess);
        XxlJobHelper.log("数据处理统计：处理总条数={}，总批次={}，成功批次={}，失败批次={}，成功导入数={}",
            totalProcessed, batchCount, successBatchCount, failBatchCount, totalSuccess);

        // 更新集团级别与集团状态信息
        List<ZqGrpGroupInfo> ZqGrpGroupInfoList = zqGrpGroupInfoMapper.getUpdQuery(createDate);
        if (ZqGrpGroupInfoList.size() > 0) {
            // 记录变更前日志（dataType=1）
            batchInsertGroupLogForUpdateBefore(ZqGrpGroupInfoList);

            boolean b = zqGrpGroupInfoMapper.updateBatchById(ZqGrpGroupInfoList);
            if (b) {
                log.info("更新集团级别与集团状态信息完成：处理总条数={}", ZqGrpGroupInfoList.size());
                XxlJobHelper.log("更新集团级别与集团状态信息完成：处理总条数={}", ZqGrpGroupInfoList.size());

                // 记录变更后日志（dataType=2）
                batchInsertGroupLog(ZqGrpGroupInfoList, AFTER_LOG_DATA_TYPE);
            } else {
                log.info("更新集团级别与集团状态信息失败");
                XxlJobHelper.handleFail("更新集团级别与集团状态信息失败");
            }
        } else {
            log.info("更新集团级别与集团状态信息完成：处理总条数=0");
            XxlJobHelper.log("更新集团级别与集团状态信息完成：处理总条数=0");
        }


        log.info("-----------------存入政企集团客户信息结束-----------------");
        XxlJobHelper.log("-----------------存入政企集团客户信息结束-----------------");
    }

    /**
     * 批量存入日志表 - 用于新增记录（查询回插入后的记录以获取grpId）
     *
     * @param groupInfoList 刚插入的集团信息列表
     */
    private void batchInsertGroupLogForNewRecords(List<ZqGrpGroupInfo> groupInfoList) {
        if (groupInfoList == null || groupInfoList.isEmpty()) {
            return;
        }
        try {
            List<String> hqGrpCodes = groupInfoList.stream()
                .map(ZqGrpGroupInfo::getHqGrpCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

            if (hqGrpCodes.isEmpty()) {
                log.warn("批量存入日志表失败：hqGrpCode列表为空");
                return;
            }

            // 查询回已插入的记录，获取完整的grpId
            List<ZqGrpGroupInfo> insertedRecords = zqGrpGroupInfoMapper.selectList(
                new LambdaQueryWrapper<ZqGrpGroupInfo>().in(ZqGrpGroupInfo::getHqGrpCode, hqGrpCodes));

            if (insertedRecords.isEmpty()) {
                log.warn("批量存入日志表失败：未查询到已插入的记录");
                return;
            }

            batchInsertGroupLog(insertedRecords, AFTER_LOG_DATA_TYPE);
        } catch (Exception e) {
            log.error("批量存入日志表（新增）失败: {}", e.getMessage(), e);
            XxlJobHelper.log("批量存入日志表（新增）失败: {}", e.getMessage());
        }
    }

    /**
     * 批量存入日志表 - 用于更新前记录变更前状态
     *
     * @param updList getUpdQuery返回的待更新列表（含新值）
     */
    private void batchInsertGroupLogForUpdateBefore(List<ZqGrpGroupInfo> updList) {
        if (updList == null || updList.isEmpty()) {
            return;
        }
        try {
            List<Long> grpIds = updList.stream()
                .map(ZqGrpGroupInfo::getGrpId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

            if (grpIds.isEmpty()) {
                log.warn("批量存入日志表（更新前）失败：grpId列表为空");
                return;
            }

            // 查询原始记录作为变更前数据
            List<ZqGrpGroupInfo> beforeRecords = zqGrpGroupInfoMapper.selectBatchIds(grpIds);

            if (beforeRecords.isEmpty()) {
                log.warn("批量存入日志表（更新前）失败：未查询到原始记录");
                return;
            }

            batchInsertGroupLog(beforeRecords, BEFORE_LOG_DATA_TYPE);
        } catch (Exception e) {
            log.error("批量存入日志表（更新前）失败: {}", e.getMessage(), e);
            XxlJobHelper.log("批量存入日志表（更新前）失败: {}", e.getMessage());
        }
    }

    /**
     * 批量存入日志表 - 通用方法
     *
     * @param infoList 集团信息列表
     * @param dataType 数据类型：1-变更前,2-变更后
     */
    private void batchInsertGroupLog(List<ZqGrpGroupInfo> infoList, String dataType) {
        if (infoList == null || infoList.isEmpty()) {
            return;
        }
        try {
            List<ZqGrpGroupLog> logList = new ArrayList<>(infoList.size());
            Date now = DateUtils.getNowDate();
            for (ZqGrpGroupInfo info : infoList) {
                ZqGrpGroupLog log = BeanUtil.toBean(info, ZqGrpGroupLog.class);
                log.setUpdateType(LOG_UPDATE_TYPE);
                log.setDataType(dataType);
                log.setCreateTime(now);
                log.setUpdateTime(now);
                logList.add(log);
            }
            zqGrpGroupLogMapper.insertBatch(logList);
            log.info("批量存入日志表成功，数量: {}, dataType: {}", logList.size(), dataType);
            XxlJobHelper.log("批量存入日志表成功，数量: {}, dataType: {}", logList.size(), dataType);
        } catch (Exception e) {
            log.error("批量存入日志表失败: {}", e.getMessage(), e);
            XxlJobHelper.log("批量存入日志表失败: {}", e.getMessage());
        }
    }

    private String generateUniqueGrpCode() {
        String grpCode;
        int attempt = 0;

        do {
            // 生成15位随机数字
            grpCode = generateRandomGrpCode();
            attempt++;

            // 检查数据库中是否已存在
            if (attempt < GENERATE_GRP_CODE_MAX_ATTEMPTS) {
                LambdaQueryWrapper<ZqGrpGroupInfo> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ZqGrpGroupInfo::getGrpCode, grpCode);

                if (zqGrpGroupInfoMapper.selectCount(queryWrapper) > 0) {
                    grpCode = null; // 重复，需要重新生成
                }
            }
        } while (grpCode == null && attempt < GENERATE_GRP_CODE_MAX_ATTEMPTS);

        if (grpCode == null) {
            // 如果达到最大重试次数仍未找到唯一值，抛出异常或使用备用方案
            throw new RuntimeException("无法生成唯一的grpCode");
        }

        return grpCode;
    }

    private String generateRandomGrpCode() {
        Random random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < GRP_CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * tf_f_cust_group_d表的cust_class_type字段同步到zqzt_grp_group_info表的GRP_CUST_TYPE字段映射
     * 转换集团客户类型
     * 1 -> 600 (普通客户)
     * 2 -> 500 (中小企业客户)
     * 3 -> 400 (大客户)
     *
     * @param custClassType 客户分类类型
     * @return 集团客户类型编码
     */
    private String convertGrpCustType(String custClassType) {
        if (StringUtils.isBlank(custClassType)) {
            return null;
        }

        switch (custClassType) {
            case "1":
                return "600"; // 普通客户
            case "2":
                return "500"; // 中小企业客户
            case "3":
                return "400"; // 大客户
            default:
                log.warn("未知的客户分类类型: {}", custClassType);
                return custClassType; // 未知类型保留原值
        }
    }
}
