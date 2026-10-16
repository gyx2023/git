package com.crm.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 终端合约到期预警未派发指派查询 Mapper。
 * <p>
 * 用于手动指派（批量转派）时按 maintenance_detail.detail_id 反查原始预警行，组装建单上下文。
 * </p>
 *
 * @author crm
 */
@Mapper
public interface TerminalContractWarningAssignMapper {

    /**
     * 新数据源：客户级预警表 ftp_tc_user_cust_warning（warning_type=000033）。
     * <p>
     * 终端合约已迁到客户级派单，未派发明细 detail_id 优先反查本表。
     * </p>
     */
    @Select({"SELECT w.id_, w.grp_name, w.grp_code, w.oa_id_dev, w.oa_name_dev, "
            + "w.serv_manager_id, w.warning_type, w.area_name, w.county_name, w.cust_grp_id "
            + "FROM ftp_tc_user_cust_warning w "
            + "WHERE w.id_ = #{id}"})
    Map<String, Object> selectByCustId(@Param("id") Long id);

    /**
     * 旧数据源（兜底）：用户级预警表 ftp_tc_user_common_warning（warning_type=000033）。
     * <p>
     * 历史未派发明细 detail_id 仍指向用户级表，客户级表查不到时回退本表。
     * </p>
     */
    @Select({"SELECT w.id_, w.grp_name, w.grp_code, w.oa_id_dev, w.oa_name_dev, "
            + "w.serv_manager_id, w.warning_type, w.area_name, w.county_id, w.cust_grp_id "
            + "FROM ftp_tc_user_common_warning w "
            + "WHERE w.id_ = #{id}"})
    Map<String, Object> selectTerminalContractById(@Param("id") Long id);
}
