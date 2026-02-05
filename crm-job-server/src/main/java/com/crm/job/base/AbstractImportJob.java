package com.crm.job.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.enums.DateTypeEnum;
import com.crm.common.utils.StringUtils;
import com.crm.domain.TaskParams;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.param.SysParamDo;
import com.crm.system.mapper.param.SysParamMapper;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Slf4j
public abstract class AbstractImportJob<T, R> {

    @Resource
    protected SftpUtil sftpUtil;

    @Resource
    protected SysParamMapper sysParamMapper;

    /**
     * 执行导入任务
     */
    public void execute() {
        log.info("开始执行导入任务");
        //获取定时任务传入参数
        String date = XxlJobHelper.getJobParam();

        try {
            // 1. 解析任务参数
            TaskParams params = parseTaskParams();
            if(StringUtils.isEmpty(params.getTargetDate()) && date == null){
                params.setTargetDate(date);
            }
            if(StringUtils.isEmpty(params.getTargetDate())){
                params.setTargetDate(DateTypeEnum.DAY.getCode().equals(params.getDateType())?
                    new SimpleDateFormat("yyyyMMdd").format(new Date()):new SimpleDateFormat("yyyyMM").format(new Date()));
            }

            // 2. 构建文件路径
            String filePath = buildFilePath(params);

            // 3. 检查文件是否存在
            if (!sftpUtil.exists(params.getSftpConfigType(), filePath)) {
                String errorMsg = "文件不存在: " + filePath;
                log.error(errorMsg);
                return;
            }

            // 4. 清空目标表（可选）
            clearTargetTable();

            // 5. 批量处理文件
            processFileInBatches(filePath, params.getBatchSize());

            // 6. 同步到目标表（可选）
            syncToTargetTable();

            // 7. 任务完成
            String successMsg = "导入任务执行完成";
            log.info(successMsg);

        } catch (Exception e) {
            String errorMsg = "导入任务执行失败: " + e.getMessage();
            log.error(errorMsg, e);
        }
    }

    /**
     * 解析任务参数
     */
    protected abstract TaskParams parseTaskParams();

    /**
     * 构建文件路径
     */
    private String buildFilePath(TaskParams params){
        SysParamDo config = sysParamMapper.selectOne(
            new LambdaQueryWrapper<SysParamDo>()
                .eq(SysParamDo::getType, params.getSftpConfigType())
                .eq(SysParamDo::getCode, "ftp_directory")
        );
        String basePath = config.getValue();
        return basePath + params.getTargetDate() + params.getFileName() + "_" + params.getTargetDate() + ".txt";
    }

    /**
     * 清空目标表
     */
    protected abstract void clearTargetTable();

    /**
     * 批量处理文件
     */
    protected abstract void processFileInBatches(String filePath, int batchSize);

    /**
     * 同步到目标表
     */
    protected abstract void syncToTargetTable();
}
