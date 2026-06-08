package com.crm.job.mapper.warning;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 宽带升级预警 Mapper
 *
 * @author crm
 */
@Mapper
public interface WideUpWarningMapper {

    @Select({"SELECT DISTINCT a.id_, a.name_, a.bss_code "
            + "FROM sys_area a "
            + "WHERE a.bss_code IS NOT NULL "
            + "ORDER BY a.id_"})
    List<Map<String, Object>> selectAllAreas();

    @Select({"SELECT DISTINCT u.user_id, u.dept_id, u.nick_name, u.phonenumber FROM sys_user u "
            + "JOIN sys_user_role ur ON u.user_id = ur.user_id "
            + "JOIN sys_role r ON ur.role_id = r.role_id "
            + "WHERE r.role_key IN ('boss', 'vice_boss') AND u.del_flag = '0'"})
    List<Map<String, Object>> selectLeaders();
}
