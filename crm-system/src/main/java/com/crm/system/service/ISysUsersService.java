package com.crm.system.service;

import com.crm.system.domain.SysUsers;
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
     * 同步亚信用户信息
     */
    String doPostForYxUser(SysUsers user, String operType);

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
}
