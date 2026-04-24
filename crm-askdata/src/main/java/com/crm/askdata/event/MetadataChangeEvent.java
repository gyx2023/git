package com.crm.askdata.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 元数据变更 Spring 事件
 * <p>
 * 陷阱防护：上下文污染陷阱
 * 元数据变更后通知活跃会话清理缓存
 *
 * @author crm
 */
@Getter
public class MetadataChangeEvent extends ApplicationEvent {

    /** 事件类型：CREATE/UPDATE/DELETE */
    private final String eventType;

    /** 实体类型：METRIC/DIMENSION */
    private final String entityType;

    /** 实体 ID */
    private final Long entityId;

    /** 实体代码 */
    private final String entityCode;

    /** 旧版本 */
    private final Integer oldVersion;

    /** 新版本 */
    private final Integer newVersion;

    /** 变更人 */
    private final String changedBy;

    public MetadataChangeEvent(
            Object source,
            String eventType,
            String entityType,
            Long entityId,
            String entityCode,
            Integer oldVersion,
            Integer newVersion,
            String changedBy) {
        super(source);
        this.eventType = eventType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityCode = entityCode;
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
        this.changedBy = changedBy;
    }
}
