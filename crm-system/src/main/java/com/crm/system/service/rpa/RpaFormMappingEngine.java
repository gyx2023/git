package com.crm.system.service.rpa;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dao.SysParamDo;
import com.crm.system.domain.dao.rpa.ZqztRpaFormMappingDo;
import com.crm.system.domain.dao.rpa.ZqztRpaScriptDefDo;
import com.crm.system.domain.dao.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.mapper.param.SysParamMapper;
import com.crm.system.mapper.rpa.ZqztRpaScriptDefMapper;
import com.crm.system.mapper.servicehall.ZqztJtNoticeRecordMapper;
import com.crm.system.service.rpa.script.RpaMappingScriptEngine;
import com.crm.system.service.rpa.script.RpaScriptExecutionContext;
import com.crm.system.service.rpa.servicehall.NengKaiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.crm.system.utils.DealMapUtil.getValueByPath;
import static com.crm.system.utils.DealMapUtil.setValueByPath;
import static com.crm.system.utils.EvaluateUtil.evaluateCondition;

/**
 * 表单映射与脚本执行的公共引擎。
 * 核心逻辑直接来自生产代码（RabbitMQConsumerService.processMessage），一字不改。
 *
 * @author crm
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "rpa.form-mapping.cache.enabled", havingValue = "true", matchIfMissing = false)
public class RpaFormMappingEngine {

    private static final String SCRIPT_STAGE_ROW = "ROW";
    private static final String SCRIPT_STAGE_POST_ALL = "POST_ALL";

    @Resource
    private NengKaiService nengKaiService;

    @Resource
    private SysParamMapper sysParamMapper;

    @Resource
    private ZqztRpaScriptDefMapper zqztRpaScriptDefMapper;

    @Resource
    public RpaMappingScriptEngine rpaMappingScriptEngine;

    @Resource
    private RpaFormMappingCacheService rpaFormMappingCacheService;

    @Resource
    private ZqztJtNoticeRecordMapper zqztJtNoticeRecordMapper;

    // ── 便捷方法 ────────────────────────────────────────────────────────

    /**
     * 查询 RPA_DEAL_USER 系统参数并写入 recordDo
     */
    public void applyDealUser(ZqztJtNoticeRecordDo recordDo, String bizCode) {
        SysParamDo param = sysParamMapper.selectOne(
            new LambdaQueryWrapper<SysParamDo>()
                .eq(SysParamDo::getType, "RPA_DEAL_USER")
                .eq(SysParamDo::getCode, bizCode));
        if (param != null && param.getValue() != null) {
            recordDo.setDealResultCode(param.getValue());
        }
    }

    /**
     * 解析原始消息体为 Map
     */
    public Map<String, Object> parseRawMessage(String body) {
        cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(body);
        return convertJsonToMap(jsonObject);
    }

    /**
     * 将处理后的 messageMap 写回 recordDo 并调 nengKai 回调
     */
    public void callNengKaiCallback(ZqztJtNoticeRecordDo recordDo, Map<String, Object> messageMap) {
        recordDo.setBody(JSONUtil.toJsonStr(messageMap));
        String response = nengKaiService.worksheetsComplete(recordDo, "RPA-YGCF");
        recordDo.setResBody(response);
    }

    /**
     * 加载表单映射配置（使用缓存服务替代 mapper 直查）
     */
    public List<ZqztRpaFormMappingDo> loadFormMappings(String bizCode) {
        return rpaFormMappingCacheService.getByBizCode(bizCode);
    }

    // ── 完整流程（Standard 兜底策略直接用这个） ─────────────────────────

    /**
     * 标准处理全流程：普通表单 → 组件表单 → POST_ALL脚本 → NengKai回调
     * 逻辑与生产 RabbitMQConsumerService.processMessage 完全一致。
     */
    public void processAndCallback(ZqztJtNoticeRecordDo recordDo,
                                    List<ZqztRpaFormMappingDo> allMappings,
                                    Map<String, Object> targetMap) throws Exception {
        String bizCode = recordDo.getBizCode();
        Map<String, ZqztRpaScriptDefDo> scriptDefMap = loadScriptDefMap(allMappings);

        // 普通表单映射（从 targetMap 读、写 targetMap —— 同原代码）
        List<ZqztRpaFormMappingDo> formList = allMappings.stream()
            .filter(item -> "0".equals(item.getIsComponent()))
            .sorted(Comparator.comparing(ZqztRpaFormMappingDo::getId, Comparator.nullsLast(Long::compareTo)))
            .collect(Collectors.toList());
        for (ZqztRpaFormMappingDo item : formList) {
            if ("1".equals(item.getConditionsShow())) {
                String conditionsValue = item.getConditionsValue();
                boolean res = evaluateCondition(conditionsValue, targetMap);
                if (res) {
                    applyMappingValue(targetMap, item, targetMap);
                    executeMappingScriptIfNeeded(item, bizCode, scriptDefMap, targetMap, targetMap);
                }
            } else {
                applyMappingValue(targetMap, item, targetMap);
                executeMappingScriptIfNeeded(item, bizCode, scriptDefMap, targetMap, targetMap);
            }
        }

        // 组件表单映射
        List<ZqztRpaFormMappingDo> componentList = allMappings.stream()
            .filter(item -> "1".equals(item.getIsComponent()))
            .collect(Collectors.toList());
        Map<String, List<ZqztRpaFormMappingDo>> componentMap = componentList.stream()
            .collect(Collectors.groupingBy(ZqztRpaFormMappingDo::getComponentKey));
        for (Map.Entry<String, List<ZqztRpaFormMappingDo>> entry : componentMap.entrySet()) {
            List<ZqztRpaFormMappingDo> values = entry.getValue();
            if (values.size() == 1) {
                Map<String, Object> compMap = JSONUtil.toBean(values.get(0).getComponentTemplate(), Map.class);
                targetMap.put(entry.getKey(), compMap);
            } else {
                List<ZqztRpaFormMappingDo> sortedValues = values.stream()
                    .sorted(Comparator.comparing(ZqztRpaFormMappingDo::getId, Comparator.nullsLast(Long::compareTo)))
                    .collect(Collectors.toList());
                cn.hutool.json.JSONObject componentObject = JSONUtil.parseObj(sortedValues.get(0).getComponentTemplate());
                Map<String, Object> compMap = convertJsonToMap(componentObject);
                for (ZqztRpaFormMappingDo item : sortedValues) {
                    if ("1".equals(item.getConditionsShow())) {
                        String conditionsValue = item.getConditionsValue();
                        boolean res = evaluateCondition(conditionsValue, targetMap);
                        if (res) {
                            applyMappingValue(targetMap, item, compMap);
                            executeMappingScriptIfNeeded(item, bizCode, scriptDefMap, targetMap, compMap);
                        }
                        continue;
                    }
                    applyMappingValue(targetMap, item, compMap);
                    executeMappingScriptIfNeeded(item, bizCode, scriptDefMap, targetMap, compMap);
                }
                targetMap.put(entry.getKey(), compMap);
            }
        }

        // POST_ALL 脚本
        executePostAllScripts(allMappings, bizCode, scriptDefMap, targetMap);

        // NengKai 回调
        callNengKaiCallback(recordDo, targetMap);

        log.info("[{}] 消息处理成功", bizCode);
    }

    /**
     * 仅执行 POST_ALL 脚本（供多目标场景按需调用）
     */
    public void executePostAllOnly(List<ZqztRpaFormMappingDo> mappings,
                                    String bizCode,
                                    Map<String, ZqztRpaScriptDefDo> scriptDefMap,
                                    Map<String, Object> targetMap) {
        executePostAllScripts(mappings, bizCode, scriptDefMap, targetMap);
    }

    /**
     * 标准处理全流程：适用于默认兜底策略，与生产 RabbitMQConsumerService.processMessage 行为一致
     */
    public ZqztJtNoticeRecordDo processFullFlow(ZqztJtNoticeRecordDo recordDo) throws Exception {
        String bizCode = recordDo.getBizCode();
        String body = recordDo.getBody();

        // 1. 系统参数 & 校验
        applyDealUser(recordDo, bizCode);
        recordDo.setDealResultCode("0");

        // 2. 解析原始消息 & 加载表单映射
        Map<String, Object> messageMap = parseRawMessage(body);
        List<ZqztRpaFormMappingDo> allMappings = loadFormMappings(bizCode);
        if(CollectionUtils.isEmpty(allMappings)){
            log.info("RPA映射表查询为空，bizCode={}", bizCode);
        }

        // 3. 普通表单映射 → 脚本执行 → 组件表单映射 → 脚本执行
        processAndCallback(recordDo, allMappings, messageMap);

        return recordDo;
    }

    // ── 底层方法（直接从生产搬入，一字不改） ─────────────────────────────

    public Map<String, ZqztRpaScriptDefDo> loadScriptDefMap(List<ZqztRpaFormMappingDo> mappings) {
        Set<String> scriptCodes = mappings.stream()
            .filter(item -> "1".equals(item.getScriptEnabled()))
            .map(ZqztRpaFormMappingDo::getScriptCode)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toSet());
        if (scriptCodes.isEmpty()) {
            return new HashMap<>();
        }
        List<ZqztRpaScriptDefDo> defs = zqztRpaScriptDefMapper.selectList(new LambdaQueryWrapper<ZqztRpaScriptDefDo>()
            .in(ZqztRpaScriptDefDo::getScriptCode, scriptCodes)
            .eq(ZqztRpaScriptDefDo::getStatus, "1"));
        return defs.stream().collect(Collectors.toMap(ZqztRpaScriptDefDo::getScriptCode, item -> item, (left, right) -> right));
    }

    private void applyMappingValue(Map<String, Object> readSource, ZqztRpaFormMappingDo item, Map<String, Object> targetMap) {
        if (StringUtils.isEmpty(item.getSourceKey()) && StringUtils.isNotEmpty(item.getDefaultValue())) {
            Object finalValue = convertValueType(item.getValueType(), item.getDefaultValue());
            setValueByPath(targetMap, item.getFormKey(), finalValue);
            log.info("RPA映射赋值 bizCode={} scope={} mappingId={} targetKey={} sourceType=default defaultValue={} finalValue={}",
                item.getBizCode(), resolveScope(item), item.getId(), item.getFormKey(), item.getDefaultValue(), finalValue);
            return;
        }
        if (StringUtils.isEmpty(item.getSourceKey())) {
            log.info("RPA映射跳过 bizCode={} scope={} mappingId={} targetKey={} reason=sourceKey与defaultValue均为空",
                item.getBizCode(), resolveScope(item), item.getId(), item.getFormKey());
            return;
        }
        Object valueByPath = getValueByPath(readSource, item.getSourceKey());
        Object finalValue = convertValueType(item.getValueType(), valueByPath);
        setValueByPath(targetMap, item.getFormKey(), finalValue);
        log.info("RPA映射赋值 bizCode={} scope={} mappingId={} targetKey={} sourceType=sourceKey sourceKey={} sourceValue={} finalValue={}",
            item.getBizCode(), resolveScope(item), item.getId(), item.getFormKey(), item.getSourceKey(), valueByPath, finalValue);
    }

    private Object convertValueType(String valueType, Object sourceValue) {
        if (sourceValue == null) {
            return null;
        }
        if ("Integer".equals(valueType)) {
            if (sourceValue instanceof Number) {
                return ((Number) sourceValue).intValue();
            }
            return Integer.valueOf(String.valueOf(sourceValue));
        }
        if ("String".equals(valueType)) {
            return String.valueOf(sourceValue);
        }
        if ("BigDecimal".equals(valueType)) {
            if (sourceValue instanceof BigDecimal) {
                return sourceValue;
            }
            return new BigDecimal(String.valueOf(sourceValue));
        }
        return sourceValue;
    }

    private void executeMappingScriptIfNeeded(ZqztRpaFormMappingDo mapping,
                                              String bizCode,
                                              Map<String, ZqztRpaScriptDefDo> scriptDefMap,
                                              Map<String, Object> messageMap,
                                              Map<String, Object> targetMap) {
        if (!shouldExecuteAtStage(mapping, SCRIPT_STAGE_ROW)) {
            return;
        }
        if (!"1".equals(mapping.getScriptEnabled()) || StringUtils.isBlank(mapping.getScriptCode())) {
            return;
        }
        ZqztRpaScriptDefDo scriptDefDo = scriptDefMap.get(mapping.getScriptCode());
        if (scriptDefDo == null || StringUtils.isBlank(scriptDefDo.getScriptContent())) {
            return;
        }
        log.info("RPA脚本开始 bizCode={} scriptStage={} scope={} mappingId={} targetKey={} scriptCode={} scriptVersion={}",
            bizCode, resolveScriptStage(mapping), resolveScope(mapping), mapping.getId(), mapping.getFormKey(), scriptDefDo.getScriptCode(), scriptDefDo.getScriptVersion());
        rpaMappingScriptEngine.execute(RpaScriptExecutionContext.builder()
            .bizCode(bizCode)
            .scriptCode(scriptDefDo.getScriptCode())
            .scriptVersion(scriptDefDo.getScriptVersion())
            .scriptContent(scriptDefDo.getScriptContent())
            .messageMap(messageMap)
            .targetMap(targetMap)
            .mapping(mapping)
            .build());
        Object finalValue = getValueByPath(targetMap, mapping.getFormKey());
        log.info("RPA脚本结束 bizCode={} scriptStage={} scope={} mappingId={} targetKey={} scriptCode={} finalValue={}",
            bizCode, resolveScriptStage(mapping), resolveScope(mapping), mapping.getId(), mapping.getFormKey(), scriptDefDo.getScriptCode(), finalValue);
    }

    private void executePostAllScripts(List<ZqztRpaFormMappingDo> mappings,
                                      String bizCode,
                                      Map<String, ZqztRpaScriptDefDo> scriptDefMap,
                                      Map<String, Object> targetMap) {
        List<ZqztRpaFormMappingDo> postScripts = mappings.stream()
            .filter(item -> shouldExecuteAtStage(item, SCRIPT_STAGE_POST_ALL))
            .filter(item -> "1".equals(item.getScriptEnabled()))
            .filter(item -> StringUtils.isNotBlank(item.getScriptCode()))
            .sorted(Comparator.comparing(ZqztRpaFormMappingDo::getId, Comparator.nullsLast(Long::compareTo)))
            .collect(Collectors.toList());
        for (ZqztRpaFormMappingDo mapping : postScripts) {
            ZqztRpaScriptDefDo scriptDefDo = scriptDefMap.get(mapping.getScriptCode());
            if (scriptDefDo == null || StringUtils.isBlank(scriptDefDo.getScriptContent())) {
                continue;
            }
            log.info("RPA后置脚本开始 bizCode={} scriptStage={} mappingId={} scriptCode={} scriptVersion={}",
                bizCode, resolveScriptStage(mapping), mapping.getId(), scriptDefDo.getScriptCode(), scriptDefDo.getScriptVersion());
            rpaMappingScriptEngine.execute(RpaScriptExecutionContext.builder()
                .bizCode(bizCode)
                .scriptCode(scriptDefDo.getScriptCode())
                .scriptVersion(scriptDefDo.getScriptVersion())
                .scriptContent(scriptDefDo.getScriptContent())
                .messageMap(targetMap)
                .targetMap(targetMap)
                .mapping(mapping)
                .build());
            log.info("RPA后置脚本结束 bizCode={} scriptStage={} mappingId={} scriptCode={}",
                bizCode, resolveScriptStage(mapping), mapping.getId(), scriptDefDo.getScriptCode());
        }
    }

    private boolean shouldExecuteAtStage(ZqztRpaFormMappingDo mapping, String expectedStage) {
        String stage = resolveScriptStage(mapping);
        return expectedStage.equalsIgnoreCase(stage);
    }

    private String resolveScriptStage(ZqztRpaFormMappingDo mapping) {
        return StringUtils.isBlank(mapping.getScriptStage()) ? SCRIPT_STAGE_ROW : mapping.getScriptStage();
    }

    private String resolveScope(ZqztRpaFormMappingDo item) {
        return "1".equals(item.getIsComponent()) ? "component:" + item.getComponentKey() : "form";
    }

    private Map<String, Object> convertJsonToMap(cn.hutool.json.JSONObject jsonObject) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            resultMap.put(key, convertJsonValue(value));
        }
        return resultMap;
    }

    private Object convertJsonValue(Object value) {
        if (value instanceof cn.hutool.json.JSONObject) {
            return convertJsonToMap((cn.hutool.json.JSONObject) value);
        } else if (value instanceof cn.hutool.json.JSONArray) {
            cn.hutool.json.JSONArray jsonArray = (cn.hutool.json.JSONArray) value;
            List<Object> resultList = new ArrayList<>();
            for (Object item : jsonArray) {
                resultList.add(convertJsonValue(item));
            }
            return resultList;
        } else {
            return value;
        }
    }
}
