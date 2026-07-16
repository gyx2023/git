package com.crm.system.service.topp.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.topp.ToppBidSuboppoBo;
import com.crm.system.domain.dao.topp.ToppBidSuboppoDo;
import com.crm.system.domain.dao.topp.ToppOpportunityDo;
import com.crm.system.domain.vo.topp.ToppBidSuboppoVo;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import com.crm.system.mapper.topp.ToppBidSuboppoMapper;
import com.crm.system.mapper.topp.ToppOpportunityMapper;
import com.crm.system.service.topp.IToppBidSuboppoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商机标讯信息Service业务层处理
 *
 * @author crm
 * @date 2026-01-09
 */
@RequiredArgsConstructor
@Service
public class ToppBidSuboppoServiceImpl implements IToppBidSuboppoService {

    private final ToppBidSuboppoMapper baseMapper;

    private final ToppOpportunityMapper opportunityMapper;

    /**
     * 查询商机标讯信息
     */
    @Override
    public ToppBidSuboppoVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询商机标讯信息列表
     */
    @Override
    public TableDataInfo<ToppBidSuboppoVo> queryPageList(ToppBidSuboppoBo bo, PageQuery pageQuery) {
        // oppoCode 筛选：topp_bid_suboppo 表中没有 oppo_code 列，
        // 需要通过 topp_opportunity 关联查询
        Map<String, Object> params = bo.getParams();
        if (StringUtils.isNotBlank(bo.getOppoCode())) {
            // 先查符合 oppoCode 的 opportunity IDs
            LambdaQueryWrapper<ToppOpportunityDo> oppoQuery = Wrappers.lambdaQuery();
            oppoQuery.eq(ToppOpportunityDo::getOppCode, bo.getOppoCode());
            oppoQuery.select(ToppOpportunityDo::getId);
            List<ToppOpportunityVo> matchedOppos = opportunityMapper.selectVoList(oppoQuery);
            if (matchedOppos.isEmpty()) {
                // 无匹配商机，返回空结果
                return TableDataInfo.build(new Page<>());
            }
            // 用匹配到的 ID 过滤
            params.put("matchedProjectIds", matchedOppos.stream()
                .map(ToppOpportunityVo::getId).collect(Collectors.toList()));
        }
        LambdaQueryWrapper<ToppBidSuboppoDo> lqw = buildQueryWrapper(bo);
        Page<ToppBidSuboppoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        // 补充商机编号和商机名称
        enrichWithOpportunityInfo(result.getRecords());
        return TableDataInfo.build(result);
    }

    /**
     * 查询商机标讯信息列表
     */
    @Override
    public List<ToppBidSuboppoVo> queryList(ToppBidSuboppoBo bo) {
        LambdaQueryWrapper<ToppBidSuboppoDo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ToppBidSuboppoDo> buildQueryWrapper(ToppBidSuboppoBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ToppBidSuboppoDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getProjectId() != null, ToppBidSuboppoDo::getProjectId, bo.getProjectId());
        lqw.eq(StringUtils.isNotBlank(bo.getInfoId()), ToppBidSuboppoDo::getInfoId, bo.getInfoId());
        // oppoCode 筛选通过 matchedProjectIds 传参
        if (params.get("matchedProjectIds") != null) {
            lqw.in(ToppBidSuboppoDo::getProjectId, (List<?>) params.get("matchedProjectIds"));
        }
        lqw.eq(StringUtils.isNotBlank(bo.getBidTaskManagerId()), ToppBidSuboppoDo::getBidTaskManagerId, bo.getBidTaskManagerId());
        lqw.eq(StringUtils.isNotBlank(bo.getProjectCode()), ToppBidSuboppoDo::getProjectCode, bo.getProjectCode());
        lqw.eq(StringUtils.isNotBlank(bo.getColorBrand()), ToppBidSuboppoDo::getColorBrand, bo.getColorBrand());
        lqw.eq(bo.isValid(), ToppBidSuboppoDo::getValid, true);
        lqw.like(StringUtils.isNotBlank(bo.getProjectName()), ToppBidSuboppoDo::getProjectName, bo.getProjectName());
        // 发布日期范围查询
        if (StringUtils.isNotBlank((String) params.get("beginReleaseDate"))) {
            lqw.ge(ToppBidSuboppoDo::getReleaseDate, params.get("beginReleaseDate"));
        }
        if (StringUtils.isNotBlank((String) params.get("endReleaseDate"))) {
            lqw.le(ToppBidSuboppoDo::getReleaseDate, params.get("endReleaseDate"));
        }
        lqw.orderByDesc(ToppBidSuboppoDo::getCreateTime);
        return lqw;
    }

    /**
     * 补充商机编号和商机名称（从 topp_opportunity 关联查询）
     */
    private void enrichWithOpportunityInfo(List<ToppBidSuboppoVo> list) {
        if (list == null || list.isEmpty()) return;
        Set<Long> projectIds = list.stream()
            .map(ToppBidSuboppoVo::getProjectId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (projectIds.isEmpty()) return;
        List<ToppOpportunityVo> oppoList = opportunityMapper.selectVoBatchIds(projectIds);
        Map<Long, ToppOpportunityVo> oppoMap = oppoList.stream()
            .collect(Collectors.toMap(ToppOpportunityVo::getId, v -> v, (v1, v2) -> v1));
        for (ToppBidSuboppoVo vo : list) {
            ToppOpportunityVo oppo = oppoMap.get(vo.getProjectId());
            if (oppo != null) {
                vo.setOppoCode(oppo.getOppCode());
                vo.setOppoName(oppo.getOppName());
            }
        }
    }

    /**
     * 新增商机标讯信息
     */
    @Override
    public Boolean insertByBo(ToppBidSuboppoBo bo) {
        ToppBidSuboppoDo add = BeanUtil.toBean(bo, ToppBidSuboppoDo.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改商机标讯信息
     */
    @Override
    public Boolean updateByBo(ToppBidSuboppoBo bo) {
        ToppBidSuboppoDo update = BeanUtil.toBean(bo, ToppBidSuboppoDo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ToppBidSuboppoDo entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除商机标讯信息
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public void insertDoBatch(List<ToppBidSuboppoDo> list) {
        baseMapper.insertBatch( list);
    }
}
