package com.crm.job.mapper.warning;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 宽带到期预警（C00002 首次预警） Mapper
 * <p>
 * 对应旧系统 TcUserCustWarningNewJob.startTask() 中 warning_type='C00002' 分支。
 * 查询表：ftp_tc_user_cust_warning（warning_type='C00002'）
 *
 * @author crm
 */
@Mapper
public interface WideExpireWarningMapper {

    @Select({"SELECT w.id_, w.grp_name, w.grp_code, w.oa_id_dev, w.oa_name_dev, "
            + "w.serv_manager_id, w.warning_type, w.area_name, w.county_name, w.cust_grp_id "
            + "FROM ftp_tc_user_cust_warning w "
            + "WHERE w.warning_type = 'C00002' "
            + "AND DATE_FORMAT(w.create_date, '%Y%m%d') = #{targetDate} "
            + "ORDER BY w.id_"})
    List<Map<String, Object>> selectWideExpireList(@Param("targetDate") String targetDate);
}
