package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 宽带到期预警明细 Mapper
 * <p>
 * 取数与老框架对齐：老框架 Wide 处理页的用户清单走
 * {@code WideSysAutoWarningService.getWideSysAutoWarningByMainOrderFlowNo}
 * （{@code wide_sys_auto_warning} 按 main_order_flow_no 查询），
 * 而非沃TV那套 {@code ftp_tc_user_common_warning} + {@code warning_info} 拼接解析。
 * </p>
 * <p>
 * 用户状态 {@code user_status} 取自 {@code ftp_tc_user_cycle_warning}，
 * 与 {@code WxCustOrderMapper.selectWideUserList}（PC 清单）保持一致口径。
 * 列别名直接输出前端字段名，Service 层无需二次转换。
 * </p>
 *
 * @author crm
 */
@Mapper
public interface WideExpireWarningDetailMapper {

    /**
     * 按主订单流水号查询用户清单（不分页）
     *
     * @param mainOrderFlowNo 主订单流水号（来自 ftp_tc_user_cust_warning.main_order_flow_no）
     * @return 明细记录列表
     */
    @Select({"SELECT w.id AS userId, w.svc_num AS svcNum, w.cust_name AS custName, "
            + "w.contact_man AS contactMan, w.contact_phone AS contactPhone, "
            + "w.developer_name AS developerName, w.scheme_name AS schemeName, "
            + "w.lvl_name AS lvlName, w.service_name AS serviceName, w.conn_type AS connType, "
            + "w.amount_per_year AS amountPerYear, "
            + "DATE_FORMAT(w.eff_time, '%Y-%m-%d %H:%i:%s') AS effTime, "
            + "DATE_FORMAT(w.exp_time, '%Y-%m-%d %H:%i:%s') AS expTime, "
            + "w.rate_ AS rate, w.user_addr AS userAddr, w.exte_flag AS exteFlag, "
            + "w.relation_type_name AS relationTypeName, "
            + "w.NEW_SCHEME_ID AS newSchemeId, w.NEW_SCHEME_NAME AS newSchemeName, "
            + "DATE_FORMAT(w.NEW_CREATE_TIME, '%Y-%m-%d %H:%i:%s') AS newCreateTime, "
            + "DATE_FORMAT(w.NEW_EFF_TIME, '%Y-%m-%d %H:%i:%s') AS newEffTime, "
            + "DATE_FORMAT(w.NEW_EXP_TIME, '%Y-%m-%d %H:%i:%s') AS newExpTime, "
            + "w.NEW_SCHEME_PERIOD AS newSchemePeriod, w.NEW_SCHEME_FEE AS newSchemeFee, "
            + "fw.user_status AS userStatus "
            + "FROM wide_sys_auto_warning w "
            + "LEFT JOIN ftp_tc_user_cycle_warning fw ON w.ORDER_FLOW_NO = fw.ORDER_FLOW_NO "
            + "WHERE w.main_order_flow_no = #{mainOrderFlowNo} "
            + "ORDER BY w.id ASC"})
    List<Map<String, Object>> selectByMainOrderFlowNo(@Param("mainOrderFlowNo") String mainOrderFlowNo);

    /**
     * 按主订单流水号分页查询用户清单（LIMIT 由分页插件注入）
     *
     * @param page            分页参数
     * @param mainOrderFlowNo 主订单流水号（来自 ftp_tc_user_cust_warning.main_order_flow_no）
     * @return 分页明细记录
     */
    @Select({"SELECT w.id AS userId, w.svc_num AS svcNum, w.cust_name AS custName, "
            + "w.contact_man AS contactMan, w.contact_phone AS contactPhone, "
            + "w.developer_name AS developerName, w.scheme_name AS schemeName, "
            + "w.lvl_name AS lvlName, w.service_name AS serviceName, w.conn_type AS connType, "
            + "w.amount_per_year AS amountPerYear, "
            + "DATE_FORMAT(w.eff_time, '%Y-%m-%d %H:%i:%s') AS effTime, "
            + "DATE_FORMAT(w.exp_time, '%Y-%m-%d %H:%i:%s') AS expTime, "
            + "w.rate_ AS rate, w.user_addr AS userAddr, w.exte_flag AS exteFlag, "
            + "w.relation_type_name AS relationTypeName, "
            + "w.NEW_SCHEME_ID AS newSchemeId, w.NEW_SCHEME_NAME AS newSchemeName, "
            + "DATE_FORMAT(w.NEW_CREATE_TIME, '%Y-%m-%d %H:%i:%s') AS newCreateTime, "
            + "DATE_FORMAT(w.NEW_EFF_TIME, '%Y-%m-%d %H:%i:%s') AS newEffTime, "
            + "DATE_FORMAT(w.NEW_EXP_TIME, '%Y-%m-%d %H:%i:%s') AS newExpTime, "
            + "w.NEW_SCHEME_PERIOD AS newSchemePeriod, w.NEW_SCHEME_FEE AS newSchemeFee, "
            + "fw.user_status AS userStatus "
            + "FROM wide_sys_auto_warning w "
            + "LEFT JOIN ftp_tc_user_cycle_warning fw ON w.ORDER_FLOW_NO = fw.ORDER_FLOW_NO "
            + "WHERE w.main_order_flow_no = #{mainOrderFlowNo} "
            + "ORDER BY w.id ASC"})
    IPage<Map<String, Object>> selectByMainOrderFlowNoPage(Page<?> page, @Param("mainOrderFlowNo") String mainOrderFlowNo);

    /**
     * 按主键查询 ftp_tc_user_cust_warning 获取 main_order_flow_no
     *
     * @param id ftp_tc_user_cust_warning 主键（即 topp_order.info_id）
     * @return main_order_flow_no
     */
    @Select({"SELECT main_order_flow_no FROM ftp_tc_user_cust_warning WHERE id_ = #{id}"})
    String selectMainOrderFlowNoById(@Param("id") Long id);
}
