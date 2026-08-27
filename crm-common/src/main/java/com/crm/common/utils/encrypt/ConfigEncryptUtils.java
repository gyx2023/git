package com.crm.common.utils.encrypt;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * 配置文件敏感信息加解密工具。
 * 密文格式 ENC(base64(iv+cipher))，算法 AES/CBC/PKCS5Padding，
 * 密钥由主密钥经 SHA-256 派生取前 16 字节（固定派生，规避 SecureRandom(SHA1PRNG) 跨 JDK 版本不一致问题）。
 * 与 yypt_web 旧系统的 EncryptablePropertyPlaceholderConfigurer 同算法同主密钥，密文可互通。
 *
 * 主密钥来源：-Dconfig.encrypt.key（优先）或环境变量 CONFIG_ENCRYPT_KEY，禁止写入配置文件。
 */
public class ConfigEncryptUtils {

    public static final String PREFIX = "ENC(";
    public static final String SUFFIX = ")";

    private ConfigEncryptUtils() {
    }

    /**
     * 判断值是否为 ENC(...) 密文
     */
    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX) && value.endsWith(SUFFIX);
    }

    /**
     * 解析主密钥：系统属性优先，其次环境变量；缺失则抛异常（fail-fast）
     */
    public static String resolveMasterKey() {
        String key = System.getProperty("config.encrypt.key");
        if (key == null || key.isEmpty()) {
            key = System.getenv("CONFIG_ENCRYPT_KEY");
        }
        if (key == null || key.isEmpty()) {
            throw new IllegalStateException("缺少主密钥 config.encrypt.key / CONFIG_ENCRYPT_KEY");
        }
        return key;
    }

    /**
     * 值带 ENC(...) 前缀则解密，否则原样返回
     */
    public static String tryDecrypt(String value) {
        if (!isEncrypted(value)) {
            return value;
        }
        try {
            return decrypt(value.substring(PREFIX.length(), value.length() - SUFFIX.length()), resolveMasterKey());
        } catch (Exception e) {
            throw new IllegalStateException("配置解密失败: " + e.getMessage(), e);
        }
    }

    public static String encrypt(String plain, String masterKey) throws Exception {
        byte[] key = deriveKey(masterKey);
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
        byte[] data = cipher.doFinal(plain.getBytes("UTF-8"));
        byte[] out = Arrays.copyOf(iv, iv.length + data.length);
        System.arraycopy(data, 0, out, iv.length, data.length);
        return Base64.getEncoder().encodeToString(out);
    }

    public static String decrypt(String enc, String masterKey) throws Exception {
        byte[] all = Base64.getDecoder().decode(enc);
        if (all.length < 17) {
            throw new IllegalArgumentException("密文格式错误");
        }
        byte[] key = deriveKey(masterKey);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(Arrays.copyOfRange(all, 0, 16)));
        return new String(cipher.doFinal(Arrays.copyOfRange(all, 16, all.length)), "UTF-8");
    }

    private static byte[] deriveKey(String masterKey) throws Exception {
        return Arrays.copyOf(MessageDigest.getInstance("SHA-256").digest(masterKey.getBytes("UTF-8")), 16);
    }
}
