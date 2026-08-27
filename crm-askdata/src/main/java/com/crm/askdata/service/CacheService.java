package com.crm.askdata.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * 缓存管理服务
 * <p>
 * 统一管理元数据缓存失效，确保 L1/L2 缓存一致性
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final CacheManager cacheManager;

    /**
     * 失效指定实体的元数据缓存
     *
     * @param entityType 实体类型：METRIC/DIMENSION
     * @param entityCode 实体代码
     */
    public void evictMetadataCache(String entityType, String entityCode) {
        if ("METRIC".equals(entityType)) {
            evictCache("semantic:metric", entityCode);
            evictCache("semantic:metrics", null); // 批量缓存全清
            evictCache("semantic:metrics:all", "all");
        } else if ("DIMENSION".equals(entityType)) {
            evictCache("semantic:dimension", entityCode);
            evictCache("semantic:dimensions:all", "all");
        }
        log.debug("Cache evicted: entityType={}, entityCode={}", entityType, entityCode);
    }

    /**
     * 清理指定缓存的某个 key
     *
     * @param cacheName 缓存名
     * @param key       key (null 表示清理全部)
     */
    public void evictCache(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return;
        }
        if (key == null) {
            cache.clear();
            log.debug("Cache cleared: name={}", cacheName);
        } else {
            cache.evict(key);
            log.debug("Cache evicted: name={}, key={}", cacheName, key);
        }
    }

    /**
     * 清理所有元数据缓存
     */
    public void evictAllMetadataCache() {
        String[] cacheNames = {
            "semantic:metric",
            "semantic:metrics",
            "semantic:metrics:all",
            "semantic:dimension",
            "semantic:dimensions:all"
        };
        for (String name : cacheNames) {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        }
        log.info("All metadata caches cleared");
    }
}
