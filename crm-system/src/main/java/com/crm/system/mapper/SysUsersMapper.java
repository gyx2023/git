package com.crm.system.mapper;

import com.crm.system.domain.dao.SysUsers;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.vo.SysUsersVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户(客户的员工)Mapper接口
 *
 * @author crm
 * @date 2025-12-23
 */
public interface SysUsersMapper extends BaseMapperPlus<SysUsersMapper, SysUsers, SysUsersVo> {

    /**
     * 超时未登录用户
     *
     * @param failureDays 超时时长
     * @return 超时未登录用户列表
     */
    @Select("SELECT u.* FROM sys_user u " +
        "LEFT JOIN (SELECT user_id, MAX(login_time) AS login_time FROM sys_login_log GROUP BY user_id) l ON u.id_ = l.user_id " +
        "WHERE u.is_valid = '1' AND NOT EXISTS (SELECT 1 FROM sys_user_not_failure n WHERE u.id_ = n.user_id) " +
        "  AND (l.login_time <= DATE_SUB(NOW(), INTERVAL #{failureDays} DAY) OR l.login_time IS NULL) " +
        "  AND u.create_time <= DATE_SUB(NOW(), INTERVAL #{failureDays} DAY) ")
    List<SysUsers> selectFailureList(@Param("failureDays") Integer failureDays);

}
