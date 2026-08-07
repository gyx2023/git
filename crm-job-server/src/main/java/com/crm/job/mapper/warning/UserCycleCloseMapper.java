package com.crm.job.mapper.warning;

import com.crm.system.domain.dao.topp.ToppOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 闭环自动完结工单查询 Mapper
 * <p>
 * 参照旧系统 TcCustCycleWarningJob.getOrder() 的查询链路：
 * ftp_tc_cust_cycle_warning（cycle_flag=1）
 *   → ftp_tc_user_cust_warning（main_order_flow_no → id_）
 *     → topp_order（info_id = id_）
 * </p>
 *
 * @author crm
 */
@Mapper
public interface UserCycleCloseMapper {

    /**
     * 查询需要自动关单的工单（cycle_flag=1 且 status=running 且有 Flowable 流程）
     * <p>
     * 对应旧系统 TcCustCycleWarningJob 分支2：闭环数据已到达，完结 Flowable 流程。
     * 含 cycleWarningType / cycleNormalCloseNum / cycleOrderNum 用于计算 endReason。
     * </p>
     */
    @Select({"<script>",
            "SELECT o.id_ AS id, o.pi_id AS piId, o.order_type AS orderType, o.status_ AS status,",
            "  f.WARNING_LEVEL AS cycleWarningLevel,",
            "  f.WARNING_TYPE AS cycleWarningType,",
            "  f.NORMAL_CLOSE_NUM AS cycleNormalCloseNum,",
            "  f.ORDER_NUM AS cycleOrderNum",
            "FROM ftp_tc_cust_cycle_warning f",
            "JOIN ftp_tc_user_cust_warning c ON c.MAIN_ORDER_FLOW_NO = f.MAIN_ORDER_FLOW_NO",
            "JOIN topp_order o ON o.info_id = c.id_",
            "WHERE f.WARNING_TYPE IN",
            "  <foreach collection='warningCodes' item='wc' open='(' separator=',' close=')'>#{wc}</foreach>",
            "  AND f.CYCLE_FLAG = 1",
            "  AND o.status_ = 'running'",
            "  AND o.process_engine = 'FLOWABLE'",
            "  AND o.order_type IN",
            "  <foreach collection='orderTypes' item='ot' open='(' separator=',' close=')'>#{ot}</foreach>",
            "</script>"})
    List<ToppOrder> selectCycleCloseOrders(@Param("orderTypes") List<String> orderTypes,
                                           @Param("warningCodes") List<String> warningCodes);

    /**
     * 查询仅需同步预警等级的工单（cycle_flag=0 或工单已结束）
     * <p>
     * 对应旧系统 TcCustCycleWarningJob 分支1：只更新 warningLevel，不操作流程。
     * </p>
     */
    @Select({"<script>",
            "SELECT o.id_ AS id,",
            "  f.WARNING_LEVEL AS cycleWarningLevel",
            "FROM ftp_tc_cust_cycle_warning f",
            "JOIN ftp_tc_user_cust_warning c ON c.MAIN_ORDER_FLOW_NO = f.MAIN_ORDER_FLOW_NO",
            "JOIN topp_order o ON o.info_id = c.id_",
            "WHERE f.WARNING_TYPE IN",
            "  <foreach collection='warningCodes' item='wc' open='(' separator=',' close=')'>#{wc}</foreach>",
            "  AND (f.CYCLE_FLAG = 0 OR o.status_ &lt;&gt; 'running')",
            "  AND o.process_engine = 'FLOWABLE'",
            "  AND o.order_type IN",
            "  <foreach collection='orderTypes' item='ot' open='(' separator=',' close=')'>#{ot}</foreach>",
            "</script>"})
    List<ToppOrder> selectCycleWarningLevelUpdateOrders(@Param("orderTypes") List<String> orderTypes,
                                                         @Param("warningCodes") List<String> warningCodes);
}
