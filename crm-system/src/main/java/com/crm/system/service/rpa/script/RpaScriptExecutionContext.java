package com.crm.service.script;

import com.crm.system.domain.dao.rpa.ZqztRpaFormMappingDo;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 行级脚本执行上下文
 */
@Data
@Builder
public class RpaScriptExecutionContext {

    private String bizCode;

    private String scriptCode;

    private String scriptContent;

    private String scriptVersion;

    private Map<String, Object> messageMap;

    private Map<String, Object> targetMap;

    private ZqztRpaFormMappingDo mapping;
}
