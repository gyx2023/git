package com.crm.common.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URLDecoder;
import java.util.Map;

@Slf4j
public class TokenUtil {

    // 16位密钥，新老系统必须一致
    private static final String SECRET_KEY = "hollycrm2024abcd";

    /**
     * 验证Token（新系统调用）
     * @return 解密后的数据，如果失败返回null
     */
    public static Map<String, Object> validateToken(String token) {
        // 首先尝试AES解密（老系统方式）
        try {
            String newToken = Base64.decodeStr(token);
            // AES解密 - 与老系统加密方式匹配
            AES aes = new AES(SECRET_KEY.getBytes());
            // 老系统使用encryptBase64生成token，这里直接使用decryptStr解密
            String decryptStr = aes.decryptStr(newToken);

            Map<String, Object> data = JSONUtil.toBean(decryptStr, Map.class);

            // 验证有效期
            Long expireTime = (Long) data.get("expireTime");
            if (expireTime != null && expireTime > System.currentTimeMillis()) {
                return data;
            }
        } catch (Exception e) {
            // AES解密失败，尝试Base64解码（备选方案）
            try {
                return SimpleTokenUtil.validateSimpleToken(token);
            } catch (Exception ex) {
                // 两种方法都失败，记录日志
                log.error("Token验证失败 - AES解密错误: {}", e.getMessage());
                log.error("Token验证失败 - Base64解码错误: {}", ex.getMessage());
            }
        }
        return null;
    }

}
