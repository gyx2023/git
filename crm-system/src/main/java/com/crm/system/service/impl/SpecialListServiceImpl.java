package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.exception.ServiceException;
import com.crm.common.utils.StringUtils;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.system.domain.bo.SpecialListBo;
import com.crm.system.domain.dao.SpecialListDo;
import com.crm.system.domain.vo.SpecialListVo;
import com.crm.system.mapper.SpecialListMapper;
import com.crm.system.mapper.sys.SysUserMapper;
import com.crm.system.service.ISpecialListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 特殊名单服务实现
 *
 * @author crm
 * @date 2026-06-16
 */
@Slf4j
@Service
public class SpecialListServiceImpl extends ServiceImpl<SpecialListMapper, SpecialListDo> implements ISpecialListService {

    @Resource
    private SysUserMapper sysUserMapper;


    /**
     * 分页查询特殊名单列表
     */
    @Override
    public TableDataInfo<SpecialListVo> queryPageList(SpecialListBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SpecialListDo> lqw = buildQueryWrapper(bo);
        lqw.orderByDesc(SpecialListDo::getCreateTime);
        Page<SpecialListVo> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        page = baseMapper.selectPageList(page, lqw);

        List<SpecialListVo> voList = page.getRecords();
        if (CollUtil.isNotEmpty(voList)) {
            // 收集创建人和更新人ID
            Set<Integer> userIds = new HashSet<>();
            voList.forEach(v -> {
                if (v.getCreateBy() != null) {
                    userIds.add(v.getCreateBy());
                }
                if (v.getUpdateBy() != null) {
                    userIds.add(v.getUpdateBy());
                }
            });

            // 批量查询用户名称
            if (!userIds.isEmpty()) {
                List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
                Map<Long, String> userNameMap = users.stream()
                    .collect(Collectors.toMap(SysUser::getUserId, SysUser::getNickName, (a, b) -> b));

                voList.forEach(v -> {
                    if (v.getCreateBy() != null && userNameMap.containsKey(v.getCreateBy().longValue())) {
                        v.setCreateByName(userNameMap.get(v.getCreateBy().longValue()));
                    }
                    if (v.getUpdateBy() != null && userNameMap.containsKey(v.getUpdateBy().longValue())) {
                        v.setUpdateByName(userNameMap.get(v.getUpdateBy().longValue()));
                    }
                });
            }
        }

        TableDataInfo<SpecialListVo> result = new TableDataInfo<>(voList, page.getTotal());
        result.setCode(HttpStatus.HTTP_OK);
        result.setMsg("查询成功");
        return result;
    }

    /**
     * 新增特殊名单
     */
    @Override
    public Boolean insertByBo(SpecialListBo bo) {
        if (checkDuplicate(bo.getSceneCode(), bo.getTargetValue(), null)) {
            throw new ServiceException("记录已存在");
        }
        SpecialListDo addDo = BeanUtil.toBean(bo, SpecialListDo.class);
        Date now = new Date();
        Integer userId = LoginHelper.getUserId().intValue();
        addDo.setCreateBy(userId);
        addDo.setCreateTime(now);
        addDo.setUpdateBy(userId);
        addDo.setUpdateTime(now);
        addDo.setStatus("1");
        return baseMapper.insert(addDo) > 0;
    }

    /**
     * 修改特殊名单（只更新状态、生效时间、失效时间、原因）
     */
    @Override
    public Boolean updateByBo(SpecialListBo bo) {
        SpecialListDo existing = baseMapper.selectById(bo.getId());
        if (existing == null) {
            throw new ServiceException("记录不存在");
        }

        UpdateWrapper<SpecialListDo> uw = Wrappers.update();
        uw.eq("id", bo.getId());
        uw.set(StringUtils.isNotBlank(bo.getStatus()), "status", bo.getStatus());
        uw.set(bo.getEffectiveTime() != null, "effective_time", bo.getEffectiveTime());
        uw.set(bo.getExpireTime() != null, "expire_time", bo.getExpireTime());
        uw.set(StringUtils.isNotBlank(bo.getReason()), "reason", bo.getReason());
        uw.set("update_by", LoginHelper.getUserId().intValue());
        uw.set("update_time", new Date());
        return baseMapper.update(null, uw) > 0;
    }

    /**
     * 批量删除（逻辑删除，状态置为0）
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Integer> ids) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        for (Integer id : ids) {
            SpecialListDo existing = baseMapper.selectById(id);
            if (existing != null && !"0".equals(existing.getStatus())) {
                UpdateWrapper<SpecialListDo> uw = Wrappers.update();
                uw.eq("id", id).set("status", "0").set("update_by", LoginHelper.getUserId().intValue()).set("update_time", new Date());
                baseMapper.update(null, uw);
            }
        }
        return true;
    }

    /**
     * 检查重复
     */
    @Override
    public boolean checkDuplicate(String sceneCode, String targetValue, Integer excludeId) {
        LambdaQueryWrapper<SpecialListDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(SpecialListDo::getSceneCode, sceneCode)
            .eq(SpecialListDo::getTargetValue, targetValue);
        if (excludeId != null) {
            lqw.ne(SpecialListDo::getId, excludeId);
        }
        return baseMapper.selectCount(lqw) > 0;
    }

    /**
     * 批量插入
     */
    @Override
    public void batchInsert(List<SpecialListDo> list) {
        baseMapper.insertBatch(list, 100);
    }

    /**
     * 批量更新
     */
    @Override
    public void batchUpdate(List<SpecialListDo> list) {
        baseMapper.updateBatchById(list, 100);
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<SpecialListDo> buildQueryWrapper(SpecialListBo bo) {
        LambdaQueryWrapper<SpecialListDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getSceneCode()), SpecialListDo::getSceneCode, bo.getSceneCode());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SpecialListDo::getStatus, bo.getStatus());
        lqw.like(StringUtils.isNotBlank(bo.getTargetValue()), SpecialListDo::getTargetValue, bo.getTargetValue());
        lqw.eq(bo.getCreateBy() != null, SpecialListDo::getCreateBy, bo.getCreateBy());
        lqw.ge(bo.getEffectiveTimeStart() != null, SpecialListDo::getEffectiveTime, bo.getEffectiveTimeStart());
        lqw.le(bo.getEffectiveTimeEnd() != null, SpecialListDo::getEffectiveTime, bo.getEffectiveTimeEnd());
        lqw.ge(bo.getCreateTimeStart() != null, SpecialListDo::getCreateTime, bo.getCreateTimeStart());
        lqw.le(bo.getCreateTimeEnd() != null, SpecialListDo::getCreateTime, bo.getCreateTimeEnd());

        // 如果传入了ids集合，按ID范围查询
        if (CollUtil.isNotEmpty(bo.getIds())) {
            lqw.in(SpecialListDo::getId, bo.getIds());
        }

        return lqw;
    }


}
