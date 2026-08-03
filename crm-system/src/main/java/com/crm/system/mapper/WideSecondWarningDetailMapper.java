package com.crm.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 宽带二次预警明细 Mapper
 * <p>
 * 查询 wide_sys_auto_warning 表（宽带到期用户明细），
 * 关联 ftp_tc_user_cycle_warning 获取用户状态。
 * 对应旧框架 SecondWarningJob / UserCustWarningAction 中宽带二次预警查询逻辑。
 * </p>
 *
 * @author crm
 */
@Mapper
public interface WideSecondWarningDetailMapper {

    /**
     * 按主订单流水号 + 到期日期查询用户明细列表
     *
     * @param mainOrderFlowNo 主订单流水号（来自 ftp_tc_user_cust_warning.main_order_flow_no）
     * @param targetDate      到期日期，格式 yyyyMMdd
     * @return 宽带到期用户明细列表
     */
    @Select({"SELECT w.svc_num, w.cust_name, w.contact_man, w.contact_phone, "
            + "w.developer_name, w.scheme_name, w.lvl_name, w.service_name, w.conn_type, "
            + "w.amount_per_year, w.eff_time, w.exp_time, w.rate_, w.user_addr, w.exte_flag, "
            + "w.new_scheme_id, w.new_scheme_name, w.new_create_time, w.new_eff_time, "
            + "w.new_exp_time, w.new_scheme_period, w.new_scheme_fee, w.relation_type_name, "
            + "fw.user_status "
            + "FROM wide_sys_auto_warning w "
            + "LEFT JOIN ftp_tc_user_cycle_warning fw ON w.ORDER_FLOW_NO = fw.ORDER_FLOW_NO "
            + "WHERE w.main_order_flow_no = #{mainOrderFlowNo} "
            + "AND DATE_FORMAT(w.exp_time, '%Y%m%d') = #{targetDate} "
            + "AND w.exte_flag = 0 "
            + "ORDER BY w.id ASC"})
    List<Map<String, Object>> selectDetailByOrderFlowNo(@Param("mainOrderFlowNo") String mainOrderFlowNo,
                                                         @Param("targetDate") String targetDate);

    /**
     * 按主订单流水号查询所有用户明细（导出用，不限制 exte_flag）
     *
     * @param mainOrderFlowNo 主订单流水号
     * @param targetDate      到期日期，格式 yyyyMMdd
     * @return 全部宽带到期用户明细
     */
    @Select({"SELECT w.svc_num, w.cust_name, w.contact_man, w.contact_phone, "
            + "w.developer_name, w.scheme_name, w.lvl_name, w.service_name, w.conn_type, "
            + "w.amount_per_year, DATE_FORMAT(w.eff_time, '%Y-%m-%d %H:%i:%s') eff_time, "
            + "DATE_FORMAT(w.exp_time, '%Y-%m-%d %H:%i:%s') exp_time, "
            + "w.rate_, w.user_addr, w.exte_flag, "
            + "w.new_scheme_id, w.new_scheme_name, "
            + "DATE_FORMAT(w.new_create_time, '%Y-%m-%d %H:%i:%s') new_create_time, "
            + "DATE_FORMAT(w.new_eff_time, '%Y-%m-%d %H:%i:%s') new_eff_time, "
            + "DATE_FORMAT(w.new_exp_time, '%Y-%m-%d %H:%i:%s') new_exp_time, "
            + "w.new_scheme_period, w.new_scheme_fee, w.relation_type_name, "
            + "fw.user_status "
            + "FROM wide_sys_auto_warning w "
            + "LEFT JOIN ftp_tc_user_cycle_warning fw ON w.ORDER_FLOW_NO = fw.ORDER_FLOW_NO "
            + "WHERE w.main_order_flow_no = #{mainOrderFlowNo} "
            + "AND DATE_FORMAT(w.exp_time, '%Y%m%d') = #{targetDate} "
            + "ORDER BY w.id ASC"})
    List<Map<String, Object>> selectAllDetailByOrderFlowNo(@Param("mainOrderFlowNo") String mainOrderFlowNo,
                                                           @Param("targetDate") String targetDate);

    /**
     * 按 detailId（infoId）查询 ftp_tc_user_cust_warning 的 main_order_flow_no
     *
     * @param id ftp_tc_user_cust_warning 主键
     * @return main_order_flow_no
     */
    @Select({"SELECT main_order_flow_no FROM ftp_tc_user_cust_warning WHERE id_ = #{id}"})
    String selectMainOrderFlowNoById(@Param("id") Long id);

    /**
     * 统计未续约用户明细数量（exte_flag = 0）
     *
     * @param mainOrderFlowNo 主订单流水号
     * @param targetDate      到期日期，格式 yyyyMMdd
     * @return 记录数
     */
    @Select({"SELECT COUNT(1) FROM wide_sys_auto_warning w "
            + "WHERE w.main_order_flow_no = #{mainOrderFlowNo} "
            + "AND DATE_FORMAT(w.exp_time, '%Y%m%d') = #{targetDate} "
            + "AND w.exte_flag = 0"})
    int countDetailByOrderFlowNo(@Param("mainOrderFlowNo") String mainOrderFlowNo,
                                  @Param("targetDate") String targetDate);
}
