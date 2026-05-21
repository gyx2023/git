package com.crm.job.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 签名工具类（对应asiainfo-openplatform的SignUtil）
 */
public class SignUtil {

    /**
     * HmacSHA256签名
     *
     * @param sysParam   系统参数Map
     * @param busiParam  业务参数JSON字符串
     * @param method     签名算法，如"HmacSHA256"
     * @param key        密钥
     * @return 签名字符串（大写十六进制）
     */
    public static String sign(Map<String, String> sysParam, String busiParam, String method, String key) throws Exception {
        if ((sysParam == null || sysParam.isEmpty()) && (busiParam == null || busiParam.trim().length() == 0)) {
            return null;
        }

        if ("HmacSHA256".equals(method)) {
            return signWithSHA256(sysParam, busiParam, key);
        } else if ("RSAWithMD5".equals(method)) {
            throw new Exception("暂不支持RSAWithMD5签名方法");
        } else {
            throw new Exception("对不起，暂不支持[" + method + "]签名方法！");
        }
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

        // 计算HmacSHA256并返回大写十六进制字符串
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(buf.toString().getBytes(StandardCharsets.UTF_8));

        // 转换为大写十六进制字符串
        return bytesToHexUpper(hash);
    }

    /**
     * 字节数组转大写十六进制字符串
     */
    private static String bytesToHexUpper(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
