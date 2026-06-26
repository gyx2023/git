package com.crm.job.mapper.warning;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 欠费预警 Mapper
 *
 * @author crm
 */
@Mapper
public interface GrpOweWarningMapper {

    @Select({"SELECT f.id_, f.grp_code, f.grp_name, f.serv_manager_id, f.warning_level, f.warning_type "
            + "FROM ftp_tc_grp_owe_warning f "
            + "LEFT JOIN maintenance_detail m ON f.id_ = m.detail_id AND m.order_type IN ('qf_warn') "
            + "WHERE m.status IS NULL "
            + "AND f.warning_type != 'C00009' "
            + "AND DATE_FORMAT(f.create_date, '%Y%m%d') = #{targetDate}"})
    List<Map<String, Object>> selectOweList(@Param("targetDate") String targetDate);
}
