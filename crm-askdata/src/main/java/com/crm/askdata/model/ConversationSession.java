package com.crm.askdata.model;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 会话上下文对象 (BE-10 规范)
 * <p>
 * 当前已对齐 BE-10 关键字段：状态机、澄清轮次、超时检查、对话历史
 * Redis 持久化、乐观锁等在 BE-10 后续任务完善
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

    // ==================== BE-10 对齐字段 ====================

    /**
     * 对话状态枚举（状态机，BE-10）
     */
    public enum ConversationStatus {
        INITIAL, CLARIFYING, EXECUTING, COMPLETED, FAILED, EXPIRED
    }

    /** 对话状态 */
    private ConversationStatus status = ConversationStatus.INITIAL;

    /** 当前澄清轮次（BE-10 最大 5 轮） */
    private Integer currentTurnCount = 0;

    /** 最大澄清轮数 */
    private static final int MAX_TURN_COUNT = 5;

    /** 最后活跃时间（用于超时检查，BE-10 300s） */
    private LocalDateTime lastActiveTime = LocalDateTime.now();

    /** 超时秒数 */
    private Integer timeoutSeconds = 300;

    /** 当前缺失的槽位列表 */
    private List<String> missingSlots;

    /** 对话历史（最近 10 轮，BE-10） */
    private List<DialogueTurn> dialogueHistory;

    // ==================== 策略锁定（防止 confidence 漂移）====================

    /**
     * 澄清期间锁定的工具名
     * <p>
     * 首轮 needClarify 时 Intent Agent 返回的 tool_name 写入，澄清完成后如果本轮大模型未返回
     * tool_name（confidence 漂移），则用此字段兜底，确保始终走 TOOL_CALL 路由。
     */
    private String lockedToolName;

    /**
     * 澄清期间锁定的路由策略
     */
    private String lockedStrategy;

    /**
     * 澄清期间每轮合并的实体参数快照
     * <p>
     * 每轮澄清答复后增量写入，澄清完成时与本轮最新 entities 合并传给执行器。
     */
    private Map<String, Object> lockedEntities;

    // ==================== 方法 ====================

    /**
     * 检查是否超时（BE-10）
     */
    public boolean checkTimeout() {
        if (lastActiveTime == null) {
            return false;
        }
        return Duration.between(lastActiveTime, LocalDateTime.now()).getSeconds() > timeoutSeconds;
    }

    /**
     * 更新最后活跃时间
     */
    public void touch() {
        this.lastActiveTime = LocalDateTime.now();
    }

    /**
     * 增加对话轮次
     */
    public void incrementTurn() {
        this.currentTurnCount++;
        this.touch();
    }

    /**
     * 是否超过最大轮次
     */
    public boolean isMaxTurnsReached() {
        return currentTurnCount >= MAX_TURN_COUNT;
    }

    /**
     * 添加对话历史（最多保留 10 轮，BE-10）
     *
     * @param turn 单轮对话
     */
    public void addDialogueTurn(DialogueTurn turn) {
        if (dialogueHistory == null) {
            dialogueHistory = new ArrayList<>();
        }
        dialogueHistory.add(turn);
        if (dialogueHistory.size() > 10) {
            dialogueHistory.remove(0);
        }
    }

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
