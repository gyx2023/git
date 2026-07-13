package com.crm.system.service.travel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.crm.system.client.hiagent.HiagentApiClient;
import com.crm.system.domain.dao.sys.SysOperLog;
import com.crm.system.service.sys.ISysOperLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PreTravelReportApiService {

    @Resource
    private PreTravelReportProperties preTravelReportProperties;
    @Resource
    private HiagentApiClient hiagentApiClient;
    @Resource
    private ISysOperLogService sysOperLogService;

    private static final Map<String, String> WARNING_SCENARIOS = new HashMap<>();
    static {
        WARNING_SCENARIOS.put("C00001", "针对办理多云业务包周期活动的用户，在活动到期前3月预警客户经理");
        WARNING_SCENARIOS.put("C00004", "预警类型包含账单预打发票 和 营业预打发票（一次性费用）");
        WARNING_SCENARIOS.put("C00005", "电路业务割接预警，提示哪些电路会受影响");
        WARNING_SCENARIOS.put("C00007", "针对双线包周期活动的用户，在活动到期前2月预警客户经理");
        WARNING_SCENARIOS.put("000031", "主要针对客户的公司内的员工，个人受理的手机套餐等活动到期前1个月预警");
        WARNING_SCENARIOS.put("000030", "客户关键人员加入红名单后不停机，在到期前15天推送预警");
        WARNING_SCENARIOS.put("000029", "与客户签订的合同到期前90天推送预警");
        WARNING_SCENARIOS.put("000034", "客户出账收入较上月下降1000元以上或出账用户数下降20%以上");
        WARNING_SCENARIOS.put("C00023", "多条宽带装在一个地址");
        WARNING_SCENARIOS.put("000032", "主要针对客户的公司内的员工，个人受理的手机特殊活动到期前30天预警");
        WARNING_SCENARIOS.put("000033", "主要针对客户的公司内的员工，个人受理的手机终端到期前3个月预警");
        WARNING_SCENARIOS.put("C00009", "客户下用户的逾期欠费（含销户用户欠费）");
        WARNING_SCENARIOS.put("C00002", "针对办理宽带包周期活动的用户，在活动到期前2月预警客户经理");
        WARNING_SCENARIOS.put("C00022", "（实时余额-下个月月租费）<0");
    }

    public String callHiagentApi(Map<String, Object> reportMeta,
                                 Map<String, Object> customer,
                                 Map<String, Object> visitTarget,
                                 Map<String, Object> lastVisit,
                                 List<Map<String, Object>> warnings,
                                 List<Map<String, Object>> opportunities) {
        SysOperLog operLog = new SysOperLog();
        operLog.setTitle("Hiagent行前报告接口");
        operLog.setRequestMethod("POST");
        operLog.setOperatorType(2);
        operLog.setOperParam(JSON.toJSONString(reportMeta));
        long startTime = System.currentTimeMillis();
        try {
            String url = preTravelReportProperties.getApiUrl();
            String apiKey = preTravelReportProperties.getApiKey();
            String appKey = preTravelReportProperties.getAppKey();
            String userId = preTravelReportProperties.getUserId();

            operLog.setOperUrl(url);

            Map<String, Object> reportData = new HashMap<>();
            reportData.put("report_meta", reportMeta);
            reportData.put("customer", customer);
            reportData.put("visit_target", visitTarget);
            reportData.put("last_visit", lastVisit != null ? lastVisit : new HashMap<>());
            reportData.put("warnings", warnings != null ? warnings : new java.util.ArrayList<>());
            reportData.put("opportunities", opportunities != null ? opportunities : new java.util.ArrayList<>());
            reportData.put("warning_scenarios", WARNING_SCENARIOS);

            Map<String, Object> inputDataMap = new HashMap<>();
            inputDataMap.put("report", JSON.toJSONString(reportData));

            Map<String, Object> reqBodyMap = new HashMap<>();
            reqBodyMap.put("AppKey", appKey);
            reqBodyMap.put("InputData", JSON.toJSONString(inputDataMap));
            reqBodyMap.put("UserID", userId);

            log.info("调用Hiagent行前报告接口, report_id={}, url={}", reportMeta.get("report_id"), url);
            String respStr = hiagentApiClient.post(url, apiKey, reqBodyMap);

            if (respStr == null) {
                throw new RuntimeException("Hiagent 接口返回为空");
            }

            log.info("Hiagent行前报告接口返回, report_id={}, resp={}", reportMeta.get("report_id"), respStr);
            operLog.setStatus(0);
            operLog.setJsonResult(respStr);
            return respStr;

        } catch (Exception e) {
            log.error("调用Hiagent行前报告接口异常, report_id={}", reportMeta.get("report_id"), e);
            operLog.setStatus(1);
            operLog.setErrorMsg(e.getMessage());
            throw new RuntimeException("调用Hiagent接口失败: " + e.getMessage(), e);
        } finally {
            operLog.setRequestTime(System.currentTimeMillis() - startTime);
            sysOperLogService.insertOperlog(operLog);
        }
    }

    /**
     * 从Hiagent返回串中提取行前报告MD内容
     * 找到 nodeType="end" 的节点，逐层解析 output → output → report_content
     */
    public String extractReportContent(String apiResponse) {
        try {
            JSONObject root = JSON.parseObject(apiResponse);

            JSONObject nodes = root.getJSONObject("nodes");
            if (nodes == null) {
                log.warn("返回串中无nodes节点");
                return null;
            }
            JSONObject endNode = null;
            for (String key : nodes.keySet()) {
                JSONObject node = nodes.getJSONObject(key);
                if ("end".equals(node.getString("nodeType"))) {
                    endNode = node;
                    break;
                }
            }
            if (endNode == null) {
                log.warn("返回串中未找到end节点");
                return null;
            }

            String output1 = endNode.getString("output");
            if (output1 == null) {
                return null;
            }
            JSONObject output1Json = JSON.parseObject(output1);

            String output2 = output1Json.getString("output");
            if (output2 == null) {
                return null;
            }
            JSONObject output2Json = JSON.parseObject(output2);
            return output2Json.get("report_content") == null ? output2Json.getString("markdown_report") : output2Json.getString("report_content");
        } catch (Exception e) {
            log.error("解析Hiagent返回内容异常", e);
            return null;
        }
    }
}
