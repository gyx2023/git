package com.crm.askdata.router;

import lombok.Getter;

/**
 * 路由策略枚举
 * <p>
 * 关键点：reversible 标记支持回退机制
 *
 * @author crm
 */
@Getter
public enum RoutingStrategy {

    TOOL_CALL("工具调用", "调用预定义的报表 API 接口", 1, true),
    SEMANTIC_QUERY("语义查询", "基于指标字典动态构建查询", 2, true),
    TEXT_TO_SQL("SQL 沙箱", "AI 生成 SQL 在沙箱执行", 3, false),
    FALLBACK_KB("知识库降级", "返回知识库相关信息", 4, false);

    /** 策略显示名称 */
    private final String displayName;
    /** 策略描述 */
    private final String description;
    /** 优先级 (越小越高) */
    private final Integer priority;
    /** 是否可回退 */
    private final boolean reversible;

    RoutingStrategy(String displayName, String description, Integer priority, boolean reversible) {
        this.displayName = displayName;
        this.description = description;
        this.priority = priority;
        this.reversible = reversible;
    }
}
