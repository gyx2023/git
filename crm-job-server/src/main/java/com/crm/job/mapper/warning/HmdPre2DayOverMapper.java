package com.crm.job.mapper.warning;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 红名单到期前两天自动升级领导 Mapper
 *
 * @author crm
 */
@Mapper
public interface HmdPre2DayOverMapper {

    /**
     * 查询「红名单到期已逾期且客户经理仍未处理」的工单，用于自动升级领导。
     * <p>
     * 到期日（{@code WARNING_INFO} 第 3 段）{@code <= 比较日期}。比较日期由 Job 传入，直接参与比较。
     * </p>
     *
     * @param compareDate 比较日期（{@code yyyy-MM-dd}），如 {@code 2026-10-15}，由 Job 计算后传入：
     *                    执行器参数有值时用参数换算结果，无值时用 {@code 当前日期 - 2 天}，
     *                    与老框架 {@code doOverCmHmdPre2Day} 的日常调度语义一致
     * @return 每条 [{@code orderId}=topp_order.id_，{@code piId}=流程实例ID，{@code taskId}=当前客户经理待办ID]
     */
    @Select({"SELECT o.id_ AS orderId, o.pi_id AS piId, t.ID_ AS taskId "
            + "FROM topp_order o "
            + "JOIN FLO_ACT_RU_TASK t ON t.PROC_INST_ID_ = o.pi_id "
            + "JOIN ftp_tc_user_cust_warning c ON o.info_id = c.id_ "
            + "WHERE o.order_type = 'hmd_warn' "
            + "  AND o.status_ IN ('running', 'tracking') "
            + "  AND o.process_engine = 'FLOWABLE' "
            + "  AND c.WARNING_TYPE = '000030' "
            + "  AND t.TASK_DEF_KEY_ = 'manager_handle' "
            + "  AND (SELECT DATE_FORMAT(SUBSTRING_INDEX(SUBSTRING_INDEX(u.WARNING_INFO, '||', 3), '||', -1), '%Y-%m-%d') "
            + "       FROM ftp_tc_user_common_warning u "
            + "       WHERE u.MAIN_ORDER_FLOW_NO = c.MAIN_ORDER_FLOW_NO "
            + "       LIMIT 1) "
            + "      <= DATE_FORMAT(#{compareDate}, '%Y-%m-%d') "
            + "  AND (SELECT COUNT(1) FROM FLO_ACT_HI_TASKINST his "
            + "       WHERE his.PROC_INST_ID_ = o.pi_id "
            + "         AND his.TASK_DEF_KEY_ = 'audit_handle') = 0 "
            + "ORDER BY o.id_"})
    List<Map<String, Object>> selectPre2DayOrders(@Param("compareDate") String compareDate);
}
