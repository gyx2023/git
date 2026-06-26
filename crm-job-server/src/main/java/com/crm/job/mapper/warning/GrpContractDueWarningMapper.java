package com.crm.job.mapper.warning;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 合同到期预警 Mapper
 *
 * @author crm
 */
@Mapper
public interface GrpContractDueWarningMapper {

    @Select({"SELECT f.id_, f.grp_code, f.grp_name, f.serv_manager_id, f.warning_level "
            + "FROM ftp_tc_grp_contract_due_warning f "
            + "LEFT JOIN maintenance_detail m ON f.id_ = m.detail_id AND m.order_type = 'htdq_warn' "
            + "WHERE m.status IS NULL "
            + "AND DATE_FORMAT(f.create_date, '%Y%m%d') = #{targetDate}"})
    List<Map<String, Object>> selectDueList(@Param("targetDate") String targetDate);
}
