package com.crm.askdata.executor.tool;

import com.crm.askdata.model.QueryResult;
import com.crm.askdata.model.ToolContext;

/**
 * 报表工具标准接口
 * <p>
 * 关键点：
 * 1. 所有工具必须实现此接口
 * 2. 支持 Spring 自动发现注册
 * 3. 统一执行入口
 *
 * @author crm
 */
public interface ReportTool {

    /**
     * 工具唯一标识 (对应 HiAgent 注册的工具名)
     *
     * @return 工具名称
     */
    String getToolName();

    /**
     * 工具描述 (用于 AI 理解)
     *
     * @return 工具描述
     */
    String getDescription();

    /**
     * 执行查询
     *
     * @param context 包含 AI 提取的参数、用户信息、TraceID
     * @return 标准化查询结果
     */
    QueryResult execute(ToolContext context);

    /**
     * 参数校验器 (可选实现，默认通过)
     *
     * @param context 工具调用上下文
     */
    default void validateParams(ToolContext context) {
        // 默认空实现，子类可重写
    }

    /**
     * 工具版本 (用于兼容性管理)
     *
     * @return 版本号
     */
    default String getVersion() {
        return "1.0.0";
    }

    /**
     * 是否敏感工具 (需额外授权)
     *
     * @return 是否敏感
     */
    default boolean isSensitive() {
        return false;
    }
}
