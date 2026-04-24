package com.crm.askdata.service;

import com.crm.askdata.model.ConversationSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话上下文管理器 (BE-10 桩实现)
 * <p>
 * 该服务将在 BE-10 (上下文管理) 中完善实现
 * 当前为桩实现，使用内存存储，仅用于开发调试
 *
 * @author crm
 */
@Slf4j
@Service
public class ConversationContextManager {

    /** 内存存储 (桩实现，生产环境应使用 Redis) */
    private final Map<String, ConversationSession> sessionStore = new ConcurrentHashMap<>();

    /**
     * 获取会话
     *
     * @param sessionId 会话 ID
     * @return 会话对象，不存在返回 null
     */
    public ConversationSession getSession(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        return sessionStore.get(sessionId);
    }

    /**
     * 创建或更新会话
     *
     * @param session 会话对象
     */
    public void saveSession(ConversationSession session) {
        if (session != null && session.getSessionId() != null) {
            sessionStore.put(session.getSessionId(), session);
            log.debug("Session saved: sessionId={}, version={}", session.getSessionId(), session.getVersion());
        }
    }

    /**
     * 更新版本号 (乐观锁)
     *
     * @param sessionId 会话 ID
     * @return 新版本号
     */
    public Long incrementVersion(String sessionId) {
        ConversationSession session = sessionStore.get(sessionId);
        if (session != null) {
            long newVersion = session.getVersion() + 1;
            session.setVersion(newVersion);
            return newVersion;
        }
        return null;
    }

    /**
     * 删除会话
     *
     * @param sessionId 会话 ID
     */
    public void removeSession(String sessionId) {
        if (sessionId != null) {
            sessionStore.remove(sessionId);
        }
    }

    /**
     * 通知活跃会话元数据已变更，清理相关缓存 (BE-04)
     * <p>
     * 陷阱防护：上下文污染陷阱
     * 遍历所有活跃会话，标记该元数据已变更，避免会话使用过期的元数据
     *
     * @param entityType 实体类型：METRIC/DIMENSION
     * @param entityCode 实体代码
     */
    public void notifyMetadataChange(String entityType, String entityCode) {
        if (entityType == null || entityCode == null) {
            return;
        }
        int count = 0;
        for (ConversationSession session : sessionStore.values()) {
            if (session != null) {
                // 标记该会话的元数据已变更，下次查询时将重新加载
                session.markMetadataStale(entityType, entityCode);
                count++;
            }
        }
        log.info("Notified {} active sessions of metadata change: entityType={}, entityCode={}",
                count, entityType, entityCode);
    }
}
