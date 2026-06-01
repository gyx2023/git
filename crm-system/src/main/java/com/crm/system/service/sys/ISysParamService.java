package com.crm.system.service.sys;

import com.crm.system.domain.param.SysParamDo;

import java.util.Map;

public interface ISysParamService {
    Map<String, String> findValueMapsByType(String type);

    SysParamDo findByCode(String code);
}
