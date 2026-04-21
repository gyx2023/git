package com.crm.system.service.encrypt;

import java.util.Map;

/**
 * 数据加密执行Service接口
 *
 * @author tjx
 * @date 2026-04-17
 */
public interface IDataEncryptionService {

    /**
     * 根据表名和字段名执行加密任务
     *
     * @param tableName 表名（必填）
     * @param fieldName 字段名（可选，不传则加密该表所有配置的字段）
     * @return 执行结果
     */
    Map<String, Object> executeEncryptionByTableAndField(String tableName, String fieldName);
}
