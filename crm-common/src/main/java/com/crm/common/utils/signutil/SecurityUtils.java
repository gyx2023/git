package com.crm.common.utils.signutil;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;

/**
 * 安全工具类（从asiainfo-openplatform搬迁）
 * 提供HmacSHA256、AES、RSA等加密算法支持
 */
@Slf4j
public final class SecurityUtils {

    public static final String ENCODE = "UTF-8";
    public static final String CIPHER_ALGORITHM = "AES/ECB/PKCS7Padding";

    private SecurityUtils() {
    }

    /**
     * 初始化AES256密钥
     */
    public static byte[] initAES256Key() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey.getEncoded();
    }

    /**
     * 初始化HmacSHA256密钥
     */
    public static byte[] initHmacSHA256Key() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
        keyGenerator.init(256);
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey.getEncoded();
    }

    /**
     * HmacSHA256加密
     */
    public static byte[] encodeHmacSHA256(byte[] data, byte[] key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
        Mac mac = Mac.getInstance(secretKeySpec.getAlgorithm());
        mac.init(secretKeySpec);
        return mac.doFinal(data);
    }

    /**
     * HmacSHA256加密并返回大写十六进制字符串
     */
    public static String encodeHmacSHA256HexUpper(String content, byte[] key)
            throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        byte[] data = content.getBytes("UTF-8");
        byte[] result = encodeHmacSHA256(data, key);
        return AESUtil.bytesToHexString(result).toUpperCase(java.util.Locale.US);
    }

    /**
     * AES加密
     */
    public static byte[] encrypt(byte[] data, byte[] key)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
                   InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Security.addProvider(new BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return cipher.doFinal(data);
    }

    /**
     * AES解密
     */
    public static byte[] decrypt(byte[] data, byte[] key)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
                   InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Security.addProvider(new BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        return cipher.doFinal(data);
    }

    /**
     * 字节数组转大写十六进制字符串
     */
    public static String encodeHexUpper(byte[] data) throws UnsupportedEncodingException {
        return AESUtil.bytesToHexString(data).toUpperCase(java.util.Locale.US);
    }

    /**
     * 十六进制字符串解码为字节数组
     */
    public static byte[] decodeHexUpper(String hexString) throws UnsupportedEncodingException {
        return Hex.decode(hexString.toLowerCase(java.util.Locale.US));
    }

    /**
     * 十六进制字符串解码为字符串
     */
    public static String decodeHexUpper(String hexString, String charset)
            throws UnsupportedEncodingException {
        byte[] data = Hex.decode(hexString.toLowerCase(java.util.Locale.US));
        return new String(data, charset);
    }

    /**
     * AES256加密并返回大写十六进制字符串
     */
    public static String encodeAES256HexUpper(String content, byte[] key)
            throws UnsupportedEncodingException, IllegalBlockSizeException,
                   InvalidKeyException, BadPaddingException,
                   NoSuchAlgorithmException, NoSuchPaddingException {
        byte[] data = content.getBytes("UTF-8");
        byte[] encrypted = encrypt(data, key);
        return encodeHexUpper(encrypted);
    }

    /**
     * AES256解密（输入为大写十六进制字符串）
     */
    public static String decodeAES256HexUpper(String hexString, byte[] key)
            throws IllegalBlockSizeException, InvalidKeyException,
                   BadPaddingException, NoSuchAlgorithmException,
                   NoSuchPaddingException, UnsupportedEncodingException {
        byte[] data = Hex.decode(hexString.toLowerCase(java.util.Locale.US));
        byte[] decrypted = decrypt(data, key);
        return new String(decrypted, "UTF-8");
    }

    /**
     * MD5加密并返回大写十六进制字符串
     */
    public static String encodeMd5HexUpper(String content) 
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(content.getBytes("UTF-8"));
        return AESUtil.bytesToHexString(digest).toUpperCase(java.util.Locale.US);
    }

    /**
     * RSA公钥加密
     */
    public static String encryptByPublicKey(String content, String publicKey) throws Exception {
        return RSAUtils.encryptByPublicKey(content, publicKey);
    }

    /**
     * RSA公钥解密
     */
    public static String decryptByPublicKey(String content, String publicKey) throws Exception {
        return RSAUtils.decryptByPublicKey(content, publicKey);
    }

    public static void main(String[] args) throws Exception {
        String content = "test";
        String key = "3888bd16498334490f5eaf4355be982f";
        log.info(encodeHmacSHA256HexUpper(content, decodeHexUpper(key)));
    }
}
