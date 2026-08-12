package com.crm.system.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.constant.CacheNames;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.exception.ServiceException;
import com.crm.common.utils.StringUtils;
import com.crm.common.utils.redis.CacheUtils;
import com.crm.system.domain.dao.SysParamDo;
import com.crm.system.mapper.param.SysParamMapper;
import com.crm.system.service.sys.ISysParamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SysParamServiceImpl implements ISysParamService {
    @Resource
    private SysParamMapper sysParamMapper;

    @Override
    @Cacheable(cacheNames = CacheNames.SYS_PARAM_CACHE, key = "'type:' + #type")
    public Map<String, String> findValueMapsByType(String type) {
        List<SysParamDo> list = sysParamMapper.selectList(new LambdaQueryWrapper<SysParamDo>().eq(SysParamDo::getType, type));
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new HashMap<>();
        for (SysParamDo param : list) {
            result.put(param.getCode(), param.getValue());
        }
        return result;
    }

    @Override
    @Cacheable(cacheNames = CacheNames.SYS_PARAM_CACHE, key = "'code:' + #code")
    public SysParamDo findByCode(String code) {
        return sysParamMapper.selectVoOne(new LambdaQueryWrapper<SysParamDo>().eq(SysParamDo::getCode, code));
    }

    @Override
    public String findValueByCode(String code) {
        SysParamDo param = findByCode(code);
        return param != null ? param.getValue() : null;
    }

    @Override
    public SysParamDo findValueByTypeCode(String type, String code) {
        List<SysParamDo> list = sysParamMapper.selectList(
            new LambdaQueryWrapper<SysParamDo>()
                .eq(SysParamDo::getType, type)
                .eq(SysParamDo::getCode, code));
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public TableDataInfo<SysParamDo> selectPageParamList(SysParamDo param, PageQuery pageQuery) {
        LambdaQueryWrapper<SysParamDo> lqw = new LambdaQueryWrapper<SysParamDo>()
            .like(StringUtils.isNotBlank(param.getCode()), SysParamDo::getCode, param.getCode())
            .like(StringUtils.isNotBlank(param.getName()), SysParamDo::getName, param.getName())
            .like(StringUtils.isNotBlank(param.getType()), SysParamDo::getType, param.getType())
            .eq(StringUtils.isNotBlank(param.getIsSystem()), SysParamDo::getIsSystem, param.getIsSystem());
        Page<SysParamDo> page = sysParamMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public List<SysParamDo> selectParamList(SysParamDo param) {
        LambdaQueryWrapper<SysParamDo> lqw = new LambdaQueryWrapper<SysParamDo>()
            .like(StringUtils.isNotBlank(param.getCode()), SysParamDo::getCode, param.getCode())
            .like(StringUtils.isNotBlank(param.getName()), SysParamDo::getName, param.getName())
            .like(StringUtils.isNotBlank(param.getType()), SysParamDo::getType, param.getType())
            .eq(StringUtils.isNotBlank(param.getIsSystem()), SysParamDo::getIsSystem, param.getIsSystem());
        return sysParamMapper.selectList(lqw);
    }

    @Override
    public SysParamDo selectParamById(Long id) {
        return sysParamMapper.selectById(id);
    }

    @Override
    public int insertParam(SysParamDo param) {
        int row = sysParamMapper.insert(param);
        if (row > 0) {
            // 更新 code 维度缓存
            CacheUtils.put(CacheNames.SYS_PARAM_CACHE, "code:" + param.getCode(), param);
            // 清除 type 维度缓存，下次查询时重新加载
            if (StringUtils.isNotBlank(param.getType())) {
                CacheUtils.evict(CacheNames.SYS_PARAM_CACHE, "type:" + param.getType());
            }
        }
        return row;
    }

    @Override
    public int updateParam(SysParamDo param) {
        SysParamDo oldParam = sysParamMapper.selectById(param.getId());
        if (oldParam == null) {
            throw new ServiceException("配置不存在");
        }

        int row = sysParamMapper.updateById(param);
        if (row > 0) {
            // 如果 code 变更，清除旧的 code 缓存
            if (!StringUtils.equals(oldParam.getCode(), param.getCode())) {
                CacheUtils.evict(CacheNames.SYS_PARAM_CACHE, "code:" + oldParam.getCode());
            }
            // 更新 code 维度缓存
            CacheUtils.put(CacheNames.SYS_PARAM_CACHE, "code:" + param.getCode(), param);

            // 清除 type 维度缓存
            if (StringUtils.isNotBlank(oldParam.getType())) {
                CacheUtils.evict(CacheNames.SYS_PARAM_CACHE, "type:" + oldParam.getType());
            }
            if (StringUtils.isNotBlank(param.getType()) && !StringUtils.equals(oldParam.getType(), param.getType())) {
                CacheUtils.evict(CacheNames.SYS_PARAM_CACHE, "type:" + param.getType());
            }
        }
        return row;
    }

    @Override
    public void deleteParamByIds(Long[] ids) {
        for (Long id : ids) {
            SysParamDo param = sysParamMapper.selectById(id);
            if (param == null) {
                continue;
            }
            // 系统内置参数禁止删除
            if ("1".equals(param.getIsSystem())) {
                throw new ServiceException(String.format("内置配置【%1$s】不能删除", param.getCode()));
            }
            // 清除缓存
            CacheUtils.evict(CacheNames.SYS_PARAM_CACHE, "code:" + param.getCode());
            if (StringUtils.isNotBlank(param.getType())) {
                CacheUtils.evict(CacheNames.SYS_PARAM_CACHE, "type:" + param.getType());
            }
        }
        sysParamMapper.deleteBatchIds(Arrays.asList(ids));
    }

    @Override
    public void resetParamCache() {
        clearParamCache();
        loadingParamCache();
    }

    /**
     * 加载配置缓存数据
     */
    public void loadingParamCache() {
        List<SysParamDo> list = sysParamMapper.selectList(null);
        // 按 code 维度加载
        for (SysParamDo param : list) {
            CacheUtils.put(CacheNames.SYS_PARAM_CACHE, "code:" + param.getCode(), param);
        }
        // 按 type 维度加载
        Map<String, Map<String, String>> typeMap = new HashMap<>();
        for (SysParamDo param : list) {
            if (StringUtils.isNotBlank(param.getType())) {
                typeMap.computeIfAbsent(param.getType(), k -> new HashMap<>())
                    .put(param.getCode(), param.getValue());
            }
        }
        for (Map.Entry<String, Map<String, String>> entry : typeMap.entrySet()) {
            CacheUtils.put(CacheNames.SYS_PARAM_CACHE, "type:" + entry.getKey(), entry.getValue());
        }
    }

    /**
     * 清空配置缓存数据
     */
    public void clearParamCache() {
        CacheUtils.clear(CacheNames.SYS_PARAM_CACHE);
    }
}
