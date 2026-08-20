package com.crm.job.service;

import com.crm.system.client.unibss.UniBssApiClient;
import com.crm.system.client.unibss.UniBssApiClient.UniBssResponse;
import com.crm.system.service.sys.ISysParamService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * UniBSS API 连通性检查 + 签名验证
 * 仅测试网络连通性 以及 签名验证 ,不包括业务流程、参数验证
 * <p>遍历所有 UniBssApiClient 调用方对应的接口地址，逐一检查：</p>
 * <ul>
 *   <li>✅ 签名生成（buildHead 无异常）</li>
 *   <li>✅ URL 配置是否存在（sys_param / @Value）</li>
 *   <li>✅ HTTP 连通性（接口可达，即使返回业务错误也算通）</li>
 * </ul>
 * 定时表达式建议：0 0 8 * * ?（每天 8:00）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UniBssApiCheckJob {

    private final UniBssApiClient uniBssApiClient;
    private final ISysParamService sysParamService;

    @Value("${ability.url.worksheetsComplete:}")
    private String worksheetsCompleteUrl;

    @XxlJob("uniBssApiCheck")
    public void check() {
        XxlJobHelper.log("===== UniBSS API 连通性检查开始 =====");

        List<ApiCheckResult> results = new ArrayList<>();

        // 1. 签名生成测试（无网络依赖）
        results.add(checkSign());

        // 2. ConOpenSendImpl — 短信发送
        results.add(checkSysParamUrl("短信发送", "SEND_MSG_INTERFACE", "zj_sms_url"));

        // 3. OrderSyncGroupJob — 订单同步
        results.add(checkSysParamUrl("订单同步", "oppSyncGroupParam", "orderInfoSyncGroupUrl"));

        // 4. DwdContractsDServiceImpl — 电子化表单合同
        results.add(checkSysParamUrl("电子化表单合同", "oppSyncGroupParam", "qryEformContractUrl"));

        // 5. PendingSyncTodoServiceImpl — 待办同步集团
        results.add(checkCodeUrl("待办同步集团", "todoTaskSyncUrl"));

        // 6. OppoUnBoundSyncServiceImpl — 商机同步
        results.add(checkSysParamUrl("商机同步", "oppSyncGroupParam", "postUrl"));

        // 7. NengKaiCallbackServiceImpl — 能开回调 worksheetsComplete
        results.add(checkDirectUrl("能开回调 worksheetComplete", worksheetsCompleteUrl));

        // 8. SmartBrainKpiDataJob — 智慧大脑 KPI 推送
        results.add(checkSysParamUrl("智慧大脑KPI推送", "oppSyncGroupParam", "insertSystemKpiDataUrl"));

        // 输出汇总
        XxlJobHelper.log("===== UniBSS API 连通性检查结果 =====");
        long passed = results.stream().filter(r -> r.passed).count();
        for (ApiCheckResult r : results) {
            String tag = r.passed ? "✅" : "❌";
            XxlJobHelper.log("{} [{}] {} — {}", tag, r.bizName, r.passed ? "通过" : "失败", r.detail);
        }
        XxlJobHelper.log("===== 总计: {}/{} 通过 =====", passed, results.size());
    }

    /** 检查签名生成 */
    private ApiCheckResult checkSign() {
        try {
            Map<String, Object> head = UniBssApiClient.buildHead("TEST_APP", "TEST_KEY");
            if (head.containsKey("APP_ID") && head.containsKey("TOKEN")) {
                return ok("签名生成", "buildHead 正常, TOKEN=" + head.get("TOKEN"));
            }
            return fail("签名生成", "buildHead 返回缺少 TOKEN");
        } catch (Exception e) {
            return fail("签名生成", "buildHead 异常: " + e.getMessage());
        }
    }

    /** 从 sys_param 按 type + key 读 URL 并连通性测试 */
    private ApiCheckResult checkSysParamUrl(String bizName, String paramType, String key) {
        try {
            Map<String, String> params = sysParamService.findValueMapsByType(paramType);
            String url = MapUtils.getString(params, key);
            if (!StringUtils.hasText(url)) {
                return fail(bizName, "sys_param[" + paramType + "." + key + "] 未配置");
            }
            return testConnectivity(bizName, url, params.get("zj_app_id"), params.get("zj_app_key"));
        } catch (Exception e) {
            return fail(bizName, "加载 sys_param[" + paramType + "." + key + "] 异常: " + e.getMessage());
        }
    }

    /** 从 sys_param 按 code 读 URL 并连通性测试 */
    private ApiCheckResult checkCodeUrl(String bizName, String code) {
        try {
            String url = sysParamService.findValueByCode(code);
            if (!StringUtils.hasText(url)) {
                return fail(bizName, "sys_param[" + code + "] 未配置");
            }
            return testConnectivity(bizName, url, null, null);
        } catch (Exception e) {
            return fail(bizName, "加载 sys_param[" + code + "] 异常: " + e.getMessage());
        }
    }

    /** 直接对给定 URL 做连通性测试（用于 @Value 注入的地址） */
    private ApiCheckResult checkDirectUrl(String bizName, String url) {
        if (!StringUtils.hasText(url)) {
            return fail(bizName, "@Value 未配置 (ability.url.worksheetsComplete)");
        }
        return testConnectivity(bizName, url, null, null);
    }

    /** 对 URL 发起最小化 UniBSS POST 请求，验证 HTTP 连通性 */
    private ApiCheckResult testConnectivity(String bizName, String url, String appId, String appKey) {
        try {
            Map<String, Object> head;
            if (appId != null && appKey != null) {
                head = UniBssApiClient.buildHead(appId, appKey);
            } else {
                head = uniBssApiClient.buildHead();
            }

            Map<String, Object> body = new HashMap<>();
            body.put("CHECK_REQ", new HashMap<>());

            long start = System.currentTimeMillis();
            UniBssResponse resp = uniBssApiClient.post(url, head, body);
            long cost = System.currentTimeMillis() - start;

            // 有 HTTP 响应（即便是业务错误）即证明连通
            if (resp != null) {
                return ok(bizName, String.format("接口可达 [%dms] respCode=%d, busiCode=%s",
                    cost, resp.getRespCode(), resp.getBusiCode()));
            }
            return fail(bizName, "响应为空");
        } catch (Exception e) {
            return fail(bizName, "连接异常: " + e.getMessage());
        }
    }

    private static ApiCheckResult ok(String bizName, String detail) {
        return new ApiCheckResult(bizName, true, detail);
    }

    private static ApiCheckResult fail(String bizName, String detail) {
        return new ApiCheckResult(bizName, false, detail);
    }

    private static class ApiCheckResult {
        final String bizName;
        final boolean passed;
        final String detail;

        ApiCheckResult(String bizName, boolean passed, String detail) {
            this.bizName = bizName;
            this.passed = passed;
            this.detail = detail;
        }
    }
}
