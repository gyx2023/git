package com.crm.system.service.topp.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.topp.ToppMemberBo;
import com.crm.system.domain.dao.topp.ToppMemberDo;
import com.crm.system.domain.vo.topp.ToppMemberVo;
import com.crm.system.mapper.topp.ToppMemberMapper;
import com.crm.system.service.topp.IToppMemberService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author crm
 * @date 2025-12-02
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class IToppMemberServiceImpl implements IToppMemberService {
    @Resource
    private ToppMemberMapper baseMapper;

    /**
     * 查询【请填写功能名称】
     */
    @Override
    public ToppMemberVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public TableDataInfo<ToppMemberVo> queryPageList(ToppMemberBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ToppMemberDo> lqw = buildQueryWrapper(bo);
        Page<ToppMemberVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<ToppMemberVo> queryList(ToppMemberBo bo) {
        LambdaQueryWrapper<ToppMemberDo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ToppMemberDo> buildQueryWrapper(ToppMemberBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ToppMemberDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getProjectId() != null, ToppMemberDo::getProjectId, bo.getProjectId());
        lqw.eq(StringUtils.isNotBlank(bo.getMemberRole()), ToppMemberDo::getMemberRole, bo.getMemberRole());
        lqw.eq(bo.getMemberId() != null, ToppMemberDo::getMemberId, bo.getMemberId());
        lqw.eq(bo.getMemberArea() != null, ToppMemberDo::getMemberArea, bo.getMemberArea());
        lqw.eq(bo.getMemberOrg() != null, ToppMemberDo::getMemberOrg, bo.getMemberOrg());
        lqw.eq(StringUtils.isNotBlank(bo.getName()), ToppMemberDo::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getRoleDesc()), ToppMemberDo::getRoleDesc, bo.getRoleDesc());
        lqw.eq(bo.getCreator() != null, ToppMemberDo::getCreator, bo.getCreator());
        lqw.eq(StringUtils.isNotBlank(bo.getMemo()), ToppMemberDo::getMemo, bo.getMemo());
        lqw.eq(StringUtils.isNotBlank(bo.getScore()), ToppMemberDo::getScore, bo.getScore());
        lqw.eq(StringUtils.isNotBlank(bo.getScoreExplain()), ToppMemberDo::getScoreExplain, bo.getScoreExplain());
        lqw.eq(bo.getScoreTime() != null, ToppMemberDo::getScoreTime, bo.getScoreTime());
        lqw.eq(StringUtils.isNotBlank(bo.getContri()), ToppMemberDo::getContri, bo.getContri());
        lqw.eq(StringUtils.isNotBlank(bo.getTeamLeaderFlag()), ToppMemberDo::getTeamLeaderFlag, bo.getTeamLeaderFlag());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(ToppMemberBo bo) {
        ToppMemberDo add = BeanUtil.toBean(bo, ToppMemberDo.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改【请填写功能名称】
     */
    @Override
    public Boolean updateByBo(ToppMemberBo bo) {
        ToppMemberDo update = BeanUtil.toBean(bo, ToppMemberDo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ToppMemberDo entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除【请填写功能名称】
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
            log.debug("deleteWithValidByIds: isValid=true, 待补充业务校验逻辑");
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
