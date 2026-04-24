package com.crm.askdata.service;

import com.crm.common.utils.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * HiAgent 会话 ID 管理器
 * <p>
 * 职责：维护 userId + agentId → AppConversationID 的映射关系，
 * 使用 Redis 缓存，TTL 默认 2 小时（与 HiAgent 会话生命周期对齐）。
 * <p>
 * 设计说明：
 * - 每个用户对每个 Agent 工作流维护独立的会话，支持多 Agent 并发使用
 * - 会话创建由 HiAgentClient 完成，本类仅负责存取映射关系
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
     * 构建 Redis Key
     *
     * @param userId  业务用户 ID
     * @param agentId Agent 业务标识
     * @return Redis Key
     */
    private String buildKey(String userId, String agentId) {
        return KEY_PREFIX + agentId + ":" + userId;
    }

    /**
     * 获取已缓存的 AppConversationID
     *
     * @param userId  业务用户 ID
     * @param agentId Agent 业务标识
     * @return AppConversationID，不存在返回 null
     */
    public String getConversationId(String userId, String agentId) {
        String key = buildKey(userId, agentId);
        String convId = RedisUtils.getCacheObject(key);
        if (convId != null) {
            log.debug("userId={} agentId={} hit cached convId={}", userId, agentId, convId);
        }
        return convId;
    }

    /**
     * 缓存 AppConversationID
     *
     * @param userId  业务用户 ID
     * @param agentId Agent 业务标识
     * @param convId  AppConversationID
     */
    public void saveConversationId(String userId, String agentId, String convId) {
        String key = buildKey(userId, agentId);
        RedisUtils.setCacheObject(key, convId, CONV_TTL);
        log.info("userId={} agentId={} saved convId={}", userId, agentId, convId);
    }

    /**
     * 强制清除指定用户某 Agent 的会话（如用户手动清空对话）
     *
     * @param userId  业务用户 ID
     * @param agentId Agent 业务标识
     */
    public void removeConversationId(String userId, String agentId) {
        String key = buildKey(userId, agentId);
        RedisUtils.deleteObject(key);
        log.info("userId={} agentId={} removed convId", userId, agentId);
    }

    /**
     * 清除指定用户所有 Agent 的会话缓存
     *
     * @param userId 业务用户 ID
     */
    public void removeAllConversations(String userId) {
        String keyPattern = KEY_PREFIX + "*:" + userId;
        RedisUtils.deleteObject(RedisUtils.keys(keyPattern));
        log.info("userId={} removed all conv caches", userId);
    }
}
