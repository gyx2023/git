package com.crm.system.service.rpa.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.system.domain.rpa.ZqztRpaFormMappingDo;
import com.crm.system.mapper.rpa.ZqztRpaFormMappingMapper;
import com.crm.system.service.rpa.IZqztRpaFormMappingService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * rpa单映射Service业务层处理
 *
 * @author crm
 * @date 2026-01-06
 */
@RequiredArgsConstructor
@Service
public class ZqztRpaFormMappingServiceImpl implements IZqztRpaFormMappingService {

    @Resource
    private ZqztRpaFormMappingMapper zqztRpaFormMappingMapper;

    @Override
    public Map<String, Object> formFilling(Map<String, Object> body, String bizCode, String taskCode) {
        List<ZqztRpaFormMappingDo> zqztRpaFormMappingDos = zqztRpaFormMappingMapper.selectList(new LambdaQueryWrapper<ZqztRpaFormMappingDo>()
            .eq(ZqztRpaFormMappingDo::getBizCode, bizCode)
            .eq(ZqztRpaFormMappingDo::getTaskCode, taskCode));
        zqztRpaFormMappingDos.forEach( zqztRpaFormMappingDo -> {
            //从body中获取到key为sourceKey的值
            Object valueByKey = this.findValueByKey(body, zqztRpaFormMappingDo.getSourceKey());
            //将获取到的值put到body中，key为formKey
            if(valueByKey !=null){
                this.setValueByFormKey(body, zqztRpaFormMappingDo.getFormKey(), valueByKey);
            }
        });
        return body;
    }

    /**
     * 设置值到嵌套Map中指定key的位置
     * @param target 目标Map（支持多层嵌套）
     * @param formKey 要设置值的formKey
     * @param value 要设置的值
     */
    public void setValueByFormKey(Map<String, Object> target, String formKey, Object value) {
        if (target == null || formKey == null || formKey.trim().isEmpty()) {
            return;
        }

        // 遍历当前层级的Map
        for (Map.Entry<String, Object> entry : target.entrySet()) {
            String currentKey = entry.getKey();
            Object currentValue = entry.getValue();

            // 1. 当前层级找到目标key，直接设置value
            if (formKey.equals(currentKey)) {
                entry.setValue(value);
                return;
            }

            // 2. 当前value是Map，递归设置下一层
            if (currentValue instanceof Map) {
                setValueByFormKey((Map<String, Object>) currentValue, formKey, value);
            }

            // 3. 当前value是List，遍历List中的元素（支持List嵌套Map）
            if (currentValue instanceof List) {
                setValueInList((List<?>) currentValue, formKey, value);
            }
        }

        // 如果在所有层级都没找到formKey，直接在顶层Map中put
        target.put(formKey, value);
    }

    /**
     * 深度检查Map中是否包含指定key
     */
    private boolean containsKeyDeep(Map<String, Object> target, String formKey) {
        if (target.containsKey(formKey)) {
            return true;
        }

        for (Object value : target.values()) {
            if (value instanceof Map) {
                if (containsKeyDeep((Map<String, Object>) value, formKey)) {
                    return true;
                }
            } else if (value instanceof List) {
                if (containsKeyInList((List<?>) value, formKey)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 在List中深度检查是否包含指定key
     */
    private boolean containsKeyInList(List<?> list, String formKey) {
        if (CollectionUtils.isEmpty(list)) {
            return false;
        }

        for (Object item : list) {
            if (item instanceof Map) {
                if (containsKeyDeep((Map<String, Object>) item, formKey)) {
                    return true;
                }
            } else if (item instanceof List) {
                if (containsKeyInList((List<?>) item, formKey)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 辅助方法：在List中设置值到嵌套Map的目标key
     */
    private void setValueInList(List<?> list, String formKey, Object value) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        for (Object item : list) {
            // List中的元素是Map，继续递归设置
            if (item instanceof Map) {
                setValueByFormKey((Map<String, Object>) item, formKey, value);
            }
            // List中的元素是List（多层List嵌套），继续遍历
            else if (item instanceof List) {
                setValueInList((List<?>) item, formKey, value);
            }
        }
    }

    /**
     * 核心方法：从嵌套Map中查找指定key的value
     * @param source 源Map（支持多层嵌套）
     * @param targetKey 要查找的目标key
     * @return 找到的value，未找到返回null
     */
    public Object findValueByKey(Map<String, Object> source, String targetKey) {
        // 空值校验
        if (source==null || targetKey == null || targetKey.trim().isEmpty()) {
            return null;
        }

        // 遍历当前层级的Map
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String currentKey = entry.getKey();
            Object currentValue = entry.getValue();

            // 1. 当前层级找到目标key，直接返回value
            if (targetKey.equals(currentKey)) {
                return currentValue;
            }

            // 2. 当前value是Map，递归查找下一层
            if (currentValue instanceof Map) {
                Object result = findValueByKey((Map<String, Object>) currentValue, targetKey);
                if (result != null) {
                    return result;
                }
            }

            // 3. 当前value是List，遍历List中的元素（支持List嵌套Map）
            if (currentValue instanceof List) {
                Object result = findValueInList((List<?>) currentValue, targetKey);
                if (result != null) {
                    return result;
                }
            }
        }

        // 所有层级都没找到
        return null;
    }

    /**
     * 辅助方法：遍历List，查找其中嵌套Map的目标key
     */
    private Object findValueInList(List<?> list, String targetKey) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        for (Object item : list) {
            // List中的元素是Map，继续递归查找
            if (item instanceof Map) {
                Object result = findValueByKey((Map<String, Object>) item, targetKey);
                if (result != null) {
                    return result;
                }
            }
            // List中的元素是List（多层List嵌套），继续遍历
            else if (item instanceof List) {
                Object result = findValueInList((List<?>) item, targetKey);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
