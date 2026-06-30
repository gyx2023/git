package com.crm.askdata.service;

import com.crm.common.utils.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * HiAgent 会话 ID 管理器
 * <p>
 * 职责：维护 userId + agentId + sessionId → AppConversationID 的映射关系，
 * 使用 Redis 缓存，TTL 默认 2 小时（与 HiAgent 会话生命周期对齐）。
 * <p>
 * 会话隔离策略：
 * - Key 中加入 sessionId 维度，确保每个页面/浏览器独立 HiAgent 会话
 * - 不同 sessionId → 不同 HiAgent AppConversationID → 互不影响的对话历史
 * - 新打开页面（新 sessionId）自动 cache miss → 创建新 HiAgent 会话
 * - TTL 超期或强制清除后，下次请求自动触发 CreateConversation
 *
 * @author crm
 */
@Slf4j
@Service
public class HiAgentConversationManager {

    /** Redis Key 前缀 */
    private static final String KEY_PREFIX = "hiagent:conv:";

    /** 会话 TTL（2 小时）*/
    private static final Duration CONV_TTL = Duration.ofHours(2);

    /**
     * 构建 Redis Key，按 agentId + userId + sessionId 三维隔离
     *
     * @param userId    业务用户 ID
     * @param agentId   Agent 业务标识
     * @param sessionId 前端会话 ID（维度隔离，不同页面/浏览器独立）
     * @return Redis Key
     */
    private String buildKey(String userId, String agentId, String sessionId) {
        return KEY_PREFIX + agentId + ":" + userId + ":" + sessionId;
    }

    /**
     * 获取已缓存的 AppConversationID
     *
     * @param userId    业务用户 ID
     * @param agentId   Agent 业务标识
     * @param sessionId 前端会话 ID（隔离维度）
     * @return AppConversationID，不存在返回 null
     */
    public String getConversationId(String userId, String agentId, String sessionId) {
        String key = buildKey(userId, agentId, sessionId);
        String convId = RedisUtils.getCacheObject(key);
        if (convId != null) {
            log.debug("userId={} agentId={} sessionId={} hit cached convId={}", userId, agentId, sessionId, convId);
        }
        return convId;
    }

    /**
     * 缓存 AppConversationID
     *
     * @param userId    业务用户 ID
     * @param agentId   Agent 业务标识
     * @param sessionId 前端会话 ID（隔离维度）
     * @param convId    AppConversationID
     */
    public void saveConversationId(String userId, String agentId, String sessionId, String convId) {
        String key = buildKey(userId, agentId, sessionId);
        RedisUtils.setCacheObject(key, convId, CONV_TTL);
        log.info("userId={} agentId={} sessionId={} saved convId={}", userId, agentId, sessionId, convId);
    }

    /**
     * 强制清除指定用户某 Agent 的会话（如用户手动清空对话）
     *
     * @param userId    业务用户 ID
     * @param agentId   Agent 业务标识
     * @param sessionId 前端会话 ID（隔离维度）
     */
    public void removeConversationId(String userId, String agentId, String sessionId) {
        String key = buildKey(userId, agentId, sessionId);
        RedisUtils.deleteObject(key);
        log.info("userId={} agentId={} sessionId={} removed convId", userId, agentId, sessionId);
    }

    /**
     * 强制续期会话：删除旧会话缓存，下次请求自动创建新会话
     * <p>
     * 在检测到用户切换话题时调用，避免旧对话历史干扰新话题
     *
     * @param userId    业务用户 ID
     * @param agentId   Agent 业务标识
     * @param sessionId 前端会话 ID（隔离维度）
     */
    public void renewConversation(String userId, String agentId, String sessionId) {
        removeConversationId(userId, agentId, sessionId);
        log.info("userId={} agentId={} sessionId={} conversation renewed (cache cleared)", userId, agentId, sessionId);
    }

    /**
     * 清除指定用户所有 Agent 的会话缓存（全 sessionId 匹配）
     *
     * @param userId 业务用户 ID
     */
    public void removeAllConversations(String userId) {
        String keyPattern = KEY_PREFIX + "*:" + userId + ":*";
        RedisUtils.deleteObject(RedisUtils.keys(keyPattern));
        log.info("userId={} removed all conv caches", userId);
    }
}
