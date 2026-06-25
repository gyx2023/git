package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.system.domain.dao.PersonnelInfo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.bo.SysUsersBo;
import com.crm.system.domain.vo.SysUsersVo;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.service.ISysUsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 用户(客户的员工)Service业务层处理
 *
 * @author crm
 * @date 2025-12-23
 */
@RequiredArgsConstructor
@Service
public class SysUsersServiceImpl implements ISysUsersService {

    private final SysUsersMapper baseMapper;

    /**
     * 查询用户(客户的员工)
     */
    @Override
    public SysUsersVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询用户(客户的员工)列表
     */
    @Override
    public TableDataInfo<SysUsersVo> queryPageList(SysUsersBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysUsers> lqw = buildQueryWrapper(bo);
        Page<SysUsersVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询用户(客户的员工)列表
     */
    @Override
    public List<SysUsersVo> queryList(SysUsersBo bo) {
        LambdaQueryWrapper<SysUsers> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SysUsers> buildQueryWrapper(SysUsersBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SysUsers> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getName()), SysUsers::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getNameCh()), SysUsers::getNameCh, bo.getNameCh());
        lqw.eq(StringUtils.isNotBlank(bo.getRelationTel()), SysUsers::getRelationTel, bo.getRelationTel());
        lqw.eq(StringUtils.isNotBlank(bo.getType()), SysUsers::getType, bo.getType());
        lqw.eq(bo.getOrgId() != null, SysUsers::getOrgId, bo.getOrgId());
        lqw.eq(StringUtils.isNotBlank(bo.getPassword()), SysUsers::getPassword, bo.getPassword());
        lqw.eq(StringUtils.isNotBlank(bo.getPasswordSalt()), SysUsers::getPasswordSalt, bo.getPasswordSalt());
        lqw.eq(StringUtils.isNotBlank(bo.getFunctionList()), SysUsers::getFunctionList, bo.getFunctionList());
        lqw.eq(StringUtils.isNotBlank(bo.getRoleList()), SysUsers::getRoleList, bo.getRoleList());
        lqw.eq(StringUtils.isNotBlank(bo.getRemark()), SysUsers::getRemark, bo.getRemark());
        lqw.eq(bo.getCreator() != null, SysUsers::getCreator, bo.getCreator());
        lqw.eq(bo.getModifier() != null, SysUsers::getModifier, bo.getModifier());
        lqw.eq(bo.getModifyTime() != null, SysUsers::getModifyTime, bo.getModifyTime());
        lqw.eq(StringUtils.isNotBlank(bo.getIsValid()), SysUsers::getIsValid, bo.getIsValid());
        lqw.eq(StringUtils.isNotBlank(bo.getWxUserFlag()), SysUsers::getWxUserFlag, bo.getWxUserFlag());
        lqw.eq(StringUtils.isNotBlank(bo.getWxPhone()), SysUsers::getWxPhone, bo.getWxPhone());
        lqw.eq(StringUtils.isNotBlank(bo.getWxNo()), SysUsers::getWxNo, bo.getWxNo());
        lqw.eq(StringUtils.isNotBlank(bo.getWxEmail()), SysUsers::getWxEmail, bo.getWxEmail());
        lqw.eq(StringUtils.isNotBlank(bo.getWxFlag()), SysUsers::getWxFlag, bo.getWxFlag());
        lqw.eq(bo.getStealth() != null, SysUsers::getStealth, bo.getStealth());
        lqw.eq(StringUtils.isNotBlank(bo.getCustomerNo()), SysUsers::getCustomerNo, bo.getCustomerNo());
        lqw.eq(StringUtils.isNotBlank(bo.getOaAccount()), SysUsers::getOaAccount, bo.getOaAccount());
        lqw.eq(StringUtils.isNotBlank(bo.getEmail()), SysUsers::getEmail, bo.getEmail());
        lqw.eq(bo.getFirstLogin() != null, SysUsers::getFirstLogin, bo.getFirstLogin());
        lqw.eq(StringUtils.isNotBlank(bo.getIncludeType()), SysUsers::getIncludeType, bo.getIncludeType());
        lqw.eq(StringUtils.isNotBlank(bo.getUnicomType()), SysUsers::getUnicomType, bo.getUnicomType());
        lqw.eq(StringUtils.isNotBlank(bo.getUnicomType2()), SysUsers::getUnicomType2, bo.getUnicomType2());
        lqw.eq(StringUtils.isNotBlank(bo.getData()), SysUsers::getData, bo.getData());
        lqw.eq(StringUtils.isNotBlank(bo.getToken()), SysUsers::getToken, bo.getToken());
        lqw.eq(StringUtils.isNotBlank(bo.getMarkunits()), SysUsers::getMarkunits, bo.getMarkunits());
        lqw.eq(StringUtils.isNotBlank(bo.getDeveloperCodes()), SysUsers::getDeveloperCodes, bo.getDeveloperCodes());
        lqw.eq(StringUtils.isNotBlank(bo.getCardNumber()), SysUsers::getCardNumber, bo.getCardNumber());
        lqw.eq(StringUtils.isNotBlank(bo.getStaffType()), SysUsers::getStaffType, bo.getStaffType());
        lqw.eq(StringUtils.isNotBlank(bo.getUserRoleYx()), SysUsers::getUserRoleYx, bo.getUserRoleYx());
        lqw.eq(StringUtils.isNotBlank(bo.getZqbusinessFlag()), SysUsers::getZqbusinessFlag, bo.getZqbusinessFlag());
        lqw.eq(StringUtils.isNotBlank(bo.getRecSmsBeginTime()), SysUsers::getRecSmsBeginTime, bo.getRecSmsBeginTime());
        lqw.eq(StringUtils.isNotBlank(bo.getRecSmsEndTime()), SysUsers::getRecSmsEndTime, bo.getRecSmsEndTime());
        lqw.eq(StringUtils.isNotBlank(bo.getAssociatedDeveloper()), SysUsers::getAssociatedDeveloper, bo.getAssociatedDeveloper());
        lqw.eq(StringUtils.isNotBlank(bo.getDefaultDeveloper()), SysUsers::getDefaultDeveloper, bo.getDefaultDeveloper());
        lqw.eq(StringUtils.isNotBlank(bo.getCheckDeveloper()), SysUsers::getCheckDeveloper, bo.getCheckDeveloper());
        return lqw;
    }

    /**
     * 新增用户(客户的员工)
     */
    @Override
    public Boolean insertByBo(SysUsersBo bo) {
        SysUsers add = BeanUtil.toBean(bo, SysUsers.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改用户(客户的员工)
     */
    @Override
    public Boolean updateByBo(SysUsersBo bo) {
        SysUsers update = BeanUtil.toBean(bo, SysUsers.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SysUsers entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除用户(客户的员工)
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 查询手机号对应的用户
     */
    @Override
    public List<SysUsers> findList(PersonnelInfo personnelInfo) {
        if (personnelInfo == null || personnelInfo.getMobileTel() == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<SysUsers> wrapper = new LambdaQueryWrapper<SysUsers>()
            .eq(SysUsers::getName, personnelInfo.getMobileTel())
            .orderByDesc(SysUsers::getCreateTime)
            .last("LIMIT 1");

        return baseMapper.selectList(wrapper);
    }

    /**
     * 查询OA对应的用户
     */
    @Override
    public List<SysUsers> findOaList(PersonnelInfo personnelInfo) {
        if (personnelInfo == null || personnelInfo.getUserId() == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<SysUsers> wrapper = new LambdaQueryWrapper<SysUsers>()
            .eq(SysUsers::getOaAccount, personnelInfo.getUserId());

        return baseMapper.selectList(wrapper);
    }

    /**
     * 修改/保存用户
     */
    @Override
    public void saveOrUpdate(SysUsers sysUsers) {
        baseMapper.insertOrUpdate(sysUsers);
    }

    /**
     * 新增用户
     */
    @Override
    public void insertSysUser(SysUsers user) {
        baseMapper.insert(user);
    }

    /**
     * 查询长期未登录用户
     *
     * @param failureDays 超时时长
     * @return 长期未登录用户列表
     */
    @Override
    public List<SysUsers> selectFailureList(Integer failureDays) {
        return baseMapper.selectFailureList(failureDays);
    }

    @Override
    public List<SysUsers> selectLongTimeNoLoginByOa(Integer failureDays) {
        return baseMapper.selectLongTimeNoLoginByOa(failureDays);
    }
}
