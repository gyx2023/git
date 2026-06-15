package com.crm.system.service;

import com.crm.system.domain.dao.PersonnelInfo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.bo.SysUsersBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.crm.system.domain.vo.SysUsersVo;

import java.util.Collection;
import java.util.List;

/**
 * 用户(客户的员工)Service接口
 *
 * @author crm
 * @date 2025-12-23
 */
public interface ISysUsersService {
    /**
     * 查询用户(客户的员工)
     */
    SysUsersVo queryById(Long id);

    /**
     * 查询用户(客户的员工)列表
     */
    TableDataInfo<SysUsersVo> queryPageList(SysUsersBo bo, PageQuery pageQuery);

    /**
     * 查询用户(客户的员工)列表
     */
    List<SysUsersVo> queryList(SysUsersBo bo);

    /**
     * 新增用户(客户的员工)
     */
    Boolean insertByBo(SysUsersBo bo);

    /**
     * 修改用户(客户的员工)
     */
    Boolean updateByBo(SysUsersBo bo);

    /**
     * 校验并批量删除用户(客户的员工)信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 查询手机号对应的用户
     */
    List<SysUsers> findList(PersonnelInfo personnelInfo);

    /**
     * 查询OA对应的用户
     */
    List<SysUsers> findOaList(PersonnelInfo personnelInfo);

    /**
     * 修改/保存用户
     */
    void saveOrUpdate(SysUsers sysUsers);

    /**
     * 新增用户
     */
    void insertSysUser(SysUsers user);

    /**
     * 查询长期未登录用户
     *
     * @param failureDays 超时时长
     * @return 长期未登录用户列表
     */
    List<SysUsers> selectFailureList(Integer failureDays);
}
