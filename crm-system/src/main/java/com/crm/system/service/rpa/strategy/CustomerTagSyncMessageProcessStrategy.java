package com.crm.service.mq.ygcf.strategy;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.StringUtils;
import com.crm.service.mq.ygcf.RpaFormMappingEngine;
import com.crm.service.script.RpaScriptExecutionContext;
import com.crm.system.domain.dao.rpa.ZqztRpaFormMappingDo;
import com.crm.system.domain.dao.rpa.ZqztRpaScriptDefDo;
import com.crm.system.domain.dao.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.mapper.servicehall.ZqztJtNoticeRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.crm.utils.DealMapUtil.getValueByPath;
import static com.crm.utils.DealMapUtil.setValueByPath;
import static com.crm.utils.EvaluateUtil.evaluateCondition;

/**
 * 客户标签同步消息处理策略（bizCode = ZH01qu）
 * 独立编排：调用引擎提供的方法实现自己的流程。
 *   - 原始消息直接给能开回调，不做映射
 *   - 所有 mappings → syncData（用于 admin 标签同步）
 *
 * @author crm
 */
@Slf4j
@Component
public class CustomerTagSyncMessageProcessStrategy implements RpaMessageProcessStrategy {

    @Resource
    private RpaFormMappingEngine engine;

    @Resource
    private RestTemplate restTemplate;


    @Resource
    private ZqztJtNoticeRecordMapper zqztJtNoticeRecordMapper;

    @Value("${customer.admin.url:http://localhost:8080/system/customerTag/sync}")
    private String customerAdminUrl;

    @Override
    public String getBizCode() {
        return null; // 由 getBizCodes() 表达多 bizCode
    }

    @Override
    public List<String> getBizCodes() {
        return Collections.singletonList("ZH01qu");
    }

    @Override
    public ZqztJtNoticeRecordDo process(ZqztJtNoticeRecordDo recordDo) throws Exception {
        String bizCode = recordDo.getBizCode();
        String body = recordDo.getBody();

        // 1. 系统参数
        engine.applyDealUser(recordDo, bizCode);
        recordDo.setDealResultCode("0");

        // 2. 解析原始消息 & 加载表单映射
        Map<String, Object> rawMessage = engine.parseRawMessage(body);
        List<ZqztRpaFormMappingDo> allMappings = engine.loadFormMappings(bizCode);
        if(CollectionUtils.isEmpty(allMappings)){
            log.info("RPA映射表查询为空,bizCode={}", bizCode);
            throw new Exception("RPA映射表查询为空");
        }
        Map<String, ZqztRpaScriptDefDo> scriptDefMap = engine.loadScriptDefMap(allMappings);

        // 3. 普通表单映射到 syncData
        Map<String, Object> syncData = new LinkedHashMap<>();
        List<ZqztRpaFormMappingDo> formList = allMappings.stream()
            .filter(item -> "0".equals(item.getIsComponent()))
            .sorted(Comparator.comparing(ZqztRpaFormMappingDo::getId, Comparator.nullsLast(Long::compareTo)))
            .collect(Collectors.toList());
        applyFormMappings(rawMessage, syncData, formList, bizCode, scriptDefMap);

        // 4. 组件表单映射到 syncData
        applyComponentMappings(rawMessage, syncData, allMappings, bizCode, scriptDefMap);

        // 5. POST_ALL 脚本（作用于 syncData）
        engine.executePostAllOnly(allMappings, bizCode, scriptDefMap, syncData);

        // 6. 客户标签同步到 admin
        long noticeRecordId = recordDo.getId();
        boolean syncOk = syncCustomerTagAsync(bizCode, JSONUtil.toJsonStr(syncData), syncData, noticeRecordId);

        // 7. 能开回调：仅当 admin 同步成功时调用；失败则走重试队列，等重试成功后再调
        if (syncOk) {
            engine.callNengKaiCallback(recordDo, rawMessage);
            log.info("[{}] 消息处理成功: {}", bizCode, noticeRecordId);
        } else {
            log.info("[{}] admin同步暂存失败队列，等待重试: {}", bizCode, noticeRecordId);
        }

        return recordDo;
    }

    // ── 从生产代码搬入的核心方法 ──────────────────────────────────────────

    /**
     * 遍历普通表单列表，将映射结果写入 syncTarget（数据源为 dataSource）
     */
    private void applyFormMappings(Map<String, Object> dataSource, Map<String, Object> syncTarget,
                                    List<ZqztRpaFormMappingDo> formList,
                                    String bizCode, Map<String, ZqztRpaScriptDefDo> scriptDefMap) {
        for (ZqztRpaFormMappingDo item : formList) {
            if ("1".equals(item.getConditionsShow())) {
                String conditionsValue = item.getConditionsValue();
                boolean res = evaluateCondition(conditionsValue, dataSource);
                if (res) {
                    applyMappingTo(syncTarget, item, dataSource);
                    executeRowScript(item, bizCode, scriptDefMap, dataSource, syncTarget);
                }
            } else {
                applyMappingTo(syncTarget, item, dataSource);
                executeRowScript(item, bizCode, scriptDefMap, dataSource, syncTarget);
            }
        }
    }

    /**
     * 遍历组件列表，将映射结果写入 syncTarget
     */
    private void applyComponentMappings(Map<String, Object> dataSource, Map<String, Object> syncTarget,
                                         List<ZqztRpaFormMappingDo> allMappings,
                                         String bizCode, Map<String, ZqztRpaScriptDefDo> scriptDefMap) {
        List<ZqztRpaFormMappingDo> componentList = allMappings.stream()
            .filter(item -> "1".equals(item.getIsComponent()))
            .collect(Collectors.toList());
        Map<String, List<ZqztRpaFormMappingDo>> componentMap = componentList.stream()
            .collect(Collectors.groupingBy(ZqztRpaFormMappingDo::getComponentKey));
        for (Map.Entry<String, List<ZqztRpaFormMappingDo>> entry : componentMap.entrySet()) {
            List<ZqztRpaFormMappingDo> values = entry.getValue();
            if (values.size() == 1) {
                Map<String, Object> compMap = JSONUtil.toBean(values.get(0).getComponentTemplate(), Map.class);
                syncTarget.put(entry.getKey(), compMap);
            } else {
                List<ZqztRpaFormMappingDo> sortedValues = values.stream()
                    .sorted(Comparator.comparing(ZqztRpaFormMappingDo::getId, Comparator.nullsLast(Long::compareTo)))
                    .collect(Collectors.toList());
                cn.hutool.json.JSONObject componentObject = JSONUtil.parseObj(sortedValues.get(0).getComponentTemplate());
                Map<String, Object> compMap = convertJsonToMap(componentObject);
                for (ZqztRpaFormMappingDo item : sortedValues) {
                    if ("1".equals(item.getConditionsShow())) {
                        String conditionsValue = item.getConditionsValue();
                        boolean res = evaluateCondition(conditionsValue, dataSource);
                        if (res) {
                            applyMappingTo(compMap, item, dataSource);
                            executeRowScript(item, bizCode, scriptDefMap, dataSource, compMap);
                        }
                        continue;
                    }
                    applyMappingTo(compMap, item, dataSource);
                    executeRowScript(item, bizCode, scriptDefMap, dataSource, compMap);
                }
                syncTarget.put(entry.getKey(), compMap);
            }
        }
    }

    private void applyMappingTo(Map<String, Object> target, ZqztRpaFormMappingDo item, Map<String, Object> readSource) {
        if (StringUtils.isEmpty(item.getSourceKey()) && StringUtils.isNotEmpty(item.getDefaultValue())) {
            Object finalValue = convertValueType(item.getValueType(), item.getDefaultValue());
            setValueByPath(target, item.getFormKey(), finalValue);
            log.info("RPA映射赋值 bizCode={} scope={} mappingId={} targetKey={} sourceType=default defaultValue={} finalValue={}",
                item.getBizCode(), resolveScope(item), item.getId(), item.getFormKey(), item.getDefaultValue(), finalValue);
            return;
        }
        if (StringUtils.isEmpty(item.getSourceKey())) {
            log.info("RPA映射跳过 bizCode={} scope={} mappingId={} targetKey={} reason=sourceKey与defaultValue均为空",
                item.getBizCode(), resolveScope(item), item.getId(), item.getFormKey());
            return;
        }
        Object val = getValueByPath(readSource, item.getSourceKey());
        Object finalValue = convertValueType(item.getValueType(), val);
        setValueByPath(target, item.getFormKey(), finalValue);
        log.info("RPA映射赋值 bizCode={} scope={} mappingId={} targetKey={} sourceType=sourceKey sourceKey={} sourceValue={} finalValue={}",
            item.getBizCode(), resolveScope(item), item.getId(), item.getFormKey(), item.getSourceKey(), val, finalValue);
    }

    private void executeRowScript(ZqztRpaFormMappingDo mapping,
                                   String bizCode,
                                   Map<String, ZqztRpaScriptDefDo> scriptDefMap,
                                   Map<String, Object> messageMap,
                                   Map<String, Object> targetMap) {
        if (!shouldExecuteAtStage(mapping, "ROW")) {
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
        engine.rpaMappingScriptEngine.execute(RpaScriptExecutionContext.builder()
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

    private void executePostAllOnly(List<ZqztRpaFormMappingDo> mappings,
                                     String bizCode,
                                     Map<String, ZqztRpaScriptDefDo> scriptDefMap,
                                     Map<String, Object> targetMap) {
        List<ZqztRpaFormMappingDo> postScripts = mappings.stream()
            .filter(item -> shouldExecuteAtStage(item, "POST_ALL"))
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
            engine.rpaMappingScriptEngine.execute(RpaScriptExecutionContext.builder()
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

    // ── 工具方法（生产代码搬运） ──────────────────────────────────────────

    private Object convertValueType(String valueType, Object sourceValue) {
        if (sourceValue == null) return null;
        if ("Integer".equals(valueType)) {
            if (sourceValue instanceof Number) return ((Number) sourceValue).intValue();
            return Integer.valueOf(String.valueOf(sourceValue));
        }
        if ("String".equals(valueType)) return String.valueOf(sourceValue);
        if ("BigDecimal".equals(valueType)) {
            if (sourceValue instanceof BigDecimal) return sourceValue;
            return new BigDecimal(String.valueOf(sourceValue));
        }
        return sourceValue;
    }

    private Map<String, Object> convertJsonToMap(cn.hutool.json.JSONObject jsonObject) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            resultMap.put(entry.getKey(), convertJsonValue(entry.getValue()));
        }
        return resultMap;
    }

    private Object convertJsonValue(Object value) {
        if (value instanceof cn.hutool.json.JSONObject) return convertJsonToMap((cn.hutool.json.JSONObject) value);
        if (value instanceof cn.hutool.json.JSONArray) {
            cn.hutool.json.JSONArray arr = (cn.hutool.json.JSONArray) value;
            List<Object> list = new ArrayList<>();
            arr.forEach(item -> list.add(convertJsonValue(item)));
            return list;
        }
        return value;
    }

    private boolean shouldExecuteAtStage(ZqztRpaFormMappingDo mapping, String expectedStage) {
        String stage = resolveScriptStage(mapping);
        return expectedStage.equalsIgnoreCase(stage);
    }

    private String resolveScriptStage(ZqztRpaFormMappingDo mapping) {
        return StringUtils.isBlank(mapping.getScriptStage()) ? "ROW" : mapping.getScriptStage();
    }

    private String resolveScope(ZqztRpaFormMappingDo item) {
        return "1".equals(item.getIsComponent()) ? "component:" + item.getComponentKey() : "form";
    }

    // ── admin 调用 ──────────────────────────────────────────────────────

    /**
     * @return true=admin 同步成功，false=存入重试队列
     */
    private boolean syncCustomerTagAsync(String bizCode, String syncDataJson, Map<String, Object> syncData, long noticeRecordId) {
        if (StringUtils.isBlank(bizCode) || StringUtils.isBlank(syncDataJson)) return false;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(syncData, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                customerAdminUrl, HttpMethod.POST, httpEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                cn.hutool.json.JSONObject resp = JSONUtil.parseObj(response.getBody());
                String code = String.valueOf(resp.get("code"));
                if ("200".equals(code) || "0".equals(code)) {
                    log.info("[客户标签同步] admin调用成功: bizCode={}", bizCode);
                    return true;
                }
            }

            log.warn("[客户标签同步] admin返回失败: bizCode={}, response={}", bizCode, response.getBody());
        } catch (Exception e) {
            log.error("[客户标签同步] admin调用异常: bizCode={}, error={}", bizCode, e.getMessage(), e);
        }
        return false;
    }
}
