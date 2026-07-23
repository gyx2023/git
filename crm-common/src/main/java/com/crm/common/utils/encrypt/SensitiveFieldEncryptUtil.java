package com.crm.common.utils.encrypt;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

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
    private static final String DEFAULT_AES_KEY = "HcR#9mK$2pL@7qR!";

    /**
     * Base64格式正则表达式（预编译，提高性能）
     */
    private static final Pattern BASE64_PATTERN = Pattern.compile("^[A-Za-z0-9+/]*={0,2}$");

    /**
     * 使用默认密钥加密
     *
     * @param data 待加密数据
     * @return 加密后的字符串（Base64编码）
     */
    public static String encrypt(String data) {
        if (StrUtil.isBlank(data)) {
            return data;
        }

        try {
            return aesEncrypt(data, DEFAULT_AES_KEY);
        } catch (Exception e) {
            log.error("AES加密失败，数据：{}", data, e);
            throw new RuntimeException("数据加密失败", e);
        }
    }

    /**
     * AES加密为base 64 code（与参考工程保持一致）
     *
     * @param content    待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的base 64 code
     * @throws Exception
     */
    private static String aesEncrypt(String content, String encryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(encryptKey.getBytes("UTF-8"));
        kgen.init(128, random);
        SecretKey secretKey = new SecretKeySpec(kgen.generateKey().getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(content.getBytes("UTF-8"));

        // 使用 BASE64Encoder 进行编码（与参考工程保持一致）
        BASE64Encoder encoder = new BASE64Encoder();
        String result = encoder.encode(encryptedBytes);
        // 移除换行符
        result = result.replaceAll("\\r|\\n|\\r\\n", "");
        return result;
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
            return aesEncrypt(data, key);
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
            return aesDecrypt(encryptedData, key);
        } catch (Exception e) {
            // 静默返回原数据，不打印日志避免刷屏
            // 如果需要调试，可以临时开启下面的日志
            // log.debug("AES解密失败，可能数据未加密或密钥错误，数据长度: {}", encryptedData.length());
            return encryptedData;
        }
    }

    /**
     * AES解密（与参考工程保持一致）
     *
     * @param encryptStr 待解密的base 64 code
     * @param decryptKey 解密密钥
     * @return 解密后的string
     * @throws Exception
     */
    private static String aesDecrypt(String encryptStr, String decryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(decryptKey.getBytes("UTF-8"));
        kgen.init(128, random);
        SecretKey secretKey = new SecretKeySpec(kgen.generateKey().getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // 使用 Base64 解码
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptStr);
        byte[] decryptBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptBytes, "UTF-8");
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

        // 快速过滤：明文数据通常不会这么长且符合Base64特征
        // AES加密后的Base64字符串通常至少24字符以上
        if (data.length() < 16) {
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
            // 解密失败说明未加密，静默返回false，不打印日志避免刷屏
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

        try {
            // Base64编码的特征：
            // 1. 长度是4的倍数
            // 2. 只包含 A-Z, a-z, 0-9, +, /, = 字符
            // 3. 可能以1-2个'='结尾作为填充
            if (str.length() % 4 != 0) {
                return false;
            }

            // 排除纯数字的情况（明文手机号、身份证等通常是纯数字）
            if (str.matches("^\\d+$")) {
                return false;
            }

            // 使用预编译的正则表达式进行匹配，避免异常
            return BASE64_PATTERN.matcher(str).matches();
        } catch (Exception e) {
            // 捕获所有可能的异常（如栈溢出、内存不足等），避免影响业务流程
            log.debug("Base64格式判断异常，数据：{}", str, e);
            return false;
        }
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
//        String encryptedStr = "yvcZXLjR4K6JpF8BV6lyyQ==";
//
//        System.out.println("========== 解密测试 ==========");
//        System.out.println("密钥：" + DEFAULT_AES_KEY);
//        System.out.println("加密字符串：" + encryptedStr);
        String str = "韩国天热色专卖店";


        try {
            // 解密
//            String decryptedStr = decrypt(encryptedStr);
//            System.out.println("解密结果：" + decryptedStr);
            String s = encrypt( str);
            log.info(s);

        } catch (Exception e) {
            log.error("解密失败：{}", e.getMessage(), e);
        } finally {
            log.info("========== 测试结束 ==========");
        }
    }

}
