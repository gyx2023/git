package com.crm.askdata.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.askdata.entity.AuthConfig;
import com.crm.askdata.entity.ToolConfig;
import com.crm.askdata.mapper.AuthConfigMapper;
import com.crm.askdata.mapper.ToolConfigMapper;
import com.crm.common.utils.TraceIdUtil;
import com.crm.common.utils.redis.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * 外部系统认证配置服务
 * <p>
 * 提供认证配置的 CRUD 操作、缓存管理、加密存储等功能。
 * 认证配置可被多个工具共享，工具通过 auth_config_id 关联。
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthConfigService {

    private final AuthConfigMapper authConfigMapper;
    private final ToolConfigMapper toolConfigMapper;

    private static final String CACHE_KEY_PREFIX = "askdata:auth:config:";
    private static final Duration DEFAULT_CACHE_TTL = Duration.ofMinutes(30);

    // ======================== 查询 ========================

    /**
     * 根据 ID 查询认证配置（带缓存）
     *
     * @param id 主键ID
     * @return 认证配置
     */
    public AuthConfig getById(Long id) {
        if (id == null) {
            return null;
        }

        String cacheKey = CACHE_KEY_PREFIX + id;

        AuthConfig cached = RedisUtils.getCacheObject(cacheKey);
        if (cached != null) {
            return cached;
        }

        AuthConfig config = authConfigMapper.selectById(id);
        if (config != null) {
            RedisUtils.setCacheObject(cacheKey, config, DEFAULT_CACHE_TTL);
        }

        return config;
    }

    /**
     * 根据系统名称查询认证配置（带缓存）
     *
     * @param systemName 系统名称
     * @return 认证配置
     */
    public AuthConfig getBySystemName(String systemName) {
        if (systemName == null || systemName.trim().isEmpty()) {
            return null;
        }

        String cacheKey = CACHE_KEY_PREFIX + "name:" + systemName;

        AuthConfig cached = RedisUtils.getCacheObject(cacheKey);
        if (cached != null) {
            return cached;
        }

        AuthConfig config = authConfigMapper.selectBySystemName(systemName);
        if (config != null) {
            RedisUtils.setCacheObject(cacheKey, config, DEFAULT_CACHE_TTL);
        }

        return config;
    }

    /**
     * 查询所有启用的认证配置
     *
     * @return 认证配置列表
     */
    public List<AuthConfig> getEnabledConfigs() {
        String cacheKey = CACHE_KEY_PREFIX + "all:enabled";

        @SuppressWarnings("unchecked")
        List<AuthConfig> cached = RedisUtils.getCacheObject(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<AuthConfig> configs = authConfigMapper.selectEnabledConfigs();
        RedisUtils.setCacheObject(cacheKey, configs, DEFAULT_CACHE_TTL);

        return configs;
    }

    /**
     * 查询所有认证配置（后台管理用）
     *
     * @param keyword 关键词（按系统名称模糊查询，可选）
     * @param authType 认证方式（可选）
     * @return 认证配置列表
     */
    public List<AuthConfig> queryList(String keyword, String authType) {
        LambdaQueryWrapper<AuthConfig> wrapper = Wrappers.lambdaQuery();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(AuthConfig::getSystemName, keyword.trim());
        }
        if (authType != null && !authType.trim().isEmpty()) {
            wrapper.eq(AuthConfig::getAuthType, authType.trim());
        }

        wrapper.orderByAsc(AuthConfig::getSystemName);

        return authConfigMapper.selectList(wrapper);
    }

    // ======================== 新增/更新 ========================

    /**
     * 保存认证配置（新增或更新）
     *
     * @param config  认证配置
     * @param operator 操作人
     * @return 保存后的认证配置
     */
    public AuthConfig saveConfig(AuthConfig config, String operator) {
        boolean isNew = config.getId() == null;

        if (isNew) {
            // 检查系统名称是否重复
            AuthConfig existing = authConfigMapper.selectBySystemName(config.getSystemName());
            if (existing != null) {
                throw new IllegalArgumentException("系统名称已存在：" + config.getSystemName());
            }

            config.setCreateBy(operator);
            authConfigMapper.insert(config);
            log.info("AuthConfig created: system={}, authType={}, operator={}",
                config.getSystemName(), config.getAuthType(), operator);
        } else {
            // 检查系统名称是否重复（排除自身）
            int count = authConfigMapper.countBySystemNameExcludeId(config.getSystemName(), config.getId());
            if (count > 0) {
                throw new IllegalArgumentException("系统名称已存在：" + config.getSystemName());
            }

            config.setUpdateBy(operator);
            authConfigMapper.updateById(config);
            log.info("AuthConfig updated: id={}, system={}, authType={}, operator={}",
                config.getId(), config.getSystemName(), config.getAuthType(), operator);
        }

        clearCache(config.getId(), config.getSystemName());

        return config;
    }

    // ======================== 删除 ========================

    /**
     * 删除认证配置（逻辑删除）
     *
     * @param id       主键ID
     * @param operator 操作人
     */
    public void deleteConfig(Long id, String operator) {
        // 检查是否被工具引用
        int refCount = countToolReferences(id);
        if (refCount > 0) {
            throw new IllegalArgumentException("该认证配置被 " + refCount + " 个工具引用，请先解除关联后再删除");
        }

        AuthConfig config = authConfigMapper.selectById(id);
        if (config == null) {
            throw new IllegalArgumentException("认证配置不存在");
        }

        config.setDelFlag("2");
        config.setUpdateBy(operator);
        authConfigMapper.updateById(config);

        clearCache(id, config.getSystemName());

        log.info("AuthConfig deleted: id={}, system={}, operator={}", id, config.getSystemName(), operator);
    }

    /**
     * 检查认证配置被多少个工具引用
     */
    private int countToolReferences(Long authConfigId) {
        if (authConfigId == null) return 0;
        return toolConfigMapper.selectCount(
            Wrappers.<ToolConfig>lambdaQuery()
                .eq(ToolConfig::getAuthConfigId, authConfigId)
        ).intValue();
    }

    // ======================== 缓存管理 ========================

    /**
     * 清除认证配置缓存
     *
     * @param id         主键ID
     * @param systemName 系统名称
     */
    public void clearCache(Long id, String systemName) {
        if (id != null) {
            RedisUtils.deleteObject(CACHE_KEY_PREFIX + id);
        }
        if (systemName != null) {
            RedisUtils.deleteObject(CACHE_KEY_PREFIX + "name:" + systemName);
        }
        RedisUtils.deleteObject(CACHE_KEY_PREFIX + "all:enabled");
    }

    /**
     * 清除所有认证配置缓存
     */
    public void clearAllCache() {
        // 简化实现：清除所有启用的（key 确知的）
        List<AuthConfig> configs = authConfigMapper.selectEnabledConfigs();
        for (AuthConfig config : configs) {
            clearCache(config.getId(), config.getSystemName());
        }
    }
}
