package com.crm.system.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dao.SysParamDo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.dao.servicehall.ZqztPreTravelReport;
import com.crm.system.domain.dao.CustNecRelationDo;
import com.crm.system.domain.dao.zbzq.VisitAppointmentDo;
import com.crm.system.domain.dao.zbzq.VisitRecordDo;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.mapper.ZqztPreTravelReportMapper;
import com.crm.system.mapper.cust.CustNecRelationMapper;
import com.crm.system.mapper.param.SysParamMapper;
import com.crm.system.mapper.zbzq.VisitAppointmentQueryMapper;
import com.crm.system.mapper.zbzq.WarningQueryMapper;
import com.crm.system.service.IZqztPreTravelReportService;
import com.crm.system.service.sys.ISysParamService;
import com.crm.system.service.travel.PreTravelReportApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ZqztPreTravelReportServiceImpl implements IZqztPreTravelReportService {

    private final ZqztPreTravelReportMapper baseMapper;
    private final VisitAppointmentQueryMapper visitAppointmentQueryMapper;
    private final CustNecRelationMapper custNecRelationMapper;
    private final WarningQueryMapper warningQueryMapper;
    private final SysUsersMapper sysUsersMapper;
    private final PreTravelReportApiService apiService;
    private final SysParamMapper sysParamMapper;

    private static final String REPORT_ID_PREFIX = "BR-";
    private static final String DATA_SOURCE_JOB = "job";
    private static final String TASK_STATUS_PENDING = "0";
    private static final String TASK_STATUS_SUCCESS = "2";
    private static final String TASK_STATUS_FAILED = "3";

    @Override
    public int createPendingReports(String startTime, String endTime) {
        SysParamDo param = sysParamMapper.selectOne(
            new LambdaQueryWrapper<SysParamDo>()
                .eq(SysParamDo::getType, "travel_report_area_type")
                .eq(SysParamDo::getCode, "travel_code")
                .select(SysParamDo::getValue)
        );
        String areaValue = Optional.ofNullable(param)
            .map(SysParamDo::getValue)
            .orElse("");
        List<VisitAppointmentDo> appointments = visitAppointmentQueryMapper
                .selectUnvisitedAppointments(areaValue, startTime, endTime);

        if (appointments.isEmpty()) {
            log.info("未查询到近1小时创建且未拜访的预约记录");
            return 0;
        }

        int seq = getNextSeqForToday();
        int inserted = 0;
        for (VisitAppointmentDo appt : appointments) {
            ZqztPreTravelReport report = new ZqztPreTravelReport();
            report.setReportId(generateReportId(seq++));
            report.setCustomerId(appt.getCustomerId());
            report.setCustomerName(appt.getCustomName());
            report.setCreator(appt.getUserId());
            report.setTaskStatus(TASK_STATUS_PENDING);
            report.setRetryCount(0);
            report.setCreateTime(new Date());
            report.setUpdateTime(new Date());
            baseMapper.insert(report);
            inserted++;
        }

        log.info("行前报告待生成记录插入完成, 共{}条, startTime={}, endTime={}", inserted, startTime, endTime);
        return inserted;
    }

    @Override
    public int processPreTravelReports(String startTime, String endTime) {
        SysParamDo param = sysParamMapper.selectOne(
            new LambdaQueryWrapper<SysParamDo>()
                .eq(SysParamDo::getType, "travel_report_area_type")
                .eq(SysParamDo::getCode, "travel_code")
                .select(SysParamDo::getValue)
        );
        String areaValue = Optional.ofNullable(param)
            .map(SysParamDo::getValue)
            .orElse("");
        List<VisitAppointmentDo> appointments = visitAppointmentQueryMapper
                .selectUnvisitedAppointments(areaValue, startTime, endTime);

        if (appointments.isEmpty()) {
            log.info("未查询到近1小时创建且未拜访的预约记录");
            return 0;
        }

        int seq = getNextSeqForToday();
        int processed = 0;


        for (VisitAppointmentDo appt : appointments) {

            // 剔重：检查 APPOINTMENT_ID 是否已经生成过报告
            String appointmentId = appt.getAppointmentId();
            if (appointmentId != null) {
                Long exists = baseMapper.selectCount(
                    new LambdaQueryWrapper<ZqztPreTravelReport>()
                        .eq(ZqztPreTravelReport::getAppointmentId, Long.valueOf(appointmentId))
                );
                if (exists > 0) {
                    log.info("该预约已生成过行前报告, appointmentId={}, 跳过", appointmentId);
                    continue;
                }
            }

            String reportId = generateReportId(seq++);
            String customerId = appt.getCustomerId();
            String userId = appt.getUserId();
            String oaAccount = appt.getCreateUser();
            // 1. 获取用户DB ID (master, 用oaAccount查)
            String userDbId = getSysUserDbId(oaAccount);

            ZqztPreTravelReport report = new ZqztPreTravelReport();
            report.setReportId(reportId);
            report.setCustomerId(customerId);
            report.setCustomerName(appt.getCustomName());
            report.setOaAccount(oaAccount);
            report.setCreator(userDbId != null ? userDbId : oaAccount);
            report.setRetryCount(0);
            report.setDataSource(DATA_SOURCE_JOB);
            if (appointmentId != null) {
                report.setAppointmentId(Long.valueOf(appointmentId));
            }
            report.setCreateTime(new Date());
            report.setUpdateTime(new Date());

            if (ObjectUtil.isEmpty(userDbId)) {
                log.warn("未找到该用户, oaAccount={}, 跳过生成并记录取消状态", oaAccount);
                report.setTaskStatus("99");
                report.setTaskStatusDesc("未找到该用户");
                baseMapper.insert(report);
                processed++;
                continue;
            }

            log.info("开始处理行前报告, reportId={}, customerId={}, userId={}, oaAccount={}, appointmentId={}",
                    reportId, customerId, userId, oaAccount, appointmentId);

            try {
                // 2. 查拜访信息 (slave_2)
                VisitRecordDo visitRecord = visitAppointmentQueryMapper
                        .selectLastVisitRecord(customerId, appt.getCreateUser());

                // 3. 查自然→实体客户映射 (slave_1)
                List<CustNecRelationDo> necRelations = custNecRelationMapper
                        .selectByNatureCustId(customerId);
                List<String> provinceCustIds = necRelations.stream()
                        .map(CustNecRelationDo::getProvinceCustId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());



                // 4. 查预警信息 (master)
                List<Map<String, Object>> warnings = new ArrayList<>();
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

                // 5. 查客户信息 (slave_2)
                Map<String, Object> custInfo = visitAppointmentQueryMapper.selectCustomerInfo(customerId);

                // 6. 查商机信息 (master)
                List<Map<String, Object>> opportunities = new ArrayList<>();
                if (customerId != null && userDbId != null) {
                    opportunities = warningQueryMapper.selectOpportunities(customerId, userDbId);
                }

                // 7. 组装Hiagent请求数据
                Map<String, Object> reportMeta = buildReportMeta(reportId, appt.getVisitTime());
                Map<String, Object> customer = buildCustomer(appt, visitRecord, custInfo);
                Map<String, Object> visitTarget = buildVisitTarget(visitRecord);
                Map<String, Object> lastVisit = buildLastVisit(visitRecord);

                // 8. 调用Hiagent API
                String apiRequest = JSON.toJSONString(buildApiRequestLog(reportMeta, customer, visitTarget, lastVisit, warnings, opportunities));
                report.setApiRequest(apiRequest);

                String apiResponse = apiService.callHiagentApi(reportMeta, customer, visitTarget, lastVisit, warnings, opportunities);
                report.setApiResponse(apiResponse);
                String mdContent = apiService.extractReportContent(apiResponse);
                if(StringUtils.isEmpty(mdContent)){
                    report.setTaskStatus(TASK_STATUS_FAILED);
                    report.setTaskStatusDesc("生成失败:mdContent为空 ");
                }else{
                    report.setMdContent(mdContent);
                    report.setTaskStatus(TASK_STATUS_SUCCESS);
                    report.setTaskStatusDesc("生成成功");
                }
            } catch (Exception e) {
                log.error("处理行前报告异常, reportId={}, customerId={}", reportId, customerId, e);
                report.setTaskStatus(TASK_STATUS_FAILED);
                report.setTaskStatusDesc("生成失败: " + e.getMessage());
                report.setApiResponse(e.getMessage());
            }

            report.setUpdateTime(new Date());
            baseMapper.insert(report);
            processed++;
        }

        log.info("行前报告全流程处理完成, 共{}条", processed);
        return processed;
    }

    @Override
    public int retryFailedReports() {
        // 1. 读取最大重试次数配置，默认3次
        int maxRetry = 3;
        SysParamDo retryParam = sysParamMapper.selectOne(
            new LambdaQueryWrapper<SysParamDo>()
                .eq(SysParamDo::getType, "travel_report_area_type")
                .eq(SysParamDo::getCode, "max_retry_count")
                .select(SysParamDo::getValue)
        );
        if (retryParam != null && retryParam.getValue() != null) {
            try {
                maxRetry = Integer.parseInt(retryParam.getValue().trim());
            } catch (NumberFormatException e) {
                log.warn("最大重试次数配置解析失败, 使用默认值3, value={}", retryParam.getValue());
            }
        }

        // 2. 查询待重试的记录：data_source=job, task_status=3, retry_count < maxRetry
        List<ZqztPreTravelReport> failedReports = baseMapper.selectList(
            new LambdaQueryWrapper<ZqztPreTravelReport>()
                .eq(ZqztPreTravelReport::getDataSource, DATA_SOURCE_JOB)
                .eq(ZqztPreTravelReport::getTaskStatus, TASK_STATUS_FAILED)
                .lt(ZqztPreTravelReport::getRetryCount, maxRetry)
        );

        if (failedReports.isEmpty()) {
            log.info("无待重试的行前报告记录, maxRetry={}", maxRetry);
            return 0;
        }

        log.info("查询到{}条待重试的行前报告记录, maxRetry={}", failedReports.size(), maxRetry);
        int processed = 0;

        for (ZqztPreTravelReport report : failedReports) {
            Long appointmentId = report.getAppointmentId();
            log.info("开始重处理行前报告, reportId={}, customerId={}, appointmentId={}, retryCount={}",
                    report.getReportId(), report.getCustomerId(), appointmentId, report.getRetryCount());

            // 3. 校验预约是否已拜访
            if (appointmentId != null) {
                int visitCount = visitAppointmentQueryMapper
                        .countVisitRecordByAppointmentId(String.valueOf(appointmentId));
                if (visitCount > 0) {
                    report.setTaskStatus("99");
                    report.setTaskStatusDesc("已完成拜访，任务取消");
                    report.setUpdateTime(new Date());
                    baseMapper.updateById(report);
                    log.info("预约已完成拜访，取消行前报告, reportId={}, appointmentId={}", report.getReportId(), appointmentId);
                    processed++;
                    continue;
                }
            }

            // 4. 重处理：重新调用Hiagent接口
            String customerId = report.getCustomerId();
            String oaAccount = report.getOaAccount();
            String userDbId = report.getCreator();

            try {
                // 查询预约信息获取 visitTime
                VisitAppointmentDo appt = null;
                if (appointmentId != null) {
                    appt = visitAppointmentQueryMapper
                            .selectAppointmentById(String.valueOf(appointmentId));
                }
                // 兜底：旧数据可能无appointmentId，用report已存字段构造
                if (appt == null) {
                    appt = new VisitAppointmentDo();
                    appt.setCustomerId(report.getCustomerId());
                    appt.setCustomName(report.getCustomerName());
                }

                // 查拜访信息
                VisitRecordDo visitRecord = visitAppointmentQueryMapper
                        .selectLastVisitRecord(customerId, oaAccount);

                // 查自然→实体客户映射
                List<CustNecRelationDo> necRelations = custNecRelationMapper
                        .selectByNatureCustId(customerId);
                List<String> provinceCustIds = necRelations.stream()
                        .map(CustNecRelationDo::getProvinceCustId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                // 查预警信息
                List<Map<String, Object>> warnings = new ArrayList<>();
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

                // 查客户信息
                Map<String, Object> custInfo = visitAppointmentQueryMapper.selectCustomerInfo(customerId);

                // 查商机信息
                List<Map<String, Object>> opportunities = new ArrayList<>();
                if (customerId != null && userDbId != null) {
                    opportunities = warningQueryMapper.selectOpportunities(customerId, userDbId);
                }

                // 组装Hiagent请求数据
                Date visitTime = (appt != null) ? appt.getVisitTime() : null;
                Map<String, Object> reportMeta = buildReportMeta(report.getReportId(), visitTime);
                Map<String, Object> customer = buildCustomer(appt, visitRecord, custInfo);
                Map<String, Object> visitTarget = buildVisitTarget(visitRecord);
                Map<String, Object> lastVisit = buildLastVisit(visitRecord);

                // 调用Hiagent API
                String apiRequest = JSON.toJSONString(buildApiRequestLog(reportMeta, customer, visitTarget, lastVisit, warnings, opportunities));
                report.setApiRequest(apiRequest);

                String apiResponse = apiService.callHiagentApi(reportMeta, customer, visitTarget, lastVisit, warnings, opportunities);
                report.setApiResponse(apiResponse);

                String mdContent = apiService.extractReportContent(apiResponse);
                if (StringUtils.isEmpty(mdContent)) {
                    report.setTaskStatus(TASK_STATUS_FAILED);
                    report.setTaskStatusDesc("重试失败: 报告内容为空");
                    report.setRetryCount(report.getRetryCount() + 1);
                } else {
                    report.setMdContent(mdContent);
                    report.setTaskStatus(TASK_STATUS_SUCCESS);
                    report.setRetryCount(report.getRetryCount() + 1);
                    report.setTaskStatusDesc("重试成功");
                }
            } catch (Exception e) {
                log.error("重处理行前报告异常, reportId={}, customerId={}", report.getReportId(), customerId, e);
                report.setTaskStatus(TASK_STATUS_FAILED);
                report.setTaskStatusDesc("重试失败: " + e.getMessage());
                report.setRetryCount(report.getRetryCount() + 1);
                report.setApiResponse(e.getMessage());
            }

            report.setUpdateTime(new Date());
            baseMapper.updateById(report);
            processed++;
        }

        log.info("行前报告重处理完成, 共处理{}条", processed);
        return processed;
    }

    private String getSysUserDbId(String oaAccount) {
        if (oaAccount == null) {
            return null;
        }
        LambdaQueryWrapper<SysUsers> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUsers::getOaAccount, oaAccount).last("limit 1");
        SysUsers user = sysUsersMapper.selectOne(wrapper);
        return user != null ? String.valueOf(user.getId()) : null;
    }

    private Map<String, Object> buildReportMeta(String reportId, Date visitTime) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("report_id", reportId);
        meta.put("generate_time", DateUtil.format(new Date(), "yyyy-MM-dd"));
        meta.put("visit_appointment_time", visitTime != null ? DateUtil.format(visitTime, "yyyy-MM-dd HH:mm") : null);
        return meta;
    }

    private Map<String, Object> buildCustomer(VisitAppointmentDo appt, VisitRecordDo visitRecord,
                                               Map<String, Object> custInfo) {
        Map<String, Object> customer = new LinkedHashMap<>();
        if (custInfo != null && !custInfo.isEmpty()) {
            customer.put("name", custInfo.getOrDefault("name", appt.getCustomName()));
            customer.put("industry", custInfo.get("industry"));
            customer.put("level", custInfo.get("level") != null ? custInfo.get("level") : "集团客户");
            customer.put("region", custInfo.get("region") != null ? custInfo.get("region") : buildRegion(visitRecord));
        } else {
            customer.put("name", appt.getCustomName());
            customer.put("industry", null);
            customer.put("level", "集团客户");
            customer.put("region", buildRegion(visitRecord));
        }
        return customer;
    }

    private String buildRegion(VisitRecordDo visitRecord) {
        if (visitRecord == null) {
            return null;
        }
        return String.join(" / ",
                Arrays.asList(visitRecord.getProv(), visitRecord.getCity(), visitRecord.getDistrict())
                        .stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }

    private Map<String, Object> buildVisitTarget(VisitRecordDo visitRecord) {
        Map<String, Object> target = new LinkedHashMap<>();
        if (visitRecord != null) {
            target.put("name", visitRecord.getIntervieweeName());
            target.put("title", visitRecord.getIntervieweeJob());
            target.put("is_same_as_last", true);
            target.put("last_visit_date", visitRecord.getVisitTime() != null
                    ? DateUtil.format(visitRecord.getVisitTime(), "yyyy-MM-dd") : null);
        }
        return target;
    }

    private Map<String, Object> buildLastVisit(VisitRecordDo visitRecord) {
        Map<String, Object> lastVisit = new LinkedHashMap<>();
        if (visitRecord != null) {
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
        }
        return lastVisit;
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

    private int getNextSeqForToday() {
        String todayPrefix = REPORT_ID_PREFIX + DateUtil.format(new Date(), "yyyyMMdd") + "-";
        LambdaQueryWrapper<ZqztPreTravelReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(ZqztPreTravelReport::getReportId, todayPrefix)
               .orderByDesc(ZqztPreTravelReport::getReportId)
               .last("limit 1");
        ZqztPreTravelReport last = baseMapper.selectOne(wrapper);
        if (last == null || last.getReportId() == null) {
            return 1;
        }
        String lastSeq = last.getReportId().substring(todayPrefix.length());
        try {
            return Integer.parseInt(lastSeq) + 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private String generateReportId(int seq) {
        String datePart = DateUtil.format(new Date(), "yyyyMMdd");
        return REPORT_ID_PREFIX + datePart + "-" + String.format("%03d", seq);
    }
}
