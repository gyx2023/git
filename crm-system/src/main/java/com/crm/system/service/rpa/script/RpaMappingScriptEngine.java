package com.crm.system.service.rpa.script;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.springframework.stereotype.Component;

import java.util.Map;

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

    public void execute(RpaScriptExecutionContext context) {
        if (context == null || context.getScriptContent() == null) {
            return;
        }
        Class<? extends Script> scriptClass;
        try {
            scriptClass = loadScriptClass(context.getScriptContent());
        } catch (Exception ex) {
            log.error("RPA脚本编译失败 bizCode={} scriptCode={} scriptVersion={} mappingId={} formKey={} scriptContentLength={}",
                context.getBizCode(),
                context.getScriptCode(),
                context.getScriptVersion(),
                context.getMapping() == null ? null : context.getMapping().getId(),
                context.getMapping() == null ? null : context.getMapping().getFormKey(),
                context.getScriptContent().length(),
                ex);
            return;
        }
        try {
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
            log.error("RPA脚本执行失败 bizCode={} scriptCode={} scriptVersion={} mappingId={} formKey={} scriptContentLength={}",
                context.getBizCode(),
                context.getScriptCode(),
                context.getScriptVersion(),
                context.getMapping() == null ? null : context.getMapping().getId(),
                context.getMapping() == null ? null : context.getMapping().getFormKey(),
                context.getScriptContent().length(),
                ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Script> loadScriptClass(String scriptContent) {
        return (Class<? extends Script>) groovyClassLoader.parseClass(scriptContent);
    }

    private void applyResult(Map<?, ?> result, RpaScriptExecutionContext context) {
        Object pathObj = result.get("setPath");
        if (pathObj == null) {
            return;
        }
        helpers.setValueByPath(context.getTargetMap(), String.valueOf(pathObj), result.get("value"));
    }
}
