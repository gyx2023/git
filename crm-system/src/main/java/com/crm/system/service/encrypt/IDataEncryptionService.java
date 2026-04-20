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
     * 根据配置ID执行加密任务
     *
     * @param configId 配置ID
     * @return 执行结果
     */
    Map<String, Object> executeEncryption(Long configId);

    /**
     * 根据表名和字段名执行加密任务
     *
     * @param tableName 表名
     * @param fieldName 字段名
     * @return 执行结果
     */
    Map<String, Object> executeEncryptionByTableAndField(String tableName, String fieldName);
}
