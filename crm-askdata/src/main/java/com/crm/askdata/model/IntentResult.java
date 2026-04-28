package com.crm.askdata.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 意图识别结果
 * <p>
 * 注意事项:
 * 1. 使用 @JsonIgnoreProperties 防止 HiAgent 新增字段导致反序列化失败
 * 2. 所有字段应有默认值，防止空指针
 *
 * @author crm
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IntentResult {

    /** 意图类型 */
    private String intent = "unknown";

    /** 置信度 (0.0-1.0) */
    private Double confidence = 0.0;

    /** 推荐执行策略 */
    private String recommendedStrategy = "FALLBACK_KB";

    /** 提取的实体参数 */
    private Map<String, Object> entities = Collections.emptyMap();

    /** 缺失的必填参数 */
    @JsonAlias({"missing_slots"})
    private List<String> missingSlots = Collections.emptyList();

    /** 匹配的工具名称 */
    @JsonAlias({"tool_name"})
    private String toolName;

    /** AI 生成的 SQL (仅 TEXT_TO_SQL 策略) */
    private String generatedSql;

    /** 是否需要澄清（兼容 needs_clarification / needClarify / need_clarify 三种命名） */
    @JsonAlias({"needs_clarification", "needClarify", "need_clarify"})
    private Boolean needClarify = false;

    /** 澄清问题 */
    @JsonAlias({"clarify_question", "clarifyQuestion"})
    private String clarifyQuestion;

    /** 澄清选项 */
    @JsonAlias({"clarify_options", "clarifyOptions"})
    private List<String> clarifyOptions = Collections.emptyList();

    /** HiAgent 侧请求 ID (用于排查) */
    private String hiagentRequestId;

    /** 会话版本 (用于状态同步校验) */
    private Long sessionVersion;

    /**
     * 验证结果有效性
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return intent != null && confidence != null;
    }

    /**
     * 获取置信度 (防止 null)
     *
     * @return 置信度
     */
    public double getConfidenceSafe() {
        return confidence != null ? confidence : 0.0;
    }
}
