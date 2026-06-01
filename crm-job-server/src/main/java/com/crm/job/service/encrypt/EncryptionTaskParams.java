package com.crm.job.service.encrypt;

import lombok.Data;

/**
 * 敏感字段加密任务参数
 *
 * @author tjx
 * @date 2026-04-17
 */
@Data
public class EncryptionTaskParams {

    /**
     * 配置ID（与tableName和fieldName二选一）
     */
    private Long configId;

    /**
     * 表名（与fieldName配合使用）
     */
    private String tableName;

    /**
     * 字段名（与tableName配合使用）
     */
    private String fieldName;
}
