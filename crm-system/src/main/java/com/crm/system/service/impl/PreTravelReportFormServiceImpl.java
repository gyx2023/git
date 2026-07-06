package com.crm.system.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.StringUtils;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.system.domain.bo.PreTravelReportFormBo;
import com.crm.system.domain.dao.CustNecRelationDo;
import com.crm.system.domain.dao.servicehall.ZqztPreTravelReport;
import com.crm.system.domain.dao.zbzq.VisitRecordDo;
import com.crm.system.mapper.ZqztPreTravelReportMapper;
import com.crm.system.mapper.cust.CustNecRelationMapper;
import com.crm.system.mapper.zbzq.VisitAppointmentQueryMapper;
import com.crm.system.mapper.zbzq.WarningQueryMapper;
import com.crm.system.service.IPreTravelReportFormService;
import com.crm.system.service.travel.PreTravelReportApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 行前报告表单服务实现
 *
 * @author crm
 * @date 2026-06-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreTravelReportFormServiceImpl implements IPreTravelReportFormService {

    private final ZqztPreTravelReportMapper baseMapper;
    private final VisitAppointmentQueryMapper visitAppointmentQueryMapper;
    private final CustNecRelationMapper custNecRelationMapper;
    private final WarningQueryMapper warningQueryMapper;
    private final PreTravelReportApiService apiService;

    private static final String REPORT_ID_PREFIX = "BR-";
    private static final String TASK_STATUS_PENDING = "0";
    private static final String TASK_STATUS_SUCCESS = "2";
    private static final String TASK_STATUS_FAILED = "3";

    @Override
    public Map<String, Object> submitPreTravelReport(PreTravelReportFormBo bo) {
        String reportId = generateReportId();
        String customerId = bo.getNatureCustId();

        log.info("开始手动生成行前报告, reportId={}, customerId={}, customerName={}",
                reportId, customerId, bo.getNatureCustName());

        ZqztPreTravelReport report = new ZqztPreTravelReport();
        report.setReportId(reportId);
        report.setCustomerId(customerId);
        report.setCustomerName(bo.getNatureCustName());
        // creator = 当前登录用户的DB ID
        Long currentUserId = LoginHelper.getUserId();
        report.setCreator(currentUserId != null ? String.valueOf(currentUserId) : null);
        report.setOaAccount(LoginHelper.getUsername());
        report.setRetryCount(0);
        report.setCreateTime(new Date());
        report.setUpdateTime(new Date());

        try {
            // 1. 组装 reportMeta
            Map<String, Object> reportMeta = buildReportMeta(reportId, bo.getVisitTime());

            // 2. 查自然→实体客户映射
            List<CustNecRelationDo> necRelations = custNecRelationMapper.selectByNatureCustId(customerId);
            List<String> provinceCustIds = necRelations.stream()
                    .map(CustNecRelationDo::getProvinceCustId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 3. 组装 customer
            Map<String, Object> customer = buildCustomer(bo);

            // 4. 组装 visitTarget（使用表单录入的拜访人员信息，不再从DB查询）
            Map<String, Object> visitTarget = buildVisitTargetFromForm(bo);

            // 5. 查询最近一次拜访记录作为上下文
            Map<String, Object> lastVisit = buildLastVisit(customerId);

            // 6. 查询预警信息
            List<Map<String, Object>> warnings = new ArrayList<>();
            String oaAccount = LoginHelper.getUsername();
            String userDbId = currentUserId != null ? String.valueOf(currentUserId) : null;
            if (!provinceCustIds.isEmpty()) {
                if (oaAccount != null) {
                    warnings.addAll(toWarningMapList(
                            warningQueryMapper.selectCutOverWarnings(provinceCustIds, oaAccount)));
                }
                if (userDbId != null) {
                    warnings.addAll(toWarningMapList(
                            warningQueryMapper.selectOweWarnings(provinceCustIds, userDbId)));
                    warnings.addAll(toWarningMapList(
                            warningQueryMapper.selectContractDueWarnings(provinceCustIds, userDbId)));
                    warnings.addAll(toWarningMapList(
                            warningQueryMapper.selectRedListWarnings(provinceCustIds, userDbId)));
                    warnings.addAll(toWarningMapList(
                            warningQueryMapper.selectReceiptWarnings(provinceCustIds, userDbId)));
                    warnings.addAll(toWarningMapList(
                            warningQueryMapper.selectGroupBuyWarnings(provinceCustIds, userDbId)));
                }
            }

            // 7. 查询商机信息
            List<Map<String, Object>> opportunities = new ArrayList<>();
            if (customerId != null && userDbId != null) {
                opportunities = warningQueryMapper.selectOpportunities(customerId, userDbId);
            }

            // 8. 调用 Hiagent API
            String apiRequest = JSON.toJSONString(buildApiRequestLog(reportMeta, customer, visitTarget, lastVisit, warnings, opportunities));
            report.setApiRequest(apiRequest);

            String apiResponse = apiService.callHiagentApi(reportMeta, customer, visitTarget, lastVisit, warnings, opportunities);
            report.setApiResponse(apiResponse);

            String mdContent = apiService.extractReportContent(apiResponse);
            if (StringUtils.isEmpty(mdContent)) {
                report.setTaskStatus(TASK_STATUS_FAILED);
                report.setTaskStatusDesc("生成失败: 报告内容为空");
            } else {
                report.setMdContent(mdContent);
                report.setTaskStatus(TASK_STATUS_SUCCESS);
                report.setTaskStatusDesc("生成成功");
            }
        } catch (Exception e) {
            log.error("手动生成行前报告异常, reportId={}, customerId={}", reportId, customerId, e);
            report.setTaskStatus(TASK_STATUS_FAILED);
            report.setTaskStatusDesc("调用Hiagent接口失败");
            report.setApiResponse(e.getMessage());
        }

        report.setUpdateTime(new Date());
        baseMapper.insert(report);

        // 返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reportId", reportId);
        result.put("taskStatus", report.getTaskStatus());
        result.put("taskStatusDesc", report.getTaskStatusDesc());
        result.put("mdContent", report.getMdContent());
        return result;
    }

    // ========== 私有方法 ==========

    private Map<String, Object> buildReportMeta(String reportId, String visitTime) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("report_id", reportId);
        meta.put("generate_time", DateUtil.format(new Date(), "yyyy-MM-dd"));
        meta.put("visit_appointment_time", visitTime);
        return meta;
    }

    /**
     * 组装客户信息 —— 基于表单选择的自然客户
     */
    private Map<String, Object> buildCustomer(PreTravelReportFormBo bo) {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("name", bo.getNatureCustName());
        customer.put("industry", null);
        customer.put("level", "集团客户");
        customer.put("region", null);
        return customer;
    }

    /**
     * 组装拜访对象信息 —— 使用表单录入的拜访人姓名和职位
     * 与 PreTravelReportJob 的关键差异：不再从历史拜访记录查询
     */
    private Map<String, Object> buildVisitTargetFromForm(PreTravelReportFormBo bo) {
        Map<String, Object> target = new LinkedHashMap<>();
        target.put("name", bo.getVisitorName());
        target.put("title", bo.getVisitorTitle());
        target.put("is_same_as_last", false);
        target.put("last_visit_date", null);
        return target;
    }

    /**
     * 查询最近一次拜访记录作为上下文
     */
    private Map<String, Object> buildLastVisit(String customerId) {
        try {
            // 使用当前登录用户的 OA account 尝试匹配历史记录
            String oaAccount = LoginHelper.getUsername();
            if (oaAccount == null) {
                return new LinkedHashMap<>();
            }
            VisitRecordDo visitRecord = visitAppointmentQueryMapper.selectLastVisitRecord(customerId, oaAccount);
            if (visitRecord == null) {
                return new LinkedHashMap<>();
            }
            Map<String, Object> lastVisit = new LinkedHashMap<>();
            lastVisit.put("date", visitRecord.getVisitTime() != null
                    ? DateUtil.format(visitRecord.getVisitTime(), "yyyy-MM-dd HH:mm:ss") : null);
            lastVisit.put("theme", visitRecord.getVisitTitle());
            lastVisit.put("form", visitRecord.getVisitType());
            Map<String, Object> interviewee = new LinkedHashMap<>();
            interviewee.put("name", visitRecord.getIntervieweeName());
            interviewee.put("title", visitRecord.getIntervieweeJob());
            lastVisit.put("interviewee", interviewee);
            lastVisit.put("content", visitRecord.getInterviewContent());
            lastVisit.put("summary", visitRecord.getVisitSummary());
            return lastVisit;
        } catch (Exception e) {
            log.warn("查询最近拜访记录失败, customerId={}, error={}", customerId, e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    private List<Map<String, Object>> toWarningMapList(List<Map<String, Object>> rawList) {
        if (rawList == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        List<String> baseKeys = Arrays.asList("type", "warning_level", "order_id", "scenario_explanation");
        for (Map<String, Object> raw : rawList) {
            Map<String, Object> warning = new LinkedHashMap<>();
            warning.put("type", raw.get("type"));
            warning.put("level", raw.get("warning_level"));
            warning.put("order_id", raw.get("order_id"));
            warning.put("scenario_explanation", raw.get("scenario_explanation"));

            Map<String, Object> keyInfo = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : raw.entrySet()) {
                String key = entry.getKey();
                if (!baseKeys.contains(key)) {
                    keyInfo.put(key, entry.getValue());
                }
            }
            warning.put("key_info", keyInfo);
            result.add(warning);
        }
        return result;
    }

    private Map<String, Object> buildApiRequestLog(Map<String, Object> reportMeta,
                                                    Map<String, Object> customer,
                                                    Map<String, Object> visitTarget,
                                                    Map<String, Object> lastVisit,
                                                    List<Map<String, Object>> warnings,
                                                    List<Map<String, Object>> opportunities) {
        Map<String, Object> textData = new LinkedHashMap<>();
        textData.put("report_meta", reportMeta);
        textData.put("customer", customer);
        textData.put("visit_target", visitTarget);
        textData.put("last_visit", lastVisit);
        textData.put("warnings", warnings);
        textData.put("opportunities", opportunities);
        return textData;
    }

    private String generateReportId() {
        String datePart = DateUtil.format(new Date(), "yyyyMMdd");
        String todayPrefix = REPORT_ID_PREFIX + datePart + "-";
        LambdaQueryWrapper<ZqztPreTravelReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(ZqztPreTravelReport::getReportId, todayPrefix)
               .orderByDesc(ZqztPreTravelReport::getReportId)
               .last("limit 1");
        ZqztPreTravelReport last = baseMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getReportId() != null) {
            String lastSeq = last.getReportId().substring(todayPrefix.length());
            try {
                seq = Integer.parseInt(lastSeq) + 1;
            } catch (NumberFormatException e) {
                seq = 1;
            }
        }
        return todayPrefix + String.format("%03d", seq);
    }
}
