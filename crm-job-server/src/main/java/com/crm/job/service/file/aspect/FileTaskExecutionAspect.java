package com.crm.job.service.file.aspect;

import com.crm.system.domain.file.FileAsyncTaskDo;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 文件任务执行切面
 * 用于记录任务执行的详细信息，便于追踪和问题排查
 *
 * @author crm
 * @date 2025-11-24
 */
@Slf4j
@Aspect
@Component
public class FileTaskExecutionAspect {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 定义切点，拦截FileAsyncTaskJob中的processTask方法
     */
    @Pointcut("execution(boolean com.crm.job.service.file.FileAsyncTaskJob.processTask(com.crm.system.domain.file.FileAsyncTaskDo))")
    public void taskExecutionPointcut() {
    }

    /**
     * 环绕通知，记录任务执行的详细信息
     *
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 异常信息
     */
    @Around("taskExecutionPointcut()")
    public Object aroundTaskExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取参数
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0 || !(args[0] instanceof FileAsyncTaskDo)) {
            log.warn("无法获取任务信息，参数: {}", Arrays.toString(args));
            return joinPoint.proceed();
        }

        FileAsyncTaskDo task = (FileAsyncTaskDo) args[0];
        String taskId = String.valueOf(task.getTaskId());
        String businessType = task.getBusinessType();
        String taskType = task.getTaskType();

        // 记录任务开始执行
        String startTime = DATE_TIME_FORMATTER.format(LocalDateTime.now());
        log.info("[任务执行切面] 任务开始执行，任务ID: {}, 业务类型: {}, 任务类型: {}, 开始时间: {}",
                taskId, businessType, taskType, startTime);

        long startTimeMillis = System.currentTimeMillis();
        boolean result = false;

        try {
            // 执行原方法
            result = (boolean) joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            // 记录异常信息
            log.error("[任务执行切面] 任务执行异常，任务ID: {}, 异常: {}", taskId, e.getMessage(), e);
            throw e;
        } finally {
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTimeMillis;
            String finishTime = DATE_TIME_FORMATTER.format(LocalDateTime.now());

            // 记录任务执行结果
            log.info("[任务执行切面] 任务执行完成，任务ID: {}, 结果: {}, 状态: {}, 开始时间: {}, 结束时间: {}, 执行时间: {}ms({}s)",
                    taskId, result ? "成功" : "失败",
                    getStatusDesc(task.getStatus()),
                    startTime, finishTime,
                    executionTime,
                    String.format("%.3f", TimeUnit.MILLISECONDS.toSeconds(executionTime)));

            // 记录详细的任务指标
            if (task.getTotalRows() != null && task.getSuccessRows() != null && task.getFailRows() != null) {
                log.info("[任务执行切面] 任务处理指标，任务ID: {}, 总行数: {}, 成功行数: {}, 失败行数: {}",
                        taskId, task.getTotalRows(), task.getSuccessRows(), task.getFailRows());
            }
        }
    }

    /**
     * 获取任务状态的描述
     *
     * @param status 状态码
     * @return 状态描述
     */
    private String getStatusDesc(Long status) {
        if (status == null) {
            return "未知状态";
        }
        switch (status.intValue()) {
            case 0: return "待处理";
            case 1: return "处理中";
            case 2: return "处理成功";
            case 3: return "处理失败";
            case 4: return "已取消";
            default: return "未知状态";
        }
    }
}
