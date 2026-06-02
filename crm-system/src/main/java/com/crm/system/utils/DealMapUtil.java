package com.crm.system.utils;

import java.util.Map;

public class DealMapUtil {

    // 根据路径从Map中获取值
// 根据路径从Map中获取值（支持多个路径选项，用|分隔）
    public static Object getValueByPath(Map<String, Object> map, String path) {
        // 按竖线分割多个路径选项
        String[] pathOptions = path.split("\\|");

        for (String singlePath : pathOptions) {
            Object result = getSingleValueByPath(map, singlePath.trim());
            if (result != null) {
                return result; // 找到值就立即返回
            }
        }

        return null; // 所有路径都找不到值则返回null
    }

    // 获取单个路径的值
    private static Object getSingleValueByPath(Map<String, Object> map, String path) {
        String[] keys = path.split("\\.");
        Object current = map;

        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(key);
                if (current == null) {
                    return null; // 路径不存在
                }
            } else {
                return null; // 中间节点不是Map类型
            }
        }

        return current;
    }


    // 在Map中按路径设置值（自动创建缺失的层级）
    public static void setValueByPath(Map<String, Object> map, String path, Object value) {
        String[] keys = path.split("\\.");
        Map<String, Object> current = map;

        // 导航到目标层级的父级
        for (int i = 0; i < keys.length - 1; i++) {
            String key = keys[i];
            Object next = current.get(key);

            if (!(next instanceof Map)) {
                // 如果路径不存在，创建新的Map
                next = new java.util.HashMap<String, Object>();
                current.put(key, next);
            }
            current = (Map<String, Object>) next;
        }

        // 设置最终值
        current.put(keys[keys.length - 1], value);
    }
}
