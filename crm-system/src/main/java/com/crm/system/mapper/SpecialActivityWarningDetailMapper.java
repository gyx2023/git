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
 * 数据源自用户级通用预警表 ftp_tc_user_common_warning(warning_type=000032)，
 * 明细按 infoId（即工单 info_id → ftp_tc_user_common_warning.id_）直接查询该条记录，
 * 并 LEFT JOIN ftp_tc_user_cycle_warning 取闭环/续约信息。
 * </p>
 */
@Mapper
public interface SpecialActivityWarningDetailMapper {

    @Select({"SELECT "
            + "w.id_, w.svc_num, w.cust_name, w.developer_name, w.developer_id, "
            + "w.prod_name, w.prod_id, w.hq_svc_type, w.user_status, "
            + "w.area_name, w.county_id, w.open_date, "
            + "w.warning_info, w.ep_special_scheme_name, w.ep_special_create_date, "
            + "w.ep_special_eff_date, w.ep_special_exp_date, "
            + "w.grp_code, w.grp_name, "
            + "c.cycle_flag, c.warning_info AS cycle_warning_info "
            + "FROM ftp_tc_user_common_warning w "
            + "LEFT JOIN ftp_tc_user_cycle_warning c ON w.order_flow_no = c.order_flow_no "
            + "WHERE w.id_ = #{id} "
            + "AND w.warning_type = '000032' "
            + "LIMIT 1"})
    Map<String, Object> selectOneById(@Param("id") Long id);
}
