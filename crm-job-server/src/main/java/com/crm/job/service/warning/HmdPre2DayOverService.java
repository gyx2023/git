package com.crm.job.service.warning;

import com.crm.common.utils.DateUtils;
import com.crm.job.mapper.warning.HmdPre2DayOverMapper;
import com.crm.system.service.IHmdExpireLeaderService;
import com.crm.workflow.service.WarningWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 红名单到期前两天自动升级领导 Service
 * <p>
 * 复刻旧系统 {@code FtpTcUserCustWarningService.doOverCmHmdPre2Day → dealHMDTask(taskFlag="3")}
 * 把「红名单到期已逾期且客户经理仍未处理」的工单，
 * 在<b>同一条流程内</b>送领导审核。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HmdPre2DayOverService {

    /** 送领导审核的流程动作值（对齐 hmd_warn_approval.bpmn managerGateway 出线条件） */
    private static final String ACTION_NOT_RENEW_TO_LEADER = "notRenewToLeader";

    /** 处理意见，逐字沿用老框架 dealHMDTask 的 taskFlag="3" 分支 */
    private static final String OPINION = "红名单到期前2天,系统自动升级领导审核";

    /** 无入参调用时的默认回退天数：比较日期 = 当前日期 - 2 天 */
    private static final int DEFAULT_BEFORE_DAYS = 2;

    private final HmdPre2DayOverMapper pre2DayOverMapper;
    private final IHmdExpireLeaderService hmdExpireLeaderService;
    private final WarningWorkflowService warningWorkflowService;

    /**
     * 执行一次红名单到期前两天自动升级。
     * <p>
     * 比较日期取默认值：当前日期 - 2 天
     * </p>
     *
     * @return 成功送领导审核的工单数
     */
    public int doOver() {
        return doOver(DateUtils.getBeforeNDays(DateUtils.YYYY_MM_DD, DEFAULT_BEFORE_DAYS));
    }

    /**
     * 执行一次红名单到期前两天自动升级，支持指定比较日期。
     *
     * @param compareDate 比较日期（{@code yyyy-MM-dd}），即 SQL 中 {@code 到期日 <= 该日期} 的右值；
     *                    日常调度由 Job 传入执行器参数换算结果，未传参时 Job 取 {@code 当前日期 - 2 天}，
     *                    等价老框架 {@code DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '%Y-%m-%d')}
     * @return 成功送领导审核的工单数
     */
    public int doOver(String compareDate) {
        List<Map<String, Object>> orders = pre2DayOverMapper.selectPre2DayOrders(compareDate);
        log.info("【红名单到期前2天升级】比较日期={}, 待处理工单数={}", compareDate, orders.size());
        if (orders.isEmpty()) {
            return 0;
        }
        int success = 0;
        for (Map<String, Object> order : orders) {
            // 单条失败不影响其余工单（对齐老框架丢线程池逐条处理、互不阻断的语义）
            try {
                if (escalate(order)) {
                    success++;
                }
            } catch (Exception e) {
                log.error("【红名单到期前2天升级】处理异常: orderId={}, taskId={}",
                        order.get("orderId"), order.get("taskId"), e);
            }
        }
        log.info("【红名单到期前2天升级】处理完成: 成功={}, 待处理={}", success, orders.size());
        return success;
    }

    /**
     * 单条工单送领导审核。
     *
     * @return 是否成功送审
     */
    private boolean escalate(Map<String, Object> order) {
        Long orderId = toLong(order.get("orderId"));
        String piId = toStr(order.get("piId"));
        String taskId = toStr(order.get("taskId"));
        if (orderId == null || taskId == null) {
            log.error("【红名单到期前2天升级】捞单结果缺少 orderId/taskId, 跳过: {}", order);
            return false;
        }

        // 领导候选人：取第一个（总经理），对齐老框架 leaderList.get(0)
        List<Map<String, Object>> leaders = hmdExpireLeaderService.listLeaders(orderId);
        if (leaders == null || leaders.isEmpty()) {
            log.error("订单{}任务{}获取领导信息为空", orderId, taskId);
            return false;
        }
        Long leaderId = toLong(leaders.get(0).get("userId"));
        if (leaderId == null) {
            log.error("订单{}任务{}领导候选人缺少 userId, 跳过", orderId, taskId);
            return false;
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put("audit", String.valueOf(leaderId));
        warningWorkflowService.completeWithAction(taskId, ACTION_NOT_RENEW_TO_LEADER, OPINION, vars);
        log.info("【红名单到期前2天升级】工单已送领导审核: orderId={}, piId={}, taskId={}, leaderId={}",
                orderId, piId, taskId, leaderId);
        return true;
    }

    /** 兼容 JDBC 返回的 Number / 字符串取值 */
    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String toStr(Object value) {
        return value == null ? null : value.toString();
    }
}
