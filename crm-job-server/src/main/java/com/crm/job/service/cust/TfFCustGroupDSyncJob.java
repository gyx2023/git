package com.crm.job.service.cust;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.crm.common.utils.DateUtils;
import com.crm.job.utils.GroupFileParser;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.SysArea;
import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.cust.TfFCustGroupDDo;
import com.crm.system.domain.param.SysParamDo;
import com.crm.system.mapper.SysAreaMapper;
import com.crm.system.mapper.ZqGrpGroupInfoMapper;
import com.crm.system.mapper.param.SysParamMapper;
import com.crm.system.service.TfFCustGroupDService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 集团实体客户全量日数据同步定时任务
 * 从SFTP读取客户集团全量日信息文件并导入到tf_f_cust_group_d表
 *
 * @author crm
 */
@Slf4j
@Component
public class TfFCustGroupDSyncJob {

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


    private final Map<String, String> cityMap = new HashMap<>();
    private volatile boolean cityListInitialized = false;

    private final Map<String, String> custStateMap = new HashMap<>();

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");


    /**
     * 集团实体客户全量日数据同步定时任务
     * 在xxl-job调度中心配置JobHandler为：tfFCustGroupDSyncJobHandler
     * 支持传入参数格式：
     * 1. 仅配置类型：configType: - 指定配置类型，使用当前日期 - 例：grp_group_info_syn
     * 2. 单日期参数：targetDate：yyyyMMdd - 指定日期 - 例：20260228
     * 3. 否入库zqzt_grp_group_info表：isInGrp：isInGrp - 指定是否入库 - 例：true
     * 参数样例：{"configType": "zqzt_cust_group_syn", "targetDate": "20260228", "isInGrp": "true"}
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
        String sftpConfigType = "grp_group_info_syn"; // 默认配置类型
        Date targetDate; // 默认T-2账期
        try {
            targetDate = new SimpleDateFormat("yyyy-MM-dd").parse(DateUtils.getBeforeNDays(2));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        boolean isInGrp = false; // 默认是否入库类型

        // 获取定时任务传入参数
        String jobStr = XxlJobHelper.getJobParam();
        if (StringUtils.isNotBlank(jobStr)) {
            JSONObject json = JSONUtil.parseObj(jobStr);

            // 指定配置类型
            String sftpConfigTypeStr = json.getStr("configType", "");
            if (StringUtils.isNotBlank(sftpConfigTypeStr)) {
                sftpConfigType = sftpConfigTypeStr;
                log.info("使用指定的配置类型: {}", sftpConfigType);
            }

            // 指定日期
            String targetDateStr = json.getStr("targetDate", "");
            if (StringUtils.isNotBlank(targetDateStr)) {
                try {
                    targetDate = simpleDateFormat.parse(targetDateStr);
                    log.info("成功解析日期参数: {}", simpleDateFormat.format(targetDate));
                } catch (ParseException e) {
                    log.warn("日期参数格式错误，使用当前日期。参数: {}, 错误: {}", targetDateStr, e.getMessage());
                    XxlJobHelper.log("日期参数格式错误，使用当前日期");
                }
            }

            // 指定是否入库类型
            String isInGrpStr = json.getStr("isInGrp", "");
            if (StringUtils.isNotBlank(isInGrpStr)) {
                isInGrp = isInGrpStr.equals("true");
                log.info("使用指定的否入库类型: {}", isInGrp);
            }
        }
        log.info("数据参数：1.配置类型：{}, 2.日期参数：{}, 3.是否入库：{}", sftpConfigType, simpleDateFormat.format(targetDate), isInGrp);


        try {
            // 生成目标文件路径：/opt/home/bi/dataout/ZQ/yyyyMMdd/DWD_D_CUS_ZQZT_CUST_GROUP_yyyyMMdd.txt
            SysParamDo grpCustGroupSyn = sysParamMapper.selectOne(new LambdaQueryWrapper<SysParamDo>()
                .eq(SysParamDo::getType, sftpConfigType)
                .eq(SysParamDo::getCode, "sftp.dataRootPath"));

            // 获取基础路径
            String basePath = grpCustGroupSyn.getValue(); // 例如 "/opt/home/bi/dataout/ZQ/"

            // 构建文件路径
            String filePath = buildCustGroupFilePath(basePath, targetDate);

            log.info("使用配置类型 '{}' 生成的文件路径: {}", sftpConfigType, filePath);


            // 1. 检查文件是否存在
            if (!sftpUtil.exists(sftpConfigType, filePath)) {
                String errorMsg = "集团实体客户全量日文件不存在: " + filePath;
                log.error(errorMsg);
                XxlJobHelper.handleFail(errorMsg);
                return;
            }

            // 2. 清空tf_f_cust_group_d表数据
            dwdDCusZqztCustGrouService.truncateTable();

            // 3. 使用批处理器处理大文件，每批处理100行数据以优化内存使用
            final int BATCH_SIZE = 500;
            log.info("开始使用批处理器处理大文件，每批处理 {} 行数据", BATCH_SIZE);
            XxlJobHelper.log("开始使用批处理器处理大文件，每批处理 {} 行数据", BATCH_SIZE);

            // 统计变量
            final int[] totalSuccess = {0};
            final int[] totalRows = {0};

            // 使用指定的SFTP配置类型和批处理器处理文件，配置更大的缓冲区
            boolean finalIsInGrp = isInGrp;
            sftpUtil.processFileInBatches(sftpConfigType, filePath, BATCH_SIZE, batchData -> {
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
                    List<TfFCustGroupDDo> tfFCustGroupDList = groupFileParser.parseFile(txtContent.toString(), TfFCustGroupDDo.class, "\\x1b");

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
                // 获取未存入zqzt_grp_group_info表数据
                List<TfFCustGroupDDo> tfFCustGroupDList = dwdDCusZqztCustGrouService.getTfFCustGroupDList(formatter.format(targetDate));
                // 处理zqzt_grp_group_info表的逻辑
                processGrpGroupInfo(tfFCustGroupDList);
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
        }
    }

    /**
     * 根据目标日期构建客户集团信息文件的完整路径
     *
     * @param basePath   基础路径，从系统参数中获取
     * @param targetDate 目标日期
     * @return 完整的文件路径
     */
    private String buildCustGroupFilePath(String basePath, Date targetDate) {
        // 创建Calendar实例并设置为目标日期
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(targetDate);

        // 获取减一个月后的日期
        Date previousMonthDate = calendar.getTime();

        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyyMMdd");
        String dateStr = fullDateFormat.format(previousMonthDate);

        return basePath + dateStr + "/DWD_D_CUS_ZQZT_CUST_GROUP_" + dateStr + ".txt";
    }

    private void initJobData() {
        for (SysArea sysArea : sysAreaMapper.selectList()) {
            if (StringUtils.isNotBlank(sysArea.getJtCode())) {
                cityMap.put(sysArea.getJtCode(), String.valueOf(sysArea.getId()));
            }
        }
        custStateMap.put("0", "03");
        custStateMap.put("1", "02");
        custStateMap.put("2", "05");
        custStateMap.put("3", "06");
        custStateMap.put("4", "04");
    }

    /**
     * 处理zqzt_grp_group_info表的逻辑
     * 获取全量未存入zqzt_grp_group_info表数据，并存入zqzt_grp_group_info表
     *
     * @param tfFCustGroupDList 客户集团全量日列表
     */
    private void processGrpGroupInfo(List<TfFCustGroupDDo> tfFCustGroupDList) {
        System.out.println("-----------------存入政企集团客户信息开始-----------------");
        XxlJobHelper.log("-----------------存入政企集团客户信息开始-----------------");

        List<ZqGrpGroupInfo> groupInfoList = new ArrayList<>();
        int batchSize = 500; // 每批处理数量
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
            newGroup.setGrpCreator(custGroup.getCreateOaUserCode());

            // 转换字符串日期为Date类型
            if (StringUtils.isNotBlank(custGroup.getCreateDate())) {
                try {
                    Date grpCreateTime = formatter.parse(custGroup.getCreateDate());
                    newGroup.setGrpCreateTime(grpCreateTime);
                } catch (ParseException e) {
                    log.info("日期转换失败，createDate:{}, error:{}", custGroup.getCreateDate(), e.getMessage());
                }
            }
            newGroup.setCreateTime(new Date());
            newGroup.setUpdateTime(new Date());

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
                        successBatchCount++;
                        totalSuccess += currentBatchSize; // 假设全部成功
                        log.info("批次处理完成，当前已处理 {} 条记录，成功导入 {} 条", totalProcessed, totalSuccess);
                        XxlJobHelper.log("批次处理完成，当前已处理 {} 条记录，成功导入 {} 条", totalProcessed, totalSuccess);
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

        System.out.println("-----------------存入政企集团客户信息结束-----------------");
        XxlJobHelper.log("-----------------存入政企集团客户信息结束-----------------");
    }

    private String generateUniqueGrpCode() {
        String grpCode;
        int maxAttempts = 10; // 最大重试次数
        int attempt = 0;

        do {
            // 生成15位随机数字
            grpCode = generateRandomGrpCode();
            attempt++;

            // 检查数据库中是否已存在
            if (attempt < maxAttempts) {
                LambdaQueryWrapper<ZqGrpGroupInfo> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ZqGrpGroupInfo::getGrpCode, grpCode);

                if (zqGrpGroupInfoMapper.selectCount(queryWrapper) > 0) {
                    grpCode = null; // 重复，需要重新生成
                }
            }
        } while (grpCode == null && attempt < maxAttempts);

        if (grpCode == null) {
            // 如果达到最大重试次数仍未找到唯一值，抛出异常或使用备用方案
            throw new RuntimeException("无法生成唯一的grpCode");
        }

        return grpCode;
    }

    private String generateRandomGrpCode() {
        Random random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
