package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 沃TV到期预警明细 Mapper
 * <p>
 * 查询 ftp_tc_user_common_warning（用户级预警明细）+ ftp_tc_user_cycle_warning（用户级闭环），
 * 通过 order_flow_no 关联。详情展示复用通用预警表，不单独建详情表。
 * </p>
 *
 * @author crm
 */
@Mapper
public interface WotvExpireWarningDetailMapper {

    /**
     * 按主订单流水号查询预警明细列表
     *
     * @param mainOrderFlowNo 主订单流水号（来自 ftp_tc_user_cust_warning.main_order_flow_no）
     * @return 明细记录列表
     */
    @Select({"SELECT "
            + "w.id_, w.svc_num, w.cust_name, w.developer_name, w.developer_id, "
            + "w.prod_name, w.hq_svc_type, w.user_status, "
            + "w.serv_manager_name, w.oa_name_dev, w.oa_id_dev, "
            + "w.warning_info, w.new_scheme_name, w.new_create_date, w.new_exp_date, "
            + "w.order_flow_no, w.open_date, "
            + "c.user_status AS cycle_user_status, "
            + "c.warning_info AS cycle_warning_info, "
            + "c.cycle_flag "
            + "FROM ftp_tc_user_common_warning w "
            + "LEFT JOIN ftp_tc_user_cycle_warning c ON w.order_flow_no = c.order_flow_no "
            + "WHERE w.main_order_flow_no = #{mainOrderFlowNo} "
            + "ORDER BY w.id_"})
    List<Map<String, Object>> selectByMainOrderFlowNo(@Param("mainOrderFlowNo") String mainOrderFlowNo);

    /**
     * 按主订单流水号分页查询预警明细列表
     *
     * @param page            分页参数
     * @param mainOrderFlowNo 主订单流水号（来自 ftp_tc_user_cust_warning.main_order_flow_no）
     * @return 分页明细记录
     */
    @Select({"SELECT "
            + "w.id_, w.svc_num, w.cust_name, w.developer_name, w.developer_id, "
            + "w.prod_name, w.hq_svc_type, w.user_status, "
            + "w.serv_manager_name, w.oa_name_dev, w.oa_id_dev, "
            + "w.warning_info, w.new_scheme_name, w.new_create_date, w.new_exp_date, "
            + "w.order_flow_no, w.open_date, "
            + "c.user_status AS cycle_user_status, "
            + "c.warning_info AS cycle_warning_info, "
            + "c.cycle_flag "
            + "FROM ftp_tc_user_common_warning w "
            + "LEFT JOIN ftp_tc_user_cycle_warning c ON w.order_flow_no = c.order_flow_no "
            + "WHERE w.main_order_flow_no = #{mainOrderFlowNo} "
            + "ORDER BY w.id_"})
    IPage<Map<String, Object>> selectByMainOrderFlowNoPage(Page<?> page, @Param("mainOrderFlowNo") String mainOrderFlowNo);

    /**
     * 按主键查询 ftp_tc_user_cust_warning 获取 main_order_flow_no
     *
     * @param id ftp_tc_user_cust_warning 主键
     * @return main_order_flow_no
     */
    @Select({"SELECT main_order_flow_no FROM ftp_tc_user_cust_warning WHERE id_ = #{id}"})
    String selectMainOrderFlowNoById(@Param("id") Long id);
}
