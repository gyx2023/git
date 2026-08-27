package com.crm.askdata.service;

import com.crm.askdata.config.ResultDataReferenceProperties;
import com.crm.common.utils.redis.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 管理结果处理工作流使用的短期数据引用。
 * <p>
 * 这里只保存单次 API 查询对应的结果处理输入，不保存聊天上下文。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResultDataReferenceService {

    private static final String KEY_PREFIX = "askdata:result-data:";
    private static final Pattern DATA_REF_PATTERN = Pattern.compile(
        "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
        Pattern.CASE_INSENSITIVE);

    private final ResultDataReferenceProperties properties;

    /**
     * 将完整结果处理输入短期缓存，并返回给工作流使用的不透明引用。
     */
    public String save(String agentInput, String traceId) {
        if (agentInput == null || agentInput.trim().isEmpty()) {
            throw new IllegalArgumentException("结果处理输入不能为空");
        }

        String dataRef = UUID.randomUUID().toString();
        RedisUtils.setCacheObject(buildKey(dataRef), agentInput, properties.getTtl());
        log.info("traceId={} result data reference created: dataRef={}, ttl={}",
            traceId, shorten(dataRef), properties.getTtl());
        return dataRef;
    }

    /**
     * 读取 dataRef 对应的完整结果处理输入。过期或非法引用返回 null。
     */
    public String get(String dataRef) {
        if (!isValidDataRef(dataRef)) {
            return null;
        }
        return RedisUtils.getCacheObject(buildKey(dataRef));
    }

    private String buildKey(String dataRef) {
        return KEY_PREFIX + dataRef;
    }

    private boolean isValidDataRef(String dataRef) {
        return dataRef != null && DATA_REF_PATTERN.matcher(dataRef).matches();
    }

    private String shorten(String dataRef) {
        return dataRef.substring(0, 8);
    }
}
