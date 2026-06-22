package com.crm.system.service.sys;

import com.crm.system.domain.dao.SysParamDo;

import java.util.Map;

public interface ISysParamService {
    Map<String, String> findValueMapsByType(String type);

    SysParamDo findByCode(String code);

    /**
     * 根据参数编码获取参数值
     * @param code 参数编码
     * @return 参数值，不存在返回null
     */
    String findValueByCode(String code);
}
