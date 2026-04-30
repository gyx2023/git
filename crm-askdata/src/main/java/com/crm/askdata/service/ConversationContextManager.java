package com.crm.askdata.service;

import com.crm.askdata.model.ConversationSession;
import com.crm.common.utils.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;

/**
 * 会话上下文管理器 (BE-10)
 * <p>
 * 使用 Redis 持久化会话数据，TTL 2 小时（与 HiAgent 会话生命周期对齐），
 * 服务重启后会话不丢失。
 *
 * @author crm
 */
@Slf4j
@Service
public class ConversationContextManager {

    /** Redis Key 前缀 */
    private static final String KEY_PREFIX = "askdata:session:";

    /** 会话 TTL（2 小时，与 HiAgentConversationManager 对齐）*/
    private static final Duration SESSION_TTL = Duration.ofHours(2);

    /**
     * 构建 Redis Key
     *
     * @param sessionId 会话 ID
     * @return Redis Key
     */
    private String buildKey(String sessionId) {
        return KEY_PREFIX + sessionId;
    }

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
        return RedisUtils.getCacheObject(buildKey(sessionId));
    }

    /**
     * 创建或更新会话
     *
     * @param session 会话对象
     */
    public void saveSession(ConversationSession session) {
        if (session != null && session.getSessionId() != null) {
            RedisUtils.setCacheObject(buildKey(session.getSessionId()), session, SESSION_TTL);
        }
    }

    /**
     * 更新版本号 (乐观锁)
     *
     * @param sessionId 会话 ID
     * @return 新版本号
     */
    public Long incrementVersion(String sessionId) {
        ConversationSession session = getSession(sessionId);
        if (session != null) {
            long newVersion = session.getVersion() + 1;
            session.setVersion(newVersion);
            saveSession(session);
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
            RedisUtils.deleteObject(buildKey(sessionId));
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
        Collection<String> keys = RedisUtils.keys(KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }
        int count = 0;
        for (String key : keys) {
            ConversationSession session = RedisUtils.getCacheObject(key);
            if (session != null) {
                session.markMetadataStale(entityType, entityCode);
                RedisUtils.setCacheObject(key, session, SESSION_TTL);
                count++;
            }
        }
        log.info("Notified {} active sessions of metadata change: entityType={}, entityCode={}",
                count, entityType, entityCode);
    }
}
