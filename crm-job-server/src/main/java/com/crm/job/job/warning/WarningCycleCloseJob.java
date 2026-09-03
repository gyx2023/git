package com.crm.job.job.warning;

import com.crm.common.enums.OrderStatusEnum;
import com.crm.common.utils.StringUtils;
import com.crm.flowable.factory.FlowServiceFactory;
import com.crm.job.mapper.warning.UserCycleCloseMapper;
import com.crm.system.domain.dao.SysParamDo;
import com.crm.system.domain.dao.topp.ToppOrder;
import com.crm.system.mapper.topp.ToppOrderMapper;
import com.crm.system.service.sys.ISysParamService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
public class WarningCycleCloseJob extends FlowServiceFactory {

    private static final String PARAM_TYPE = "cycle_close";
    private static final String PARAM_CODE = "cycle_close";

    private static final ExecutorService executorService = new ThreadPoolExecutor(
            10, 10, 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());

    private final ISysParamService sysParamService;
    private final UserCycleCloseMapper userCycleCloseMapper;

    @Autowired
    private ToppOrderMapper toppOrderMapper;

    /** 直接返回"3"（超时）的预警类型 —— 这些类型不判断 normalCloseNum/orderNum 是否相等（对齐老框架 getEndReason） */
    private static final Set<String> FORCE_TIMEOUT_WARNING_TYPES = new HashSet<>(Arrays.asList(
            "C00005",  // 客户级割接预警
            "C00014",  // 双线0出账预警
            "C00015",  // 违规PCDN业务稽核
            "C00019",  // 双线起租预警
            "C00020"   // SI结算稽核预警
    ));

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

        // 3. 查询需要关单的工单（cycleFlag in (1,2,3) 且状态非 end）
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
            updateWarningLevelOnly(order);
        }

        // 6. 异步关单
        for (ToppOrder order : closeOrders) {
            closeOrderAsync(order,
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

    /**
     * 同步关闭单个工单的 Flowable 流程
     * <p>
     * endReason 由 {@link #computeEndReason} 根据闭环数据动态计算，对齐旧系统
     * {@code TcCustCycleWarningJob.getEndReason()}。
     * </p>
     *
     * @param order      待关闭的工单（需包含 cycleWarningType / cycleNormalCloseNum / cycleOrderNum 等 transient 字段）
     * @param resultMemo 处理意见（写入 Flowable 任务 local 变量）
     */
    public void closeOrder(ToppOrder order, String resultMemo) {
        String piId = order.getPiId();
        if (piId == null) {
            log.warn("【闭环自动完结】piId 为空, orderId={}", order.getId());
            return;
        }

        // 查询当前待办任务
        Task task = taskService.createTaskQuery()
                .processInstanceId(piId)
                .singleResult();

        if (task == null) {
            log.warn("【闭环自动完结】无待办任务, orderId={}", order.getId());
            return;
        }

        // 设置本地任务变量
        Map<String, Object> localVariables = new HashMap<>();
        localVariables.put(task.getTaskDefinitionKey() + "_resultCode", "0");
        localVariables.put(task.getTaskDefinitionKey() + "_opinion", "自动完结");
        localVariables.put(task.getTaskDefinitionKey() + "_resultMemo", resultMemo);
        taskService.setVariablesLocal(task.getId(), localVariables);

        // double_approval（两节点）：用 auto_close 走网关默认线直达结束，不推进到稽核节点；
        // single_approval（单节点）：用 agree 正常完结
        Map<String, Object> variables = new HashMap<>();
        variables.put("action",
                task.getProcessDefinitionId().startsWith("double_approval:") ? "auto_close" : "agree");
        taskService.complete(task.getId(), variables);

        // 计算 endReason（对齐旧系统 getEndReason）
        String endReason = computeEndReason(order);

        // 更新工单状态为已结束
        ToppOrder updateOrder = new ToppOrder();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(OrderStatusEnum.END.getCode());
        updateOrder.setEndReason(endReason);
        // 闭环数据到达时同步 warningLevel
        if (order.getCycleWarningLevel() != null) {
            updateOrder.setWarningLevel(order.getCycleWarningLevel());
        }
        toppOrderMapper.updateById(updateOrder);

        log.info("【闭环自动完结】工单已完结: orderId={}, piId={}, type={}, endReason={}",
                order.getId(), piId, order.getOrderType(), endReason);
    }

    /**
     * 仅同步预警等级（不操作 Flowable 流程）
     * <p>
     * 对应旧系统分支1：cycleFlag==0 或工单已结束，只更新续约信息（warningLevel）。
     * </p>
     *
     * @param order 仅需 id_ 和 cycleWarningLevel
     */
    public void updateWarningLevelOnly(ToppOrder order) {
        if (order.getCycleWarningLevel() == null) {
            return;
        }
        ToppOrder updateOrder = new ToppOrder();
        updateOrder.setId(order.getId());
        updateOrder.setWarningLevel(order.getCycleWarningLevel());
        toppOrderMapper.updateById(updateOrder);
        log.info("【闭环自动完结】仅同步预警等级: orderId={}, warningLevel={}",
                order.getId(), order.getCycleWarningLevel());
    }

    /**
     * 计算工单结束原因，对齐旧系统 {@code TcCustCycleWarningJob.getEndReason()}
     * <p>
     * 逻辑：
     * <ol>
     *   <li>cycleFlag 为 2（离网）/3（超时）时原样返回 "2"/"3"</li>
     *   <li>cycleFlag=1 时，对强制超时类型（C00005/C00014/C00015/C00019/C00020）返回 "3"</li>
     *   <li>cycleFlag=1 且 normalCloseNum ≠ orderNum 返回 "3"（部分闭环=超时）</li>
     *   <li>其余 cycleFlag=1 返回 "1"（正常闭环）</li>
     * </ol>
     * </p>
     *
     * @param order 需包含 cycleFlag / cycleWarningType / cycleNormalCloseNum / cycleOrderNum
     * @return endReason 值："1"（正常闭环）、"2"（离网）或 "3"（超时）
     */
    private String computeEndReason(ToppOrder order) {
        Integer cycleFlag = order.getCycleFlag();
        // 离网=2 / 超时=3 原样返回
        if (cycleFlag != null && (cycleFlag == 2 || cycleFlag == 3)) {
            return String.valueOf(cycleFlag);
        }
        // cycleFlag=1 已闭环
        String warningType = order.getCycleWarningType();
        if (StringUtils.isNotEmpty(warningType) && FORCE_TIMEOUT_WARNING_TYPES.contains(warningType)) {
            return "3";
        }
        if (order.getCycleNormalCloseNum() != null && order.getCycleOrderNum() != null
                && !order.getCycleNormalCloseNum().equals(order.getCycleOrderNum())) {
            return "3";
        }
        // 正常闭环
        return "1";
    }

    /**
     * 异步关闭工单（提交到线程池）
     */
    public void closeOrderAsync(ToppOrder order, String resultMemo) {
        executorService.execute(() -> {
            try {
                closeOrder(order, resultMemo);
            } catch (Exception e) {
                log.error("【闭环自动完结】关闭失败 orderId={}", order.getId(), e);
            }
        });
    }
}
