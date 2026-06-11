package com.crm.askdata.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.askdata.entity.AuthConfig;
import com.crm.askdata.entity.ToolConfig;
import com.crm.askdata.entity.ToolParam;
import com.crm.askdata.entity.ToolResponseField;
import com.crm.askdata.mapper.ToolConfigMapper;
import com.crm.askdata.mapper.ToolParamMapper;
import com.crm.askdata.mapper.ToolResponseFieldMapper;
import com.crm.askdata.model.ToolConfigWithParams;
import com.crm.common.utils.redis.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

/**
 * 工具配置服务
 * <p>
 * 提供工具配置的查询、缓存管理等功能
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolConfigService {

    private final ToolConfigMapper configMapper;
    private final ToolParamMapper paramMapper;
    private final ToolResponseFieldMapper responseFieldMapper;
    private final AuthConfigService authConfigService;

    private static final String CACHE_KEY_PREFIX = "askdata:tool:config:";
    private static final Duration DEFAULT_CACHE_TTL = Duration.ofMinutes(5);

    /**
     * 获取工具配置（带缓存）
     *
     * @param toolName 工具名称
     * @return 工具配置（包含参数列表）
     */
    public ToolConfigWithParams getToolConfigWithParams(String toolName) {
        String cacheKey = CACHE_KEY_PREFIX + "full:" + toolName;

        // 1. 尝试从缓存获取
//        ToolConfigWithParams cached = RedisUtils.getCacheObject(cacheKey);
//        if (cached != null) {
//            log.debug("traceId={} cache hit for tool: {}", getTraceId(), toolName);
//            return cached;
//        }

        // 2. 从数据库查询
        ToolConfig config = configMapper.selectByToolName(toolName);
        if (config == null) {
            log.warn("traceId={} tool config not found: {}", getTraceId(), toolName);
            throw new IllegalArgumentException("工具配置不存在：" + toolName);
        }

        // 3. 加载参数列表
        List<ToolParam> params = paramMapper.selectByToolId(config.getId());

        // 4. 组装完整对象
        ToolConfigWithParams fullConfig = new ToolConfigWithParams();
        fullConfig.setConfig(config);
        fullConfig.setParams(params);

        // 4b. 加载关联的认证配置
        if (config.getAuthConfigId() != null) {
            AuthConfig authConfig = authConfigService.getById(config.getAuthConfigId());
            fullConfig.setAuthConfig(authConfig);
            config.setAuthConfig(authConfig);  // 也回写到 config 对象
        }

        // 5. 写入缓存（使用配置的 TTL，如果为 0 则使用默认值）
        Duration ttl = config.getCacheTtlSeconds() != null && config.getCacheTtlSeconds() > 0
            ? Duration.ofSeconds(config.getCacheTtlSeconds())
            : DEFAULT_CACHE_TTL;

//        RedisUtils.setCacheObject(cacheKey, fullConfig, ttl);
//        log.debug("traceId={} cache set for tool: {}, ttl={}s", getTraceId(), toolName, ttl.getSeconds());

        return fullConfig;
    }

    /**
     * 获取工具配置（带缓存）- 兼容旧接口
     *
     * @param toolName 工具名称
     * @return 工具配置
     */
    public ToolConfig getToolConfig(String toolName) {
        String cacheKey = CACHE_KEY_PREFIX + toolName;

        // 1. 尝试从缓存获取
        ToolConfig cached = RedisUtils.getCacheObject(cacheKey);
        if (cached != null) {
            log.debug("traceId={} cache hit for tool: {}", getTraceId(), toolName);
            return cached;
        }

        // 2. 从数据库查询
        ToolConfig config = configMapper.selectByToolName(toolName);
        if (config == null) {
            log.warn("traceId={} tool config not found: {}", getTraceId(), toolName);
            throw new IllegalArgumentException("工具配置不存在：" + toolName);
        }

        // 3. 加载参数列表
        List<ToolParam> params = paramMapper.selectByToolId(config.getId());
        // 参数已在 SQL 中按 sort_order 排序

        // 4. 写入缓存（使用配置的 TTL，如果为 0 则使用默认值）
        Duration ttl = config.getCacheTtlSeconds() != null && config.getCacheTtlSeconds() > 0
            ? Duration.ofSeconds(config.getCacheTtlSeconds())
            : DEFAULT_CACHE_TTL;

        RedisUtils.setCacheObject(cacheKey, config, ttl);
        log.debug("traceId={} cache set for tool: {}, ttl={}s", getTraceId(), toolName, ttl.getSeconds());

        return config;
    }

    /**
     * 获取所有启用的工具配置
     *
     * @return 工具配置列表
     */
    public List<ToolConfig> getAllEnabledTools() {
        String cacheKey = CACHE_KEY_PREFIX + "all:enabled";

        // 尝试从缓存获取
        @SuppressWarnings("unchecked")
//        List<ToolConfig> cached = RedisUtils.getCacheObject(cacheKey);
//        if (cached != null) {
//            return cached;
//        }

        // 从数据库查询
        List<ToolConfig> configs = configMapper.selectEnabledTools();

        // 写入缓存
        RedisUtils.setCacheObject(cacheKey, configs, DEFAULT_CACHE_TTL);

        return configs;
    }

    /**
     * 清除工具配置缓存
     *
     * @param toolName 工具名称
     */
    public void clearCache(String toolName) {
        String cacheKey = CACHE_KEY_PREFIX + toolName;
        RedisUtils.deleteObject(cacheKey);

        // 同时清除全量缓存和完整对象缓存
        RedisUtils.deleteObject(CACHE_KEY_PREFIX + "all:enabled");
        RedisUtils.deleteObject(CACHE_KEY_PREFIX + "full:" + toolName);

        log.info("traceId={} cache cleared for tool: {}", getTraceId(), toolName);
    }

    /**
     * 清除所有工具配置缓存
     */
    public void clearAllCache() {
        // 简化实现：清除所有以 askdata:tool:config: 开头的键
        // 实际生产中可能需要使用 Redis keys 命令或维护一个集合
        List<ToolConfig> configs = configMapper.selectEnabledTools();
        for (ToolConfig config : configs) {
            clearCache(config.getToolName());
        }
        log.info("traceId={} all tool config cache cleared", getTraceId());
    }

    /**
     * 统计引用指定认证配置的工具数量
     *
     * @param authConfigId 认证配置ID
     * @return 引用的工具数量
     */
    public int countByAuthConfigId(Long authConfigId) {
        if (authConfigId == null) return 0;
        return configMapper.selectCount(
            Wrappers.<ToolConfig>lambdaQuery()
                .eq(ToolConfig::getAuthConfigId, authConfigId)
        ).intValue();
    }

    /**
     * 获取工具响应字段映射列表
     * <p>
     * 用于将 API 返回的原始英文字段名替换为中文显示名，
     * 并应用格式化规则。
     *
     * @param toolId 工具ID
     * @return 响应字段映射列表（按 sort_order 排序）
     */
    public List<ToolResponseField> getResponseFields(Long toolId) {
        if (toolId == null) return java.util.Collections.emptyList();
        return responseFieldMapper.selectByToolId(toolId);
    }

    /**
     * 获取 TraceID（用于日志）
     */
    private String getTraceId() {
        try {
            return com.crm.common.utils.TraceIdUtil.getTraceId();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
