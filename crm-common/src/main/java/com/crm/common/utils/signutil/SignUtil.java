package com.crm.common.utils.signutil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 签名工具类（从asiainfo-openplatform搬迁）
 * 用于能力汇聚平台接口调用签名
 */
public class SignUtil {

    private static final String SIGN_METHOD_RSAWITHMD5 = "RSAWithMD5";
    private static final String SIGN_METHOD_HMACSHA256 = "HmacSHA256";

    /**
     * 生成签名
     *
     * @param sysParam   系统参数Map
     * @param busiParam  业务参数JSON字符串
     * @param method     签名算法，如"HmacSHA256"或"RSAWithMD5"
     * @param key        密钥
     * @return 签名字符串（大写十六进制）
     * @throws Exception 签名异常
     */
    public static String sign(Map<String, String> sysParam, String busiParam, String method, String key) throws Exception {
        if ((sysParam == null || sysParam.isEmpty()) && (busiParam == null || busiParam.trim().length() == 0)) {
            return null;
        }

        if (SIGN_METHOD_HMACSHA256.equals(method)) {
            return signWithSHA256(sysParam, busiParam, key);
        } else if (SIGN_METHOD_RSAWITHMD5.equals(method)) {
            return signWithRSA(sysParam, busiParam, key);
        } else {
            throw new Exception("对不起，暂不支持[" + method + "]签名方法！");
        }
    }

    /**
     * 生成签名（默认使用RSAWithMD5）
     *
     * @param sysParam   系统参数Map
     * @param busiParam  业务参数JSON字符串
     * @param key        密钥
     * @return 签名字符串
     * @throws Exception 签名异常
     */
    public static String sign(Map<String, String> sysParam, String busiParam, String key) throws Exception {
        if ((sysParam == null || sysParam.isEmpty()) && (busiParam == null || busiParam.trim().length() == 0)) {
            return null;
        }

        return signWithRSA(sysParam, busiParam, key);
    }

    /**
     * RSAWithMD5签名实现
     */
    private static String signWithRSA(Map<String, String> sysParam, String busiParam, String key) throws Exception {
        Map<String, String> map = new HashMap<>(sysParam);
        map.put("content", busiParam);

        String[] keys = map.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        StringBuilder buf = new StringBuilder(200);

        for (String k : keys) {
            if (!"sign".equalsIgnoreCase(k)) {
                buf.append(k).append(map.get(k));
            }
        }

        System.out.println(buf.toString());

        // MD5签名实现（简化版，实际可能需要完整的RSA实现）
        return SecurityUtils.encodeMd5HexUpper(buf.toString() + key);
    }

    /**
     * HmacSHA256签名实现
     */
    private static String signWithSHA256(Map<String, String> sysParam, String busiParam, String key) throws Exception {
        Map<String, String> map = new HashMap<>(sysParam);
        map.put("content", busiParam);

        String[] keys = map.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        StringBuilder buf = new StringBuilder(200);
        buf.append(key);

        for (String k : keys) {
            if (!"sign".equalsIgnoreCase(k)) {
                buf.append(k).append(map.get(k));
            }
        }

        buf.append(key);

        System.out.println("signStr=" + buf.toString());

        // 计算HmacSHA256并返回大写十六进制字符串
        byte[] decodedKey = SecurityUtils.decodeHexUpper(key);
        return SecurityUtils.encodeHmacSHA256HexUpper(buf.toString(), decodedKey);
    }
}
