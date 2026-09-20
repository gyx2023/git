package com.crm.job.service.rpa;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.crm.common.config.AbilityProperties;
import com.crm.job.utils.HttpClientUtil;
import com.crm.system.domain.dao.rpa.RpaKpiStatisticsResultDo;
import com.crm.system.service.rpa.IRpaKpiStatisticsResultService;
import com.crm.system.service.sys.ISysParamService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * RPA KPI 数据加工任务。
 *
 * <p>在 XXL-Job 调度中心配置 JobHandler 为：rpaKpiDataJobHandler。</p>
 */
@Slf4j
@Component
public class RpaKpiDataJob {

    private static final String TASK_SOURCE = "C";
    private static final String PARAM_TYPE = "oppSyncGroupParam";
    private static final String URL_PARAM_CODE = "insertSystemKpiDataUrl";
    private static final DateTimeFormatter DATA_CYCLE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss SSS";

    @Resource
    private IRpaKpiStatisticsResultService rpaKpiStatisticsResultService;

    @Resource
    private ISysParamService sysParamService;

    @Resource
    private AbilityProperties abilityProperties;

    /**
     * 调用原存储过程加工昨日 RPA KPI 数据，加工成功后推送至智慧大脑。
     */
    @XxlJob("rpaKpiDataJobHandler")
    public void execute() {
        long taskStartTime = System.currentTimeMillis();
        String dataCycleD = LocalDate.now().minusDays(1).format(DATA_CYCLE_FORMATTER);
        logBoth("开始执行数据加工与推送任务，数据账期：{}", dataCycleD);

        try {
            // 调用存储过程加工数据
            long processStartTime = System.currentTimeMillis();
            logBoth("开始调用存储过程 rpa_result()");
            rpaKpiStatisticsResultService.callProcessData();
            logBoth("存储过程 rpa_result() 执行完成，耗时：{}ms", System.currentTimeMillis() - processStartTime);

            // 查询账期数据
            long queryStartTime = System.currentTimeMillis();
            List<RpaKpiStatisticsResultDo> kpiDataList = rpaKpiStatisticsResultService.selectByDataCycle(dataCycleD);
            logBoth("待推送数据查询完成，数据账期：{}，数据量：{}，耗时：{}ms", dataCycleD, kpiDataList.size(), System.currentTimeMillis() - queryStartTime);

            // 查询配置
            String pushUrl = getPushUrl();
            validateAbilityConfig();

            // 调用大脑接口推数据
            long pushStartTime = System.currentTimeMillis();
            logBoth("开始推送智慧大脑 KPI 数据，地址：{}，taskSource：{}，数据量：{}", pushUrl, TASK_SOURCE, kpiDataList.size());
            pushKpiData(pushUrl, kpiDataList);
            logBoth("智慧大脑 KPI 数据推送成功，数据账期：{}，数据量：{}，耗时：{}ms", dataCycleD, kpiDataList.size(), System.currentTimeMillis() - pushStartTime);

            long costTime = System.currentTimeMillis() - taskStartTime;
            String successMessage = String.format("RPA KPI 数据加工与推送完成，数据账期：%s，数据量：%d，总耗时：%dms",dataCycleD, kpiDataList.size(), costTime);
            logBoth(successMessage);
            XxlJobHelper.handleSuccess(successMessage);
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - taskStartTime;
            String errorMessage = String.format(
                "RPA KPI 数据加工或推送失败，数据账期：%s，总耗时：%dms，异常：%s",
                dataCycleD, costTime, e.getMessage());
            log.error("[RPA KPI] {}", errorMessage, e);
            XxlJobHelper.log("[RPA KPI] {}", errorMessage);
            XxlJobHelper.handleFail(errorMessage);
        }
    }

    private String getPushUrl() {
        Map<String, String> params = sysParamService.findValueMapsByType(PARAM_TYPE);
        String pushUrl = params.get(URL_PARAM_CODE);
        if (StrUtil.isBlank(pushUrl)) {
            throw new IllegalStateException("sys_param 未配置智慧大脑 KPI 推送地址：type=" + PARAM_TYPE + "，code=" + URL_PARAM_CODE);
        }
        return pushUrl;
    }

    private void validateAbilityConfig() {
        if (StrUtil.isBlank(abilityProperties.getAppId())) {
            throw new IllegalStateException("当前系统 ability.appId 未配置");
        }
        if (StrUtil.isBlank(abilityProperties.getAppSecret())) {
            throw new IllegalStateException("当前系统 ability.appSecret 未配置");
        }
    }

    private void pushKpiData(String pushUrl, List<RpaKpiStatisticsResultDo> kpiDataList) throws Exception {
        String requestJson = buildRequestJson(kpiDataList);
        String responseBody = HttpClientUtil.doPost(pushUrl, requestJson, "UTF-8");
        if (StrUtil.isBlank(responseBody)) {
            throw new IllegalStateException("智慧大脑 KPI 推送接口返回为空");
        }

        JSONObject responseJson = JSON.parseObject(responseBody);
        JSONObject uniBssBody = responseJson.getJSONObject("UNI_BSS_BODY");
        if (uniBssBody == null) {
            throw new IllegalStateException("接口响应缺少 UNI_BSS_BODY");
        }

        JSONObject kpiResponse = uniBssBody.getJSONObject("INSERT_SYSTEM_KPI_DATA_RSP");
        if (kpiResponse == null) {
            throw new IllegalStateException("接口响应缺少 INSERT_SYSTEM_KPI_DATA_RSP");
        }

        Integer code = kpiResponse.getInteger("code");
        if (!Integer.valueOf(200).equals(code)) {
            String message = StrUtil.blankToDefault(kpiResponse.getString("msg"),
                kpiResponse.getString("message"));
            throw new IllegalStateException(
                "智慧大脑 KPI 推送业务失败，code=" + code +
                    "，message=" + StrUtil.maxLength(message, 500));
        }
        logBoth("智慧大脑 KPI 推送接口返回成功，code：{}", code);
    }

    private String buildRequestJson(List<RpaKpiStatisticsResultDo> kpiDataList) {
        Date now = new Date();
        String timestamp = new SimpleDateFormat(TIMESTAMP_PATTERN).format(now);
        String transId = timestamp + RandomUtil.randomNumbers(6);
        String tokenDesc = "APP_ID" + abilityProperties.getAppId()
            + "TIMESTAMP" + timestamp
            + "TRANS_ID" + transId
            + abilityProperties.getAppSecret();

        JSONObject head = new JSONObject();
        head.put("APP_ID", abilityProperties.getAppId());
        head.put("TIMESTAMP", timestamp);
        head.put("TRANS_ID", transId);
        head.put("TOKEN", SecureUtil.md5(tokenDesc));

        JSONObject dataRequest = new JSONObject();
        dataRequest.put("taskSource", TASK_SOURCE);
        dataRequest.put("kpiDataList", kpiDataList);

        JSONObject body = new JSONObject();
        body.put("INSERT_SYSTEM_KPI_DATA_REQ", dataRequest);

        JSONObject request = new JSONObject();
        request.put("UNI_BSS_BODY", body);
        request.put("UNI_BSS_HEAD", head);
        return JSON.toJSONString(request);
    }

    private void logBoth(String format, Object... arguments) {
        log.info("[RPA KPI] " + format, arguments);
        XxlJobHelper.log("[RPA KPI] " + format, arguments);
    }
}
