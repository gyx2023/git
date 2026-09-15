package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 用户红名单到期预警(000030) 明细 Mapper
 * <p>
 * 明细维来自 ftp_tc_user_common_warning，通过主订单流水号关联。
 * 查询列/段位对齐旧系统 UserCustWarningAction 000030 导出 SQL（Raw 列，展示转换在 Service 层完成）。
 * </p>
 *
 * @author crm
 */
@Mapper
public interface HmdExpireWarningDetailMapper {

    /**
     * 按主键查询 ftp_tc_user_cust_warning 获取 main_order_flow_no
     *
     * @param id ftp_tc_user_cust_warning 主键（工单 info_id）
     * @return main_order_flow_no
     */
    @Select({"SELECT main_order_flow_no FROM ftp_tc_user_cust_warning WHERE id_ = #{id}"})
    String selectMainOrderFlowNoById(@Param("id") Long id);

    /**
     * 按主键查询客户级预警表头（老 infoByHMD.jsp 首行：客户名称/编码/用户规模 + warning_info）
     *
     * @param id ftp_tc_user_cust_warning 主键（工单 info_id）
     * @return 客户级预警记录（grp_name/grp_code/user_scale/warning_info），无记录时返回 null
     */
    @Select({"SELECT grp_name, grp_code, user_scale, warning_info "
            + "FROM ftp_tc_user_cust_warning WHERE id_ = #{id}"})
    Map<String, Object> selectCustHeaderById(@Param("id") Long id);

    /**
     * 按主订单流水号查询明细列表（对齐旧导出 SQL：concat(area_name, county_id) 拆列 + CASE 转义在 Service 层完成）
     *
     * @param mainOrderFlowNo 主订单流水号（来自 ftp_tc_user_cust_warning.main_order_flow_no）
     * @return 明细记录列表
     */
    @Select({"SELECT w.id_, w.area_name, w.county_id, w.developer_name, "
            + "w.svc_num, w.cust_name, w.open_date, w.prod_name, w.hq_svc_type, w.user_status, w.cert_type, "
            + "w.is_grp_key, "
            + "w.new_red_create_date, w.new_eff_date, w.new_red_exp_date, "
            + "w.cycle_flag, w.warning_level, w.warning_info "
            + "FROM ftp_tc_user_common_warning w "
            + "WHERE w.MAIN_ORDER_FLOW_NO = #{mainOrderFlowNo} ORDER BY w.id_"})
    List<Map<String, Object>> selectByMainOrderFlowNo(@Param("mainOrderFlowNo") String mainOrderFlowNo);

    /**
     * 按主订单流水号分页查询明细列表
     *
     * @param page            分页参数
     * @param mainOrderFlowNo 主订单流水号（来自 ftp_tc_user_cust_warning.main_order_flow_no）
     * @return 分页明细记录
     */
    @Select({"SELECT w.id_, w.area_name, w.county_id, w.developer_name, "
            + "w.svc_num, w.cust_name, w.open_date, w.prod_name, w.hq_svc_type, w.user_status, w.cert_type, "
            + "w.is_grp_key, "
            + "w.new_red_create_date, w.new_eff_date, w.new_red_exp_date, "
            + "w.cycle_flag, w.warning_level, w.warning_info "
            + "FROM ftp_tc_user_common_warning w "
            + "WHERE w.MAIN_ORDER_FLOW_NO = #{mainOrderFlowNo} ORDER BY w.id_"})
    IPage<Map<String, Object>> selectByMainOrderFlowNoPage(Page<?> page, @Param("mainOrderFlowNo") String mainOrderFlowNo);
}
