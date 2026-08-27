package com.crm.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 沃TV到期预警未派发指派查询 Mapper。
 * <p>
 * 用于手动指派（批量转派）时按 maintenance_detail.detail_id 反查
 * ftp_tc_user_cust_warning 原始预警行，组装建单上下文。
 * </p>
 *
 * @author crm
 */
@Mapper
public interface WotvExpireWarningAssignMapper {

    @Select({"SELECT w.id_, w.grp_name, w.grp_code, w.oa_id_dev, w.oa_name_dev, "
            + "w.serv_manager_id, w.warning_type, w.area_name, w.county_name, w.cust_grp_id "
            + "FROM ftp_tc_user_cust_warning w "
            + "WHERE w.id_ = #{id} "
            + "AND w.warning_type = 'C00024' "
            + "LIMIT 1"})
    Map<String, Object> selectWotvById(@Param("id") Long id);
}
