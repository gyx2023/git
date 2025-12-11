package com.crm.job.service.grp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.crm.job.utils.GroupFileParser;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.SysArea;
import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.grp.GrpGroupInfoDO;
import com.crm.system.domain.grp.TfFCustGroupMDO;
import com.crm.system.domain.param.SysParamDO;
import com.crm.system.mapper.SysAreaMapper;
import com.crm.system.mapper.ZqGrpGroupInfoMapper;
import com.crm.system.mapper.grp.GrpGroupInfoMapper;
import com.crm.system.mapper.param.SysParamMapper;
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
 * 客户集团信息同步定时任务
 * 从SFTP读取客户集团信息文件并导入到tf_f_cust_group_m表
 *
 * @author crm
 */
@Slf4j
@Component
public class GrpCustGroupSyncJob {

    @Resource
    private SftpUtil sftpUtil;

    @Resource
    private GroupFileParser groupFileParser;

    @Resource
    private SysParamMapper sysParamMapper;

    @Resource
    private TfFCustGroupMService tfFCustGroupMService;

    @Resource
    private ZqGrpGroupInfoMapper zqGrpGroupInfoMapper;

    @Resource
    private SysAreaMapper sysAreaMapper;


    private final Map<String,String> cityMap = new HashMap<>();
    private volatile boolean cityListInitialized = false;

    private final Map<String,String> custStateMap = new HashMap<>();

    /**
     * 客户集团信息同步任务
     * 在xxl-job调度中心配置JobHandler为：grpCustGroupSyncJobHandler
     * 支持传入参数格式：
     * 1. 单日期参数：YYYYMMDD - 仅指定日期
     * 2. 配置类型和日期：configType:YYYYMMDD - 指定配置类型和日期
     * 3. 仅配置类型：configType: - 仅指定配置类型，使用当前日期
     */
    @XxlJob("grpCustGroupSyncJobHandler")
    public void execute() {
        log.info("开始执行客户集团信息同步任务");
        XxlJobHelper.log("开始执行客户集团信息同步任务");

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
        String param = XxlJobHelper.getJobParam();
        Date targetDate = new Date();
        String sftpConfigType = "grp_group_info_syn"; // 默认配置类型
        // 处理任务参数
        if (param != null && !param.isEmpty()) {
            log.info("任务参数: {}", param);
            XxlJobHelper.log("任务参数: {}", param);

            // 检查是否包含配置类型前缀（格式：configType:YYYYMMDD 或 configType:）
            if (param.contains(":")) {
                String[] parts = param.split(":", 2);
                sftpConfigType = parts[0]; // 获取配置类型
                log.info("使用指定的配置类型: {}", sftpConfigType);

                // 如果冒号后面有内容，则尝试解析为日期
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                        targetDate = dateFormat.parse(parts[1]);
                        log.info("成功解析日期参数: {}", dateFormat.format(targetDate));
                    } catch (ParseException e) {
                        log.warn("日期参数格式错误，使用当前日期。参数: {}, 错误: {}", parts[1], e.getMessage());
                        XxlJobHelper.log("日期参数格式错误，使用当前日期");
                    }
                }
            } else {
                // 仅日期参数，尝试解析为日期
                try {
                    // 尝试解析日期参数（格式：YYYYMMDD）
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                    targetDate = dateFormat.parse(param);
                    log.info("成功解析日期参数: {}", dateFormat.format(targetDate));
                } catch (ParseException e) {
                    log.warn("日期参数格式错误，使用当前日期。参数: {}, 错误: {}", param, e.getMessage());
                    XxlJobHelper.log("日期参数格式错误，使用当前日期");
                }
            }
        }

        try {
            // 生成目标文件路径：/opt/home/bi/dataout/ZQ/YYYY/MM/DD/GRP_CUST_GROUP_YYYYMMDD.txt
            SysParamDO grpCustGroupSyn = sysParamMapper.selectOne(new LambdaQueryWrapper<SysParamDO>()
                .eq(SysParamDO::getType, "grp_group_info_syn")
                .eq(SysParamDO::getCode, "sftp.dataRootPath"));

            // 获取基础路径
            String basePath = grpCustGroupSyn.getValue(); // 例如 "/opt/home/bi/dataout/ZQ/"

            // 构建文件路径
            String filePath = buildCustGroupFilePath(basePath, targetDate);

            log.info("使用配置类型 '{}' 生成的文件路径: {}", sftpConfigType, filePath);

            // 1. 检查文件是否存在
            if (!sftpUtil.exists(sftpConfigType, filePath)) {
                String errorMsg = "客户集团信息文件不存在: " + filePath;
                log.error(errorMsg);
                XxlJobHelper.handleFail(errorMsg);
                return;
            }

            // 2. 清空tf_f_cust_group_m表数据
            tfFCustGroupMService.truncateTable();

            // 3. 使用批处理器处理大文件，每批处理100行数据以优化内存使用
            final int BATCH_SIZE = 500;
            log.info("开始使用批处理器处理大文件，每批处理 {} 行数据", BATCH_SIZE);
            XxlJobHelper.log("开始使用批处理器处理大文件，每批处理 {} 行数据", BATCH_SIZE);

            // 统计变量
            final int[] totalSuccess = {0};
            final int[] totalRows = {0};

            // 使用指定的SFTP配置类型和批处理器处理文件，配置更大的缓冲区
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
                    List<TfFCustGroupMDO> custGroupList = groupFileParser.parseFile(txtContent.toString(), TfFCustGroupMDO.class, "\\x1b");

                    if (!custGroupList.isEmpty()) {
                        // 批量插入数据库
                        int success = tfFCustGroupMService.batchInsert(custGroupList);
                        totalSuccess[0] += success;

                        log.info("批次处理完成，当前已处理 {} 条记录，成功导入 {} 条",
                            totalRows[0], totalSuccess[0]);
                        XxlJobHelper.log("批次处理完成，当前已处理 {} 条记录，成功导入 {} 条",
                            totalRows[0], totalSuccess[0]);

                        // 处理zqzt_grp_group_info表的逻辑
                        processGrpGroupInfo(custGroupList);
                    }

                    return true; // 继续处理下一批数据
                } catch (Exception e) {
                    log.error("批次处理失败: {}", e.getMessage(), e);
                    XxlJobHelper.log("批次处理失败: {}", e.getMessage());
                    // 发生异常时的处理
                    return true; // 可以根据业务需求决定是否继续处理，这里选择继续
                }
            });

            // 验证是否有数据被处理
            if (totalRows[0] == 0) {
                log.warn("文件中没有有效数据需要导入");
                XxlJobHelper.handleSuccess("文件中没有有效数据需要导入");
                return;
            }

            // 4. 任务完成，记录结果
            String successMsg = String.format("客户集团信息同步任务执行完成，成功导入: %d 条，失败: %d 条",
                totalSuccess[0], totalRows[0] - totalSuccess[0]);
            log.info(successMsg);
            XxlJobHelper.handleSuccess(successMsg);

        } catch (Exception e) {
            // 5. 处理异常
            String errorMsg = "客户集团信息同步任务执行失败: " + e.getMessage();
            log.error(errorMsg, e);
            XxlJobHelper.log("任务执行失败: {}", e.getMessage());
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    /**
     * 处理zqzt_grp_group_info表的逻辑
     * 根据GROUP_ID查询是否有数据，如果没有数据则插入
     *
     * @param custGroupList 客户集团列表
     */
    private void processGrpGroupInfo(List<TfFCustGroupMDO> custGroupList) {
        List<ZqGrpGroupInfo> groupInfoList = new ArrayList<>();
        for (TfFCustGroupMDO custGroup : custGroupList) {
            String groupId = custGroup.getGroupId(); // 假设TfFCustGroupMDO有getGroupId方法

            // 查询zqzt_grp_group_info表是否有该GROUP_ID的数据
            ZqGrpGroupInfo zqGrpGroupInfo = zqGrpGroupInfoMapper.selectOne(new LambdaQueryWrapper<ZqGrpGroupInfo>().eq(ZqGrpGroupInfo::getHqGrpCode, custGroup.getGroupId()));
            if(zqGrpGroupInfo==null){
                ZqGrpGroupInfo newGroup = new ZqGrpGroupInfo();
                newGroup.setGrpName(custGroup.getContactName());
                newGroup.setGrpCode(generateUniqueGrpCode());
                newGroup.setHqGrpCode(custGroup.getCertiCode());
                newGroup.setCustId(custGroup.getProvinceCustId());
                newGroup.setGrpStatus(custStateMap.get(custGroup.getCustState()));
                newGroup.setAreaId(cityMap.get(custGroup.getEparchyCode()));
                newGroup.setCountyId(cityMap.get(custGroup.getBCityCode()));
                newGroup.setIndustryMajorKind(custGroup.getFirstCallingTypeCode());
                newGroup.setIndustrySubKind(custGroup.getSecondCallingTypeCode());
                newGroup.setIndustryDetailKind(custGroup.getThirdCallingTypeCode());
                newGroup.setGrpCustType(custGroup.getCustClassType());
                //newGroup.setGrpCreateTime(custGroup.getCreateDate());
                newGroup.setCreateTime(new Date());
                newGroup.setUpdateTime(new Date());
                groupInfoList.add(newGroup);
            }
        }
        zqGrpGroupInfoMapper.insertBatch(groupInfoList);
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

        // 减去一个月
        calendar.add(Calendar.MONTH, -1);

        // 获取减一个月后的日期
        Date previousMonthDate = calendar.getTime();

        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyyMM");
        String dateStr = fullDateFormat.format(previousMonthDate);

        return basePath + dateStr + "/DWD_M_CUST_ZQZT_CUST_GROUP_" + dateStr + ".txt";
    }

    private void initJobData(){
        for (SysArea sysArea : sysAreaMapper.selectList()) {
            if(StringUtils.isNotBlank(sysArea.getJtCode())){
                cityMap.put(sysArea.getJtCode(),String.valueOf(sysArea.getId()));
            }
        }
        custStateMap.put("0","03");
        custStateMap.put("1","02");
        custStateMap.put("2","05");
        custStateMap.put("3","06");
        custStateMap.put("4","04");
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
