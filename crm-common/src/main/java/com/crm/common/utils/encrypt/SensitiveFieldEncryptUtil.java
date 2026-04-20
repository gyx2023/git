package com.crm.common.utils.encrypt;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * 敏感字段加解密工具类
 * 基于AES算法实现字段的加密和解密
 *
 * @author tjx
 */
@Slf4j
public class SensitiveFieldEncryptUtil {

    /**
     * 默认AES密钥（16位）
     * 生产环境建议从配置文件读取
     */
    private static final String DEFAULT_AES_KEY = "HcR#9mk$2pL@7qR!";

    /**
     * 使用默认密钥加密
     *
     * @param data 待加密数据
     * @return 加密后的字符串（Base64编码）
     */
    public static String encrypt(String data) {
        return encrypt(data, DEFAULT_AES_KEY);
    }

    /**
     * 使用指定密钥加密
     *
     * @param data 待加密数据
     * @param key  AES密钥（必须为16位、24位或32位）
     * @return 加密后的字符串（Base64编码）
     */
    public static String encrypt(String data, String key) {
        if (StrUtil.isBlank(data)) {
            return data;
        }

        if (StrUtil.isBlank(key)) {
            throw new IllegalArgumentException("AES密钥不能为空");
        }

        // AES密钥长度校验：16位、24位、32位
        int keyLength = key.length();
        if (keyLength != 16 && keyLength != 24 && keyLength != 32) {
            throw new IllegalArgumentException("AES密钥长度必须为16位、24位或32位，当前长度：" + keyLength);
        }

        try {
            AES aes = SecureUtil.aes(key.getBytes(StandardCharsets.UTF_8));
            return aes.encryptBase64(data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES加密失败，数据：{}", data, e);
            throw new RuntimeException("数据加密失败", e);
        }
    }

    /**
     * 使用默认密钥解密
     *
     * @param encryptedData 待解密的字符串（Base64编码）
     * @return 解密后的原始数据
     */
    public static String decrypt(String encryptedData) {
        return decrypt(encryptedData, DEFAULT_AES_KEY);
    }

    /**
     * 使用指定密钥解密
     *
     * @param encryptedData 待解密的字符串（Base64编码）
     * @param key           AES密钥（必须为16位、24位或32位）
     * @return 解密后的原始数据
     */
    public static String decrypt(String encryptedData, String key) {
        if (StrUtil.isBlank(encryptedData)) {
            return encryptedData;
        }

        if (StrUtil.isBlank(key)) {
            throw new IllegalArgumentException("AES密钥不能为空");
        }

        try {
            AES aes = SecureUtil.aes(key.getBytes(StandardCharsets.UTF_8));
            return aes.decryptStr(encryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密失败，可能数据未加密或密钥错误，数据：{}", encryptedData, e);
            // 解密失败时返回原数据，避免影响业务流程
            return encryptedData;
        }
    }

    /**
     * 判断数据是否已加密
     * 通过以下方式综合判断：
     * 1. 检查是否符合Base64编码格式
     * 2. 尝试解密，如果解密后与原文不同则认为已加密
     *
     * @param data 待判断的数据
     * @return true-已加密，false-未加密
     */
    public static boolean isEncrypted(String data) {
        if (StrUtil.isBlank(data)) {
            return false;
        }

        // 快速判断：加密后的数据通常是Base64编码，长度一般为4的倍数
        // 且包含Base64特征字符（A-Z, a-z, 0-9, +, /, =）
        if (!isBase64Format(data)) {
            return false;
        }

        try {
            String decrypted = decrypt(data);
            // 如果解密成功且与原文不同，说明已加密
            return decrypted != null && !decrypted.equals(data);
        } catch (Exception e) {
            // 解密失败说明未加密
            return false;
        }
    }

    /**
     * 判断字符串是否符合Base64编码格式
     *
     * @param str 待判断的字符串
     * @return true-符合Base64格式，false-不符合
     */
    private static boolean isBase64Format(String str) {
        if (StrUtil.isBlank(str)) {
            return false;
        }

        // Base64编码的特征：
        // 1. 长度是4的倍数
        // 2. 只包含 A-Z, a-z, 0-9, +, /, = 字符
        // 3. 可能以1-2个'='结尾作为填充
        if (str.length() % 4 != 0) {
            return false;
        }

        // 正则表达式匹配Base64格式
        return str.matches("^[A-Za-z0-9+/]*={0,2}$");
    }

    /**
     * 智能加密：如果数据未加密则加密，已加密则跳过
     *
     * @param data 待处理的数据
     * @return 加密后的数据
     */
    public static String smartEncrypt(String data) {
        if (StrUtil.isBlank(data)) {
            return data;
        }

        if (isEncrypted(data)) {
            log.debug("数据已加密，跳过加密操作");
            return data;
        }

        return encrypt(data);
    }

    /**
     * 解密测试
     */
    public static void main(String[] args) {
        // 加密串（请在这里填入需要解密的字符串）
        String encryptedStr = "/XQxj2Pb38d/RnbkEJZKvr71fuTmvbLIiutBq92tPUCiaDjnCua8DKDE/jSyAm/7tQHgkcf+/ehIQ3PW9+00Mg==";

        System.out.println("========== 解密测试 ==========");
        System.out.println("密钥：" + DEFAULT_AES_KEY);
        System.out.println("加密字符串：" + encryptedStr);

        try {
            // 解密
            String decryptedStr = decrypt(encryptedStr);
            System.out.println("解密结果：" + decryptedStr);

        } catch (Exception e) {
            System.err.println("解密失败：" + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("========== 测试结束 ==========");
        }
    }
}
