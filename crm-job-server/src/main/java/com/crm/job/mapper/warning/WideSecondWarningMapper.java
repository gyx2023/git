package com.crm.job.mapper.warning;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 宽带二次预警 Mapper
 *
 * @author crm
 */
@Mapper
public interface WideSecondWarningMapper {

    @Select({"SELECT w.id, w.grp_name, w.grp_code, w.oa_id_dev, w.serv_manager_id, w.warning_type "
            + "FROM ftp_tc_user_cust_warning w "
            + "WHERE w.warning_type = 'C00002' "
            + "AND w.is_valid = '1' "
            + "AND DATE_FORMAT(w.exp_time, '%Y-%m-%d') >= #{targetDate} "
            + "ORDER BY w.id"})
    List<Map<String, Object>> selectSecondWarnList(@Param("targetDate") String targetDate);

    @Select({"SELECT user_id, dept_id, nick_name, phonenumber FROM sys_user "
            + "WHERE user_name = #{oaId} AND del_flag = '0' LIMIT 1"})
    Map<String, Object> selectUserByOaId(@Param("oaId") String oaId);
}
