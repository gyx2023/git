package com.crm.common.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.json.JSONUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 简化的Token工具类
 * 使用Base64编码而不是AES加密，避免跨系统加密解密不一致的问题
 */
public class SimpleTokenUtil {

    // Token有效期（分钟）
    private static final int EXPIRE_MINUTES = 30;

    /**
     * 验证简化Token（老系统调用）
     * @return 解码后的数据，如果失败返回null
     */
    public static Map<String, Object> validateSimpleToken(String token) {
        try {
            String jsonStr = Base64.decodeStr(token);
            Map<String, Object> data = JSONUtil.toBean(jsonStr, Map.class);

            // 验证有效期
            Long expireTime = (Long) data.get("expireTime");
            if (expireTime != null && expireTime > System.currentTimeMillis()) {
                return data;
            }
        } catch (Exception e) {
            // 静默处理异常
        }
        return null;
    }
}
