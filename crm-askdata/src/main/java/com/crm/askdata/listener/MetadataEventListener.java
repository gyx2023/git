package com.crm.askdata.listener;

import com.crm.askdata.entity.MetaChangeEvent;
import com.crm.askdata.event.MetadataChangeEvent;
import com.crm.askdata.mapper.MetaChangeEventMapper;
import com.crm.askdata.service.CacheService;
import com.crm.askdata.service.ConversationContextManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 元数据变更事件监听器
 * <p>
 * 陷阱防护：上下文污染陷阱
 * 元数据变更后清理相关缓存，通知活跃会话
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetadataEventListener {

    private final CacheService cacheService;
    private final ConversationContextManager contextManager;
    private final MetaChangeEventMapper changeEventMapper;

    /**
     * 处理元数据变更事件
     * 异步处理，防止阻塞主流程
     *
     * @param event 变更事件
     */
    @Async
    @EventListener
    public void handleMetadataChange(MetadataChangeEvent event) {
        log.info("Metadata change event: type={}, entity={}, code={}, version={}",
                event.getEventType(), event.getEntityType(),
                event.getEntityCode(), event.getNewVersion());

        // 1. 清理缓存 (所有层级)
        cacheService.evictMetadataCache(event.getEntityType(), event.getEntityCode());

        // 2. 通知活跃会话清理上下文缓存 (陷阱防护)
        notifySessions(event);

        // 3. 记录审计日志到数据库
        persistChangeEvent(event);
    }

    /**
     * 通知活跃会话清理与该元数据相关的上下文
     */
    private void notifySessions(MetadataChangeEvent event) {
        try {
            contextManager.notifyMetadataChange(event.getEntityType(), event.getEntityCode());
        } catch (Exception e) {
            // 通知会话失败不影响主流程
            log.warn("Failed to notify sessions for metadata change: entity={}, code={}",
                    event.getEntityType(), event.getEntityCode(), e);
        }
    }

    /**
     * 持久化变更事件到数据库
     */
    private void persistChangeEvent(MetadataChangeEvent event) {
        try {
            MetaChangeEvent record = MetaChangeEvent.builder()
                    .eventType(event.getEventType())
                    .entityType(event.getEntityType())
                    .entityId(event.getEntityId())
                    .entityCode(event.getEntityCode())
                    .oldVersion(event.getOldVersion())
                    .newVersion(event.getNewVersion())
                    .changedBy(event.getChangedBy())
                    .changedAt(new Date())
                    .build();
            changeEventMapper.insert(record);

            log.debug("AUDIT: Metadata changed: {} {} v{} -> v{} by {}",
                    event.getEntityType(), event.getEntityCode(),
                    event.getOldVersion(), event.getNewVersion(), event.getChangedBy());
        } catch (Exception e) {
            // 审计日志写入失败不影响主流程
            log.error("Failed to persist metadata change event: entity={}, code={}",
                    event.getEntityType(), event.getEntityCode(), e);
        }
    }
}
