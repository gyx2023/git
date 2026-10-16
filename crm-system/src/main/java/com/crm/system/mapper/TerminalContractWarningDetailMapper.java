package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 终端合约到期预警明细 Mapper。
 * <p>
 * 与团购折扣（000031）、特殊活动（000032）同构：客户级建单主表
 * ftp_tc_user_cust_warning(warning_type=000033) 的 main_order_flow_no，对应用户级通用预警表
 * ftp_tc_user_common_warning(warning_type=000033) 同主订单流水的多条用户行。
 * 明细链：工单 info_id(客户级行id) → main_order_flow_no → ftp_tc_user_common_warning 多条。
 * </p>
 * <p>
 * 与 000031 的差异：续约字段用 new_scheme_name/new_create_date/new_exp_date
 * （不是 ep_purchase_*），且旧列表页/导出表头<b>没有「续约生效时间」列</b>
 * （new_eff_date 字段有值但不展示，本迁移对齐旧页面）。
 * </p>
 * <p>
 * 闭环标识/预警等级优先取闭环表 ftp_tc_user_cycle_warning 的最新一条，回退取用户级预警行自身的值。
 * </p>
 *
 * @author crm
 */
@Mapper
public interface TerminalContractWarningDetailMapper {

    /** 明细列（列表与分页共用） */
    String DETAIL_COLUMNS = "SELECT "
            + "w.id_, w.svc_num, w.county_id, w.cust_name, w.developer_name, w.developer_id, "
            + "w.area_name, w.grp_code, w.grp_name, w.main_order_flow_no, "
            + "w.warning_info, "
            + "w.new_scheme_name, w.new_create_date, w.new_exp_date, "
            + "COALESCE(c.cycle_flag, w.cycle_flag) AS cycle_flag, "
            + "COALESCE(c.warning_level, w.warning_level) AS warning_level "
            + "FROM ftp_tc_user_common_warning w "
            + "LEFT JOIN ftp_tc_user_cycle_warning c ON c.order_flow_no = w.order_flow_no "
            + "AND c.id_ = (SELECT MAX(c2.id_) FROM ftp_tc_user_cycle_warning c2 WHERE c2.order_flow_no = w.order_flow_no) "
            + "WHERE w.warning_type = '000033' "
            + "AND w.main_order_flow_no = #{mainOrderFlowNo} "
            + "ORDER BY w.id_";

    /**
     * 新数据源：按主键回查客户级预警表 ftp_tc_user_cust_warning 的 main_order_flow_no。
     *
     * @param id 客户级预警行主键（工单 info_id）
     * @return main_order_flow_no，无则 null
     */
    @Select({"SELECT main_order_flow_no FROM ftp_tc_user_cust_warning "
            + "WHERE id_ = #{id}"})
    String selectMainOrderFlowNoFromCust(@Param("id") Long id);

    /**
     * 历史数据兜底：按主键回查用户级预警表 ftp_tc_user_common_warning 的 main_order_flow_no。
     *
     * @param id 用户级预警行主键（历史工单 info_id）
     * @return main_order_flow_no，无则 null
     */
    @Select({"SELECT main_order_flow_no FROM ftp_tc_user_common_warning "
            + "WHERE id_ = #{id}"})
    String selectMainOrderFlowNoById(@Param("id") Long id);

    /**
     * 按主订单流水号查询同一批用户级明细（多条）
     */
    @Select({DETAIL_COLUMNS})
    List<Map<String, Object>> selectByMainOrderFlowNo(@Param("mainOrderFlowNo") String mainOrderFlowNo);

    /**
     * 按主订单流水号分页查询同一批用户级明细（多条）
     */
    @Select({DETAIL_COLUMNS})
    IPage<Map<String, Object>> selectByMainOrderFlowNoPage(Page<?> page, @Param("mainOrderFlowNo") String mainOrderFlowNo);
}
