package com.crm.job.service.rpa;

import cn.hutool.json.JSONUtil;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.service.servicehall.INengKaiCallbackService;
import com.crm.system.domain.ZqztRpaBusinessRetryConfigDo;
import com.crm.system.domain.ZqztRpaBusinessTaskRetryDo;
import com.crm.system.mapper.servicehall.ZqztJtNoticeRecordMapper;
import com.crm.system.mapper.ZqztRpaBusinessTaskRetryMapper;
import com.crm.system.service.IZqztRpaBusinessRetryService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * RPA业务重试定时任务
 * 在xxl-job调度中心配置JobHandler为：rpaBusinessRetryJobHandler
 *
 * @author crm
 */
@Slf4j
@Component
public class RpaBusinessRetryJob {

    private static final int DEFAULT_RETRY_INTERVAL_SECONDS = 300;
    private static final int DEFAULT_MAX_RETRY_COUNT = 999;

    @Resource
    private ZqztRpaBusinessTaskRetryMapper taskRetryMapper;

    @Resource
    private ZqztJtNoticeRecordMapper noticeRecordMapper;

    @Resource
    private IZqztRpaBusinessRetryService retryService;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private INengKaiCallbackService nengKaiCallbackService;

    /**
     * RPA业务重试任务
     * 查询待重试的任务，调用admin API进行同步，根据结果更新状态
     */
    @XxlJob("rpaBusinessRetryJobHandler")
    public void execute() {
        log.info("[RPA重试] 开始执行重试任务");
        XxlJobHelper.log("[RPA重试] 开始执行重试任务");

        Date now = new Date();
        List<ZqztRpaBusinessTaskRetryDo> pendingTasks = taskRetryMapper.selectPendingRetryTasks(now);

        if (pendingTasks == null || pendingTasks.isEmpty()) {
            log.info("[RPA重试] 无待处理的重试任务");
            XxlJobHelper.log("无待处理的重试任务");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (ZqztRpaBusinessTaskRetryDo task : pendingTasks) {
            try {
                String bizCode = task.getBizCode();
                Integer currentRetryCount = task.getRetryCount() != null ? task.getRetryCount() : 0;

                // 获取重试配置
                ZqztRpaBusinessRetryConfigDo config = retryService.getConfig(bizCode);
                Integer maxRetries = config.getMaxRetryCount() != null ? config.getMaxRetryCount() : DEFAULT_MAX_RETRY_COUNT;

                // 检查是否超过最大重试次数
                if (currentRetryCount >= maxRetries) {
                    taskRetryMapper.updateTaskStatus(task.getId(), 3, currentRetryCount, "超过最大重试次数", null);
                    failCount++;
                    continue;
                }

                // HTTP 重试
                Boolean syncResult = executeHttpRetry(task);

                if (Boolean.TRUE.equals(syncResult)) {
                    // 重试成功：调用能开回调 + 标记完成
                    notifyNengKai(task);
                    taskRetryMapper.updateTaskStatus(task.getId(), 2, currentRetryCount + 1, null, null);
                    successCount++;
                    XxlJobHelper.log("重试成功: retryId={}, bizCode={}", task.getRetryId(), bizCode);
                } else {
                    int nextRetryCount = currentRetryCount + 1;
                    long interval = calculateRetryInterval(config, nextRetryCount);
                    Date nextRetryTime = new Date(System.currentTimeMillis() + interval * 1000L);
                    taskRetryMapper.updateTaskStatus(
                        task.getId(), 1, nextRetryCount, "重试" + nextRetryCount + "次失败", nextRetryTime);
                    failCount++;
                }
            } catch (Exception e) {
                log.error("[RPA重试] 重试异常: id={}, error={}", task.getId(), e.getMessage(), e);
                int nextRetryCount = (task.getRetryCount() != null ? task.getRetryCount() : 0) + 1;
                Date nextRetryTime = new Date(System.currentTimeMillis() + DEFAULT_RETRY_INTERVAL_SECONDS * 1000L);
                taskRetryMapper.updateTaskStatus(task.getId(), 1, nextRetryCount, e.getMessage(), nextRetryTime);
                failCount++;
            }
        }

        log.info("[RPA重试] 重试任务完成: 成功{}, 失败{}", successCount, failCount);
        XxlJobHelper.log("重试任务完成: 成功{}条, 失败{}条", successCount, failCount);
    }

    /**
     * HTTP 重试：从任务记录中取目标地址和请求参数，执行重试
     */
    private Boolean executeHttpRetry(ZqztRpaBusinessTaskRetryDo task) {
        String messageBody = task.getMessageBody();
        if (StringUtils.isBlank(messageBody)) {
            log.warn("[RPA重试] 消息体为空: retryId={}", task.getRetryId());
            return false;
        }

        Map<String, Object> syncData;
        try {
            syncData = JSONUtil.toBean(messageBody, Map.class);
        } catch (Exception e) {
            log.error("[RPA重试] 解析 messageBody 失败: retryId={}, error={}", task.getRetryId(), e.getMessage());
            throw e;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(parseMediaType(task.getContentType()));

            ResponseEntity<String> response = restTemplate.exchange(
                task.getTargetUrl(), parseHttpMethod(task.getHttpMethod()), new HttpEntity<>(syncData, headers), String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                cn.hutool.json.JSONObject resp = JSONUtil.parseObj(response.getBody());
                String code = String.valueOf(resp.get("code"));
                if ("200".equals(code) || "0".equals(code)) {
                    log.info("[RPA重试] 调用成功: retryId={}, bizCode={}", task.getRetryId(), task.getBizCode());
                    return true;
                }
            }

            log.warn("[RPA重试] 调用失败: retryId={}, url={}, response={}", task.getRetryId(), task.getTargetUrl(), response.getBody());
            return false;
        } catch (Exception e) {
            log.error("[RPA重试] admin调用异常: retryId={}, error={}", task.getRetryId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 重试成功后通知能开（通过noticeRecordId查原始消息体，调能开回调）
     */
    private void notifyNengKai(ZqztRpaBusinessTaskRetryDo task) {
        try {
            Long noticeId = task.getNoticeRecordId();
            if (noticeId == null) {
                log.warn("[RPA重试] 未关联notice_record_id: retryId={}", task.getRetryId());
                return;
            }
            ZqztJtNoticeRecordDo recordDo = noticeRecordMapper.selectById(noticeId);
            if (recordDo == null) {
                log.warn("[RPA重试] 未找到关联的notice_record: id={}", noticeId);
                return;
            }

            nengKaiCallbackService.callWorksheetsComplete(recordDo, "RPA-YGCF");
            log.info("[RPA重试] 能开回调成功: retryId={}, noticeId={}", task.getRetryId(), noticeId);
        } catch (Exception e) {
            log.error("[RPA重试] 能开回调失败: retryId={}, error={}", task.getRetryId(), e.getMessage(), e);
        }
    }

    private HttpMethod parseHttpMethod(String method) {
        if (method == null) return HttpMethod.POST;
        switch (method.toUpperCase()) {
            case "PUT":   return HttpMethod.PUT;
            case "GET":   return HttpMethod.GET;
            case "DELETE": return HttpMethod.DELETE;
            default:      return HttpMethod.POST;
        }
    }

    private MediaType parseMediaType(String contentType) {
        if (contentType == null) return MediaType.APPLICATION_JSON;
        return MediaType.parseMediaType(contentType);
    }

    long calculateRetryInterval(ZqztRpaBusinessRetryConfigDo config, int retryCount) {
        int baseInterval = config.getRetryIntervalSeconds() != null ? config.getRetryIntervalSeconds() : DEFAULT_RETRY_INTERVAL_SECONDS;
        int exponent = Math.min(retryCount - 1, 6);
        return (long) baseInterval * (1L << exponent);
    }
}
