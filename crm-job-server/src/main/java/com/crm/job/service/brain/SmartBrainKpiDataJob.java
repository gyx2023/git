package com.crm.job.service.brain;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.crm.common.utils.DateUtils;
import com.crm.system.client.unibss.UniBssApiClient;
import com.crm.system.client.unibss.UniBssApiClient.UniBssResponse;
import com.crm.system.service.sys.ISysOperLogService;
import com.crm.system.service.sys.ISysParamService;
import com.crm.system.service.brain.SmartBrainKpiDataService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 系统运营指标数据接口,提供给大脑
 *
 * @author fupp
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmartBrainKpiDataJob {
    @Resource
    private SmartBrainKpiDataService smartBrainKpiDataService;
    @Resource
    private UniBssApiClient uniBssApiClient;
    @Resource
    private ISysParamService sysParamService;
    @Resource
    private ISysOperLogService sysOperLogService;

    @XxlJob(value = "smartBrainKpiDataJob")
    public void wxOrderStatistics() {
        log.info("系统运营指标数据接口开始");

        // 默认昨天
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        String yyyyMMdd = new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());

        // 支持任务传参
        String jobParam = XxlJobHelper.getJobParam();
        if (ObjectUtil.isNotEmpty(jobParam)) {
            yyyyMMdd = jobParam;
        }

        startTask(yyyyMMdd);
        log.info("系统运营指标数据接口结束");
    }

    private void startTask(String yyyyMMdd) {
        try {
            log.info("开始清理{}账期数据", yyyyMMdd);
            smartBrainKpiDataService.deleteKpiData(yyyyMMdd, "D");

            // 日指标
            log.info("开始执行日指标");
            insertKpiData(yyyyMMdd, "D");

            // 月指标
            log.info("开始执行月指标");
            smartBrainKpiDataService.deleteKpiData(yyyyMMdd, "M");
            insertKpiData(yyyyMMdd, "M");

            // 年指标（月底执行）
            if (DateUtils.isLastDayOfMonth(yyyyMMdd)) {
                log.info("开始执行年指标");
                smartBrainKpiDataService.deleteKpiData(yyyyMMdd, "Y");
                insertKpiData(yyyyMMdd, "Y");
            }

            // 推送
            outputKpiData(yyyyMMdd);

        } catch (Exception e) {
            log.error("系统运营指标数据接口执行异常", e);
        }
    }

    private void insertKpiData(String yyyyMMdd, String cycleType) {
        // 省级维系预警工单
        long t1 = System.currentTimeMillis();
        smartBrainKpiDataService.buildProvinceKpiInsertSql(yyyyMMdd, cycleType, "GY0002");
        log.info("{}省级维系预警工单数统计耗时：{}ms", cycleType, System.currentTimeMillis() - t1);

        // 地市级维系预警工单
        long t2 = System.currentTimeMillis();
        smartBrainKpiDataService.buildCityKpiInsertSql(yyyyMMdd, cycleType, "GY0002");
        log.info("{}地市级维系预警工单数统计耗时：{}ms", cycleType, System.currentTimeMillis() - t2);

        // 区县级维系预警工单
        long t3 = System.currentTimeMillis();
        smartBrainKpiDataService.buildCountyKpiInsertSql(yyyyMMdd, cycleType, "GY0002");
        log.info("{}区县级维系预警工单数统计耗时：{}ms", cycleType, System.currentTimeMillis() - t3);

        // 闭环指标
        long t4 = System.currentTimeMillis();
        smartBrainKpiDataService.buildProvinceClosedLoopKpiInsertSql(yyyyMMdd, cycleType, "GY0002001");
        log.info("{}省级闭环指标统计耗时：{}ms", cycleType, System.currentTimeMillis() - t4);

        long t5 = System.currentTimeMillis();
        smartBrainKpiDataService.buildCityClosedLoopKpiInsertSql(yyyyMMdd, cycleType, "GY0002001");
        log.info("{}地市级闭环指标统计耗时：{}ms", cycleType, System.currentTimeMillis() - t5);

        long t6 = System.currentTimeMillis();
        smartBrainKpiDataService.buildCountyClosedLoopKpiInsertSql(yyyyMMdd, cycleType, "GY0002001");
        log.info("{}区县级闭环指标统计耗时：{}ms", cycleType, System.currentTimeMillis() - t6);

        // 平均处理时长
        long t7 = System.currentTimeMillis();
        smartBrainKpiDataService.buildProvinceAvgDealTimeKpiInsertSql(yyyyMMdd, cycleType, "GY0002002");
        log.info("{}省级平均处理时长统计耗时：{}ms", cycleType, System.currentTimeMillis() - t7);

        long t8 = System.currentTimeMillis();
        smartBrainKpiDataService.buildCityAvgDealTimeKpiInsertSql(yyyyMMdd, cycleType, "GY0002002");
        log.info("{}地市级平均处理时长统计耗时：{}ms", cycleType, System.currentTimeMillis() - t8);

        long t9 = System.currentTimeMillis();
        smartBrainKpiDataService.buildCountyAvgDealTimeKpiInsertSql(yyyyMMdd, cycleType, "GY0002002");
        log.info("{}区县级平均处理时长统计耗时：{}ms", cycleType, System.currentTimeMillis() - t9);

        // 日活
        long t10 = System.currentTimeMillis();
        smartBrainKpiDataService.buildProvinceDailyActiveUsersKpiInsertSql(yyyyMMdd, cycleType, "GY0001");
        log.info("{}省级日活统计耗时：{}ms", cycleType, System.currentTimeMillis() - t10);

        long t11 = System.currentTimeMillis();
        smartBrainKpiDataService.buildCityDailyActiveUsersKpiInsertSql(yyyyMMdd, cycleType, "GY0001");
        log.info("{}地市级日活统计耗时：{}ms", cycleType, System.currentTimeMillis() - t11);

        long t12 = System.currentTimeMillis();
        smartBrainKpiDataService.buildCountyDailyActiveUsersKpiInsertSql(yyyyMMdd, cycleType, "GY0001");
        log.info("{}区县级日活统计耗时：{}ms", cycleType, System.currentTimeMillis() - t12);

        // 接口调用量
        long t13 = System.currentTimeMillis();
        smartBrainKpiDataService.buildProvinceLogCountKpiInsertSql(yyyyMMdd, cycleType, "GW0001");
        log.info("{}接口调用量统计耗时：{}ms", cycleType, System.currentTimeMillis() - t13);

        // 大于3S接口量
        long t14 = System.currentTimeMillis();
        smartBrainKpiDataService.buildProvinceLongCountKpiInsertSql(yyyyMMdd, cycleType, "GW0002");
        log.info("{}大于3S接口量统计耗时：{}ms", cycleType, System.currentTimeMillis() - t14);
    }

    private void outputKpiData(String yyyyMMdd) {
        log.info("政企中台指标信息推送智慧大脑文件接口开始");
        List<Map> kpiDataList = smartBrainKpiDataService.getKpiDataList(yyyyMMdd);
        log.info("共计需要推送{}条数据", kpiDataList.size());
        Map paramMap = sysParamService.findValueMapsByType("oppSyncGroupParam");
        String url = MapUtils.getString(paramMap, "insertSystemKpiDataUrl");
        String appId = MapUtils.getString(paramMap, "appId");
        String appKey = MapUtils.getString(paramMap, "appKey");
        boolean success = sendToNewInterface(
            url,
            appId,
            appKey,
            kpiDataList
        );

        if (success) {
            log.info("政企中台指标信息推送智慧大脑接口成功");
        } else {
            log.info("政企中台指标信息推送智慧大脑接口失败");
        }
    }

    /**
     * 推送指标数据到智慧大脑（UNI_BSS 协议）
     */
    private boolean sendToNewInterface(String url, String appId, String appSecret, List<Map> kpiDataList) {
        return sysOperLogService.tryApiCall("智慧大脑KPI数据推送", url,
            "kpiDataList.size=" + (kpiDataList != null ? kpiDataList.size() : 0), () -> {
            Map<String, Object> head = UniBssApiClient.buildHead(appId, appSecret);

            Map<String, Object> dataReq = new HashMap<>();
            dataReq.put("taskSource", "G");
            dataReq.put("kpiDataList", kpiDataList);

            Map<String, Object> body = new HashMap<>();
            body.put("INSERT_SYSTEM_KPI_DATA_REQ", dataReq);

            // 原报文中含 UNI_BSS_ATTACHED(MEDIA_INFO="")，仅为空占位，UniBssApiClient 不发送该字段
            UniBssResponse resp = uniBssApiClient.post(url, head, body);

            if (!resp.isSuccess()) {
                throw new RuntimeException("接口返回失败: " + resp);
            }

            JSONObject rspBody = resp.getRawJson();
            if (rspBody == null) {
                throw new RuntimeException("响应为空");
            }

            JSONObject uniBssBody = rspBody.getJSONObject("UNI_BSS_BODY");
            if (uniBssBody == null) {
                throw new RuntimeException("响应无UNI_BSS_BODY");
            }

            JSONObject rsp = uniBssBody.getJSONObject("INSERT_SYSTEM_KPI_DATA_RSP");
            if (rsp == null || rsp.getInteger("code") != 200) {
                throw new RuntimeException(rsp != null ? rsp.toString() : "业务返回code非200");
            }

            return resp.toString();
        });
    }
}
