package com.crm.job.service.grp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.crm.job.utils.GroupFileParser;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.SysArea;
import com.crm.system.domain.grp.GrpGroupInfoDo;
import com.crm.system.domain.param.SysParamDo;
import com.crm.system.mapper.SysAreaMapper;
import com.crm.system.mapper.ZqGrpGroupInfoMapper;
import com.crm.system.mapper.param.SysParamMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 集团信息同步定时任务
 * 从SFTP读取集团信息文件并导入到grp_group_info表
 *
 * @author crm
 */
@Slf4j
@Component
public class GrpGroupInfoSyncJob {

    @Resource
    private SftpUtil sftpUtil;

    @Resource
    private GroupFileParser groupFileParser;

    @Resource
    private GrpGroupInfoService grpGroupInfoService;

    @Resource
    private SysParamMapper sysParamMapper;

    @Resource
    private SysAreaMapper sysAreaMapper;

    @Resource
    private ZqGrpGroupInfoMapper zqGrpGroupInfoMapper;

    private final Map<String,String> cityMap = new HashMap<>();
    private volatile boolean cityListInitialized = false;

    /**
     * 集团信息同步任务
     * 在xxl-job调度中心配置JobHandler为：grpGroupInfoSyncJobHandler
     * 支持传入参数格式：
     * 1. 单日期参数：YYYYMMDD - 仅指定日期
     * 2. 配置类型和日期：configType:YYYYMMDD - 指定配置类型和日期
     * 3. 仅配置类型：configType: - 仅指定配置类型，使用当前日期
     */
    @XxlJob("grpGroupInfoSyncJobHandler")
    public void execute() {
        log.info("开始执行集团信息同步任务");
        XxlJobHelper.log("开始执行集团信息同步任务");

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
            // 生成目标文件路径：/opt/home/bi/dataout/ZQ/YYYY/MM/DD/GRP_GROUP_INFO_YYYYMMDD.txt
            SysParamDo grpGroupInfoSyn = sysParamMapper.selectOne(new LambdaQueryWrapper<SysParamDo>()
                .eq(SysParamDo::getType, "grp_group_info_syn")
                .eq(SysParamDo::getCode, "sftp.dataRootPath"));

            // 获取基础路径
            String basePath = grpGroupInfoSyn.getValue(); // 例如 "/opt/home/bi/dataout/ZQ/"


            // 构建文件路径
            String filePath = buildGroupInfoFilePath(basePath, targetDate);


            log.info("使用配置类型 '{}' 生成的文件路径: {}", sftpConfigType, filePath);

            // 1. 检查文件是否存在
            if (!sftpUtil.exists(sftpConfigType, filePath)) {
                String errorMsg = "集团信息文件不存在: " + filePath;
                log.error(errorMsg);
                XxlJobHelper.handleFail(errorMsg);
                return;
            }

            // 2. 清空表数据（可选，根据业务需求决定）
            grpGroupInfoService.truncateTable();

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

                    // 解析文本数据
                    List<GrpGroupInfoDo> groupInfoList = groupFileParser.parseFile(txtContent.toString(), GrpGroupInfoDo.class, "\\x1b");

                    if (!groupInfoList.isEmpty()) {
                        groupInfoList.forEach(p->{
                            if(StringUtils.isNotBlank(p.getAreaid())){
                                p.setAreaid(cityMap.get(p.getAreaid())==null?p.getAreaid():cityMap.get(p.getAreaid()));
                            }
                            if(StringUtils.isNotBlank(p.getCountyid())){
                                p.setCountyid(cityMap.get(p.getCountyid())==null?p.getCountyid():cityMap.get(p.getCountyid()));
                            }
                            if(StringUtils.isNotBlank(p.getCountyid2())){
                                p.setCountyid2(cityMap.get(p.getCountyid2())==null?p.getCountyid2():cityMap.get(p.getCountyid2()));
                            }
                        });
                        // 批量插入数据库
                        int success = grpGroupInfoService.batchUpsert(groupInfoList);
                        totalSuccess[0] += success;

                        log.info("批次处理完成，当前已处理 {} 条记录，成功导入 {} 条",
                            totalRows[0], totalSuccess[0]);
                        XxlJobHelper.log("批次处理完成，当前已处理 {} 条记录，成功导入 {} 条",
                            totalRows[0], totalSuccess[0]);
                    }

                    // 显式清空中间变量，释放内存
                    groupInfoList.clear();
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
            //通过开关控制是否要插入
            syncToZqGrpGroupInfo();

            // 4. 任务完成，记录结果
            String successMsg = String.format("集团信息同步任务执行完成，成功导入: %d 条，失败: %d 条",
                totalSuccess[0], totalRows[0] - totalSuccess[0]);
            log.info(successMsg);
            XxlJobHelper.handleSuccess(successMsg);

        } catch (Exception e) {
            // 7. 处理异常
            String errorMsg = "集团信息同步任务执行失败: " + e.getMessage();
            log.error(errorMsg, e);
            XxlJobHelper.log("任务执行失败: {}", e.getMessage());
            XxlJobHelper.handleFail(errorMsg);
        }
    }


    private void initJobData(){
        for (SysArea sysArea : sysAreaMapper.selectList()) {
            if(StringUtils.isNotBlank(sysArea.getBssCode())){
                cityMap.put(sysArea.getBssCode(),String.valueOf(sysArea.getId()));
            }
        }
    }

    /**
     * 根据目标日期构建集团信息文件的完整路径
     *
     * @param basePath   基础路径，从系统参数中获取
     * @param targetDate 目标日期
     * @return 完整的文件路径
     */
    private String buildGroupInfoFilePath(String basePath, Date targetDate) {
        // 创建Calendar实例并设置为目标日期
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(targetDate);

        // 减去一天得到昨天的日期
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        // 获取昨天的日期
        Date yesterday = calendar.getTime();

        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyyMMdd");
        String dateStr = fullDateFormat.format(yesterday);

        return basePath + dateStr + "/GRP_GROUP_INFO_" + dateStr + ".txt";
    }

    /**
     * 同步数据到zqzt_grp_group_info表
     * 根据sys_param表中的开关配置决定是否同步
     */
    private void syncToZqGrpGroupInfo() {
        try {
            // 查询同步开关配置
            SysParamDO syncSwitchParam = sysParamMapper.selectOne(
                new LambdaQueryWrapper<SysParamDO>()
                    .eq(SysParamDO::getType, "insert_grp_group_info")
                    .eq(SysParamDO::getCode, "zqzt.switch")
            );

            // 检查开关是否为开启状态（值为"1"或"true"表示开启）
            if (syncSwitchParam == null || syncSwitchParam.getValue() == null) {
                log.info("未配置同步到zqzt_grp_group_info的开关，跳过同步");
                XxlJobHelper.log("未配置同步到zqzt_grp_group_info的开关，跳过同步");
                return;
            }

            String switchValue = syncSwitchParam.getValue().trim();
            if (!"1".equals(switchValue) && !"true".equalsIgnoreCase(switchValue)) {
                log.info("同步到zqzt_grp_group_info的开关已关闭，跳过同步。当前开关值: {}", switchValue);
                XxlJobHelper.log("同步到zqzt_grp_group_info的开关已关闭，跳过同步。当前开关值: {}", switchValue);
                return;
            }

            log.info("开始同步数据到zqzt_grp_group_info表");
            XxlJobHelper.log("开始同步数据到zqzt_grp_group_info表");

            // 1. 全表删除zqzt_grp_group_info表数据
            int deleteCount = zqGrpGroupInfoMapper.truncateZqGrpGroupInfo();
            log.info("清空zqzt_grp_group_info表完成，删除记录数: {}", deleteCount);
            XxlJobHelper.log("清空zqzt_grp_group_info表完成，删除记录数: {}", deleteCount);

            // 2. 从grp_group_info同步数据
            int syncCount = zqGrpGroupInfoMapper.syncFromGrpGroupInfo();
            log.info("同步数据到zqzt_grp_group_info表完成，同步记录数: {}", syncCount);
            XxlJobHelper.log("同步数据到zqzt_grp_group_info表完成，同步记录数: {}", syncCount);

        } catch (Exception e) {
            log.error("同步数据到zqzt_grp_group_info表失败: {}", e.getMessage(), e);
            XxlJobHelper.log("同步数据到zqzt_grp_group_info表失败: {}", e.getMessage());
            // 注意：这里不抛出异常，避免影响主任务的成功状态
        }
    }
}
