package com.crm.job.service.grp;

import com.crm.job.utils.GroupFileParser;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.grp.GrpGroupInfoDO;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 集团信息同步定时任务
 * 从SFTP读取集团信息文件并导入到grp_group_info表
 *
 * @author crm
 */
@Slf4j
@Component
public class GrpGroupInfoSyncJob {

    @Autowired
    private SftpUtil sftpUtil;

    @Autowired
    private GroupFileParser groupFileParser;

    @Autowired
    private GrpGroupInfoService grpGroupInfoService;

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

        // 任务参数
        String param = XxlJobHelper.getJobParam();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date targetDate = calendar.getTime();
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
                // 生成目标文件路径：/opt/home/bi/dataout/ZQ/YYYYMMDD/GRP_GROUP_INFO_YYYYMMDD.txt
                // 使用指定的SFTP配置类型生成路径
                String filePath = sftpUtil.generateFullFilePath(sftpConfigType, targetDate);
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

            // 为大文件设置更大的缓冲区（64KB）以提高读取性能
            final int BUFFER_SIZE = 65536;

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
                        List<GrpGroupInfoDO> groupInfoList = groupFileParser.parseCsvFile(txtContent.toString());

                        if (!groupInfoList.isEmpty()) {
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
}
