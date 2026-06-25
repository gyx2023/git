package com.crm.system.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.constant.CacheNames;
import com.crm.system.domain.dao.SysParamDo;
import com.crm.system.mapper.param.SysParamMapper;
import com.crm.system.service.sys.ISysParamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    public SysParamDo findValueByTypeCode(String type, String code){
        List<SysParamDo> list = sysParamMapper.selectList(
            new LambdaQueryWrapper<SysParamDo>()
                .eq(SysParamDo::getType, type)
                .eq(SysParamDo::getCode, code));
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
}
