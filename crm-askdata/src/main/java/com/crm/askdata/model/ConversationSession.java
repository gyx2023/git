package com.crm.askdata.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 会话上下文对象 (BE-10 桩)
 * <p>
 * 该模型将在 BE-10 (上下文管理) 中完善实现
 * 当前为桩实现，提供基本字段
 *
 * @author crm
 */
@Data
public class ConversationSession {

    /** 会话 ID */
    private String sessionId;

    /** 会话版本 (乐观锁) */
    private Long version = 0L;

    /** 用户 ID */
    private String userId;

    /** 会话参数 */
    private Map<String, Object> params;

    /** 过期元数据标识集合，格式：entityType:entityCode (BE-04) */
    private Set<String> staleMetadataKeys = new HashSet<>();

    /**
     * 标记元数据已变更，清除相关上下文缓存
     * <p>
     * 陷阱防护：上下文污染陷阱
     *
     * @param entityType 实体类型：METRIC/DIMENSION
     * @param entityCode 实体代码
     */
    public void markMetadataStale(String entityType, String entityCode) {
        if (staleMetadataKeys == null) {
            staleMetadataKeys = new HashSet<>();
        }
        staleMetadataKeys.add(entityType + ":" + entityCode);
    }

    /**
     * 检查元数据是否已过期
     *
     * @param entityType 实体类型
     * @param entityCode 实体代码
     * @return 是否过期
     */
    public boolean isMetadataStale(String entityType, String entityCode) {
        if (staleMetadataKeys == null) {
            return false;
        }
        return staleMetadataKeys.contains(entityType + ":" + entityCode);
    }

    /**
     * 清除过期标记
     */
    public void clearStaleMetadata() {
        if (staleMetadataKeys != null) {
            staleMetadataKeys.clear();
        }
    }
}

