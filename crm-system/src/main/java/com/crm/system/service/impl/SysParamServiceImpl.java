package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.system.domain.param.SysParamDo;
import com.crm.system.mapper.param.SysParamMapper;
import com.crm.system.service.ISysParamService;
import lombok.extern.slf4j.Slf4j;
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
}
