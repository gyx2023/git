package com.crm.system.service.rpa.script;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dao.SysParamDo;
import com.crm.system.mapper.param.SysParamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 脚本可用的sys_param只读查询能力
 */
@Component
@RequiredArgsConstructor
public class RpaScriptSysParamApi {

    private final SysParamMapper sysParamMapper;

    public String getValue(String type, String code) {
        if (StringUtils.isBlank(type) || StringUtils.isBlank(code)) {
            return null;
        }
        SysParamDo sysParamDo = sysParamMapper.selectOne(new LambdaQueryWrapper<SysParamDo>()
            .eq(SysParamDo::getType, type)
            .eq(SysParamDo::getCode, code));
        return sysParamDo == null ? null : sysParamDo.getValue();
    }
}
