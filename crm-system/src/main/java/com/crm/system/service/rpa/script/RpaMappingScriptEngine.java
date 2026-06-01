package com.crm.service.script;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * mapping行级脚本执行引擎
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RpaMappingScriptEngine {

    private final RpaScriptSysParamApi sysParamApi;

    private final RpaScriptHelpers helpers = new RpaScriptHelpers();

    private final GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

    private final Map<String, Class<? extends Script>> scriptCache = new ConcurrentHashMap<>();

    public void execute(RpaScriptExecutionContext context) {
        if (context == null || context.getScriptContent() == null) {
            return;
        }
        try {
            Class<? extends Script> scriptClass = loadScriptClass(context.getScriptCode(), context.getScriptVersion(), context.getScriptContent());
            Binding binding = new Binding();
            binding.setVariable("messageMap", context.getMessageMap());
            binding.setVariable("targetMap", context.getTargetMap());
            binding.setVariable("mapping", context.getMapping());
            binding.setVariable("bizCode", context.getBizCode());
            binding.setVariable("helpers", helpers);
            binding.setVariable("sysParamApi", sysParamApi);
            binding.setVariable("log", log);
            Script script = InvokerHelper.createScript(scriptClass, binding);
            Object result = script.run();
            if (result instanceof Map) {
                applyResult((Map<?, ?>) result, context);
            }
        } catch (Exception ex) {
            log.error("RPA脚本执行失败 bizCode={} scriptCode={} mappingId={} formKey={}",
                context.getBizCode(),
                context.getScriptCode(),
                context.getMapping() == null ? null : context.getMapping().getId(),
                context.getMapping() == null ? null : context.getMapping().getFormKey(),
                ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Script> loadScriptClass(String scriptCode, String scriptVersion, String scriptContent) {
        String cacheKey = scriptCode + ":" + scriptVersion;
        return scriptCache.computeIfAbsent(cacheKey, key -> (Class<? extends Script>) groovyClassLoader.parseClass(scriptContent));
    }

    private void applyResult(Map<?, ?> result, RpaScriptExecutionContext context) {
        Object pathObj = result.get("setPath");
        if (pathObj == null) {
            return;
        }
        helpers.setValueByPath(context.getTargetMap(), String.valueOf(pathObj), result.get("value"));
    }
}
