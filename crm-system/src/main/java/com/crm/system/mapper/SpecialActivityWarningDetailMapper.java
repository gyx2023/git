package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 特殊活动到期预警明细 Mapper。
 * <p>
 * 与沃TV同构：客户级建单主表 ftp_tc_user_cust_warning(warning_type=000032) 的 main_order_flow_no，
 * 对应用户级通用预警表 ftp_tc_user_common_warning(warning_type=000032) 同主订单流水的多条用户行。
 * 明细链：工单 info_id(客户级行id) → main_order_flow_no → ftp_tc_user_common_warning 多条。
 * </p>
 *
 * @author crm
 */
@Mapper
public interface SpecialActivityWarningDetailMapper {

    /**
     * 新数据源：按主键回查客户级预警表 ftp_tc_user_cust_warning 的 main_order_flow_no。
     *
     * @param id 客户级预警行主键（工单 info_id）
     * @return main_order_flow_no，无则 null
     */
    @Select({"SELECT main_order_flow_no FROM ftp_tc_user_cust_warning "
            + "WHERE id_ = #{id} AND warning_type = '000032' LIMIT 1"})
    String selectMainOrderFlowNoFromCust(@Param("id") Long id);

    /**
     * 历史数据兜底：按主键回查用户级预警表 ftp_tc_user_common_warning 的 main_order_flow_no。
     *
     * @param id 用户级预警行主键（历史工单 info_id）
     * @return main_order_flow_no，无则 null
     */
    @Select({"SELECT main_order_flow_no FROM ftp_tc_user_common_warning "
            + "WHERE id_ = #{id} AND warning_type = '000032' LIMIT 1"})
    String selectMainOrderFlowNoById(@Param("id") Long id);

    /**
     * 按主订单流水号查询同一批用户级明细（多条）
     */
    @Select({"SELECT "
            + "w.id_, w.svc_num, w.cust_name, w.developer_name, w.developer_id, "
            + "w.prod_name, w.hq_svc_type, w.user_status, "
            + "w.area_name, w.county_id, w.open_date, "
            + "w.warning_info, w.ep_special_scheme_name, w.ep_special_create_date, "
            + "w.ep_special_eff_date, w.ep_special_exp_date, "
            + "w.grp_code, w.grp_name, w.main_order_flow_no, "
            + "c.cycle_flag, c.warning_info AS cycle_warning_info "
            + "FROM ftp_tc_user_common_warning w "
            + "LEFT JOIN ftp_tc_user_cycle_warning c ON c.order_flow_no = w.order_flow_no AND c.id_ = (SELECT MAX(c2.id_) FROM ftp_tc_user_cycle_warning c2 WHERE c2.order_flow_no = w.order_flow_no) "
            + "WHERE w.warning_type = '000032' "
            + "AND w.main_order_flow_no = #{mainOrderFlowNo} "
            + "ORDER BY w.id_"})
    List<Map<String, Object>> selectByMainOrderFlowNo(@Param("mainOrderFlowNo") String mainOrderFlowNo);

    /**
     * 按主订单流水号分页查询同一批用户级明细（多条）
     */
    @Select({"SELECT "
            + "w.id_, w.svc_num, w.cust_name, w.developer_name, w.developer_id, "
            + "w.prod_name, w.hq_svc_type, w.user_status, "
            + "w.area_name, w.county_id, w.open_date, "
            + "w.warning_info, w.ep_special_scheme_name, w.ep_special_create_date, "
            + "w.ep_special_eff_date, w.ep_special_exp_date, "
            + "w.grp_code, w.grp_name, w.main_order_flow_no, "
            + "c.cycle_flag, c.warning_info AS cycle_warning_info "
            + "FROM ftp_tc_user_common_warning w "
            + "LEFT JOIN ftp_tc_user_cycle_warning c ON c.order_flow_no = w.order_flow_no AND c.id_ = (SELECT MAX(c2.id_) FROM ftp_tc_user_cycle_warning c2 WHERE c2.order_flow_no = w.order_flow_no) "
            + "WHERE w.warning_type = '000032' "
            + "AND w.main_order_flow_no = #{mainOrderFlowNo} "
            + "ORDER BY w.id_"})
    IPage<Map<String, Object>> selectByMainOrderFlowNoPage(Page<?> page, @Param("mainOrderFlowNo") String mainOrderFlowNo);
}
