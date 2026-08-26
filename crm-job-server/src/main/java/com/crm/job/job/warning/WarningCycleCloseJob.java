package com.crm.job.job.warning;

import com.crm.common.utils.StringUtils;
import com.crm.job.mapper.warning.UserCycleCloseMapper;
import com.crm.job.service.workflow.WarningOrderCloseService;
import com.crm.system.domain.dao.SysParamDo;
import com.crm.system.domain.dao.topp.ToppOrder;
import com.crm.system.service.sys.ISysParamService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用闭环自动完结任务
 * <p>
 * 参照旧系统 TcCustCycleWarningJob 的查询链路：
 * ftp_tc_cust_cycle_warning（cycle_flag=1）→ ftp_tc_user_cust_warning → topp_order，
 * 找到对应的运行中 Flowable 工单并立即关闭。与 {@code WarningAutoCloseJob}（14天超时关闭）互补。
 * </p>
 * <p>
 * 配置驱动：通过 sys_param（type='cycle_close', code='cycle_close'）配置
 * warning_type → order_type 映射，逗号分隔，格式如：
 * <pre>C00023=kd_addr_warn,C00024=wotv_expire_warning</pre>
 * 后续新增类型只需修改配置值，无需改动代码。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WarningCycleCloseJob {

    private static final String PARAM_TYPE = "cycle_close";
    private static final String PARAM_CODE = "cycle_close";

    private final ISysParamService sysParamService;
    private final UserCycleCloseMapper userCycleCloseMapper;
    private final WarningOrderCloseService closeService;

    @XxlJob("warningCycleCloseJob")
    public void cycleClose() {
        long startTime = System.currentTimeMillis();
        log.info("【闭环自动完结】开始执行");

        // 1. 读取配置
        SysParamDo config = sysParamService.findValueByTypeCode(PARAM_TYPE, PARAM_CODE);
        if (config == null || StringUtils.isBlank(config.getValue())) {
            log.warn("【闭环自动完结】未找到 type={}, code={} 的配置", PARAM_TYPE, PARAM_CODE);
            XxlJobHelper.handleSuccess("未找到闭环自动完结配置");
            return;
        }

        // 2. 解析配置：C00023=kd_addr_warn,C00024=wotv_expire_warning → Map<warningCode, orderType>
        Map<String, String> typeMapping = parseTypeMapping(config.getValue());
        if (typeMapping.isEmpty()) {
            log.warn("【闭环自动完结】配置解析为空, value={}", config.getValue());
            XxlJobHelper.handleSuccess("闭环自动完结配置解析为空");
            return;
        }

        List<String> warningCodes = new ArrayList<>(typeMapping.keySet());
        List<String> orderTypes = new ArrayList<>(typeMapping.values());

        // 与旧系统一致：默认前2天，jobparam 可覆盖为指定 yyyyMMdd
        String date = resolveDate();

        log.info("【闭环自动完结】配置: {}, 处理日期: {}", typeMapping, date);
        XxlJobHelper.log("闭环自动完结配置: " + typeMapping + ", 处理日期: " + date);

        // 3. 查询需要关单的工单（cycleFlag in (1,2,3) 且 running）
        List<ToppOrder> closeOrders = userCycleCloseMapper
                .selectCycleCloseOrders(orderTypes, warningCodes, date);

        // 4. 查询仅需同步预警等级的工单（cycleFlag=0 或已结束）
        List<ToppOrder> levelUpdateOrders = userCycleCloseMapper
                .selectCycleWarningLevelUpdateOrders(orderTypes, warningCodes, date);

        log.info("【闭环自动完结】关单={} 条, 同步预警等级={} 条", closeOrders.size(), levelUpdateOrders.size());
        XxlJobHelper.log("关单=" + closeOrders.size() + " 条, 同步预警等级=" + levelUpdateOrders.size() + " 条");

        if (closeOrders.isEmpty() && levelUpdateOrders.isEmpty()) {
            long costTime = System.currentTimeMillis() - startTime;
            XxlJobHelper.handleSuccess("无待处理工单, 耗时=" + costTime + "ms");
            return;
        }

        // 5. 同步预警等级（不操作流程）
        for (ToppOrder order : levelUpdateOrders) {
            closeService.updateWarningLevelOnly(order);
        }

        // 6. 异步关单
        for (ToppOrder order : closeOrders) {
            closeService.closeOrderAsync(order,
                    "[自动完结]闭环数据到达，系统自动结束");
        }

        long costTime = System.currentTimeMillis() - startTime;
        String resultMsg = String.format("闭环自动完结完成: 关闭=%d, 同步预警等级=%d, 耗时=%dms",
                closeOrders.size(), levelUpdateOrders.size(), costTime);
        log.info("【闭环自动完结】{}", resultMsg);
        XxlJobHelper.handleSuccess(resultMsg);
    }

    /**
     * 与旧系统 TcCustCycleWarningJob.download() 一致：
     * xxl-job 固定传 yyyyMMdd；未传 jobparam 时默认取前2天。
     */
    private String resolveDate() {
        String jobparam = XxlJobHelper.getJobParam();
        if (jobparam != null && !jobparam.trim().isEmpty()) {
            return jobparam.trim();
        }
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DATE, -2);
        return new java.text.SimpleDateFormat("yyyyMMdd").format(calendar.getTime());
    }

    /**
     * 解析配置字符串为 warningType → orderType 映射
     * <p>格式：C00023=kd_addr_warn,C00024=wotv_expire_warning</p>
     */
    static Map<String, String> parseTypeMapping(String configValue) {        Map<String, String> mapping = new LinkedHashMap<>();
        for (String pair : configValue.split(",")) {
            String trimmed = pair.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            int eqIdx = trimmed.indexOf('=');
            if (eqIdx <= 0 || eqIdx >= trimmed.length() - 1) {
                log.warn("【闭环自动完结】配置格式异常，跳过: {}", trimmed);
                continue;
            }
            String key = trimmed.substring(0, eqIdx).trim();
            String value = trimmed.substring(eqIdx + 1).trim();
            if (!key.isEmpty() && !value.isEmpty()) {
                mapping.put(key, value);
            }
        }
        return mapping;
    }
}
