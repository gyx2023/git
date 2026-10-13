package com.crm.job.mapper.warning;

import org.apache.ibatis.annotations.Mapper;
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
     * 条件为 {@code <=}
     * </p>
     *
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
            + "      <= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '%Y-%m-%d') "
            + "  AND (SELECT COUNT(1) FROM FLO_ACT_HI_TASKINST his "
            + "       WHERE his.PROC_INST_ID_ = o.pi_id "
            + "         AND his.TASK_DEF_KEY_ = 'audit_handle') = 0 "
            + "ORDER BY o.id_"})
    List<Map<String, Object>> selectPre2DayOrders();
}
