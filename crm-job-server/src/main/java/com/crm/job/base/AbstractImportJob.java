package com.crm.job.base;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.enums.DateTypeEnum;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.StringUtils;
import com.crm.job.domain.TaskParams;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.dao.SysParamDo;
import com.crm.system.mapper.param.SysParamMapper;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


@Slf4j
public abstract class AbstractImportJob<T, R> {

    @Resource
    protected SftpUtil sftpUtil;

    @Resource
    protected SysParamMapper sysParamMapper;

    final String DATE_FORMAT_DAY = "yyyyMMdd";
    final String DATE_FORMAT_MONTH = "yyyyMM";

    /**
     * 执行导入任务
     */
    public void execute() {

        log.info("开始执行导入任务");

        try {
            // 1.初始化任务参数
            TaskParams params = parseTaskParams();

            // 获取定时任务传入参数
            String jsonStr = XxlJobHelper.getJobParam();

            // 配置任务参数
            if (StringUtils.isNotBlank(jsonStr)) {
                // xxl-job参数解析为JSON对象
                JSONObject json = JSONUtil.parseObj(jsonStr);

                // 配置指定账期路径
                String targetDate = json.getStr("targetDate","");
                if(StringUtils.isEmpty(params.getTargetDate())){
                    params.setTargetDate(targetDate);
                }

                // 配置指定文件类型后缀
                String suffixDate = json.getStr("suffixDate","");
                if(StringUtils.isEmpty(params.getSuffixDate())){
                    params.setSuffixDate(suffixDate);
                }

                // 配置文件保存路径，是否包含路径账期（默认包含）
                Boolean hasPathDate = json.getBool("hasPathDate", true);
                params.setHasPathDate(hasPathDate);

                // 配置文件名称，是否包含账期后缀（默认包含）
                Boolean hasSuffixDate = json.getBool("hasSuffixDate", true);
                params.setHasSuffixDate(hasSuffixDate);

                // 配置账期偏移量 （默认 0）
                Integer offsetDay = json.getInt("offsetDay",0);
                params.setOffsetDay(offsetDay);
            }

            // 根据偏移量生成账期字符串
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, - (params.getOffsetDay() == 0 ? 0 : params.getOffsetDay()));
            Date fileEtlCycle = calendar.getTime();



            // TargetDate为空 且需要账期目录：设置账期目录
            if(StringUtils.isEmpty(params.getTargetDate()) && params.isHasPathDate()){
                params.setTargetDate(DateTypeEnum.DAY.getCode().equals(params.getDateType())?
                    DateUtils.parseDateToStr(DATE_FORMAT_DAY, calendar.getTime()):
                    DateUtils.parseDateToStr(DATE_FORMAT_MONTH, calendar.getTime()));
            }
            // SuffixDate为空 且需要账期目录：设置账期后缀
            if(StringUtils.isEmpty(params.getSuffixDate()) && params.isHasSuffixDate()){
                params.setSuffixDate(DateTypeEnum.DAY.getCode().equals(params.getDateType())?
                    DateUtils.parseDateToStr(DATE_FORMAT_DAY, calendar.getTime()):
                    DateUtils.parseDateToStr(DATE_FORMAT_MONTH, calendar.getTime()));

                // 生成的文件账期后缀，作为 elt 账期保存
                params.setFileEtlCycle(fileEtlCycle);
            }

            // 2. 构建文件路径
            buildFilePath(params);

            // 3. 检查文件是否存在
            for (String filePath : params.getFilePathList()) {
                if (!sftpUtil.exists(params.getSftpConfigType(), filePath)) {
                    String errorMsg = "文件不存在: " + filePath;
                    log.error(errorMsg);
                    return;
                }
            }

            // 4. 清空目标表（可选）
            clearTargetTable();

            // 5. 批量处理文件
            processFileInBatches(params);

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
    private void buildFilePath(TaskParams params){
        String extension = StringUtils.isNotEmpty(params.getExtension()) ? "."+params.getExtension() : "";
        String suffixDate = StringUtils.isNotEmpty(params.getSuffixDate()) ? "_"+params.getSuffixDate() : "";
        String targetDate = StringUtils.isNotEmpty(params.getTargetDate()) ? params.getTargetDate() + "/" : "";
        SysParamDo config = sysParamMapper.selectOne(
            new LambdaQueryWrapper<SysParamDo>()
                .eq(SysParamDo::getType, params.getSftpConfigType())
                .eq(SysParamDo::getCode, "sftp.dataRootPath")
        );

        String basePath = config.getValue();
        params.setDataRootPath(basePath);
        List<String> fileNameList = params.getFileNameList();
        // 拼完整路径
        List<String> filePathList = new ArrayList<>();
        for (String fileName : fileNameList) {
            String filePath = basePath + targetDate + fileName + suffixDate + extension;
            filePathList.add(filePath);
        }
        params.setFilePathList(filePathList);
    }

    /**
     * 清空目标表
     */
    protected abstract void clearTargetTable();

    /**
     * 批量处理文件
     */
    protected abstract void processFileInBatches(TaskParams params);

    /**
     * 同步到目标表
     */
    protected abstract void syncToTargetTable();
}
