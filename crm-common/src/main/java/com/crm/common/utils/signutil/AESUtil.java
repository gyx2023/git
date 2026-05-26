package com.crm.common.utils.signutil;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;

/**
 * AES加密工具类（从asiainfo-openplatform搬迁）
 */
public class AESUtil {

    private static final String UTF_8 = "UTF-8";
    private static final String alogrithm = "AES";

    private CipherMode cipherMode;
    private PaddingMode paddingMode;
    private SecretKeySpec skeySpec;
    private IvParameterSpec ivParamSpec;
    private Cipher cipher;
    private String AES_CIPHER_PADDING_MODE;
    private boolean simple;

    /**
     * 加密模式枚举
     */
    public enum CipherMode {
        ECB, CBC
    }

    /**
     * 填充模式枚举
     */
    public enum PaddingMode {
        PKCS5Padding, PKCS7Padding, NoPadding
    }

    /**
     * 构造函数
     */
    public AESUtil(CipherMode cipherMode, PaddingMode paddingMode, 
                   String key, String iv, boolean simple) 
            throws NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException {
        this.simple = simple;
        
        if (simple) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", new BouncyCastleProvider());
            keyGenerator.init(128, new SecureRandom());
            // 简化模式使用随机密钥
        } else {
            byte[] keyBytes = key.getBytes(UTF_8);
            this.skeySpec = new SecretKeySpec(keyBytes, "AES");
            
            if (cipherMode == CipherMode.CBC && iv != null && !iv.isEmpty()) {
                this.ivParamSpec = new IvParameterSpec(iv.getBytes(UTF_8));
            }
        }
        
        this.cipherMode = cipherMode;
        this.paddingMode = paddingMode;
        
        if (paddingMode == PaddingMode.PKCS7Padding) {
            AES_CIPHER_PADDING_MODE = "AES/ECB/PKCS7Padding";
        } else if (paddingMode == PaddingMode.PKCS5Padding) {
            AES_CIPHER_PADDING_MODE = "AES/ECB/PKCS5Padding";
        } else {
            AES_CIPHER_PADDING_MODE = "AES/ECB/NoPadding";
        }
        
        if (cipherMode == CipherMode.CBC) {
            AES_CIPHER_PADDING_MODE = AES_CIPHER_PADDING_MODE.replace("ECB", "CBC");
        }
        
        this.cipher = Cipher.getInstance(AES_CIPHER_PADDING_MODE);
    }

    /**
     * 加密字符串
     */
    public String encrypt(String content) throws Exception {
        if (cipherMode == CipherMode.ECB) {
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParamSpec);
        }
        
        byte[] contentBytes = content.getBytes(UTF_8);
        byte[] encrypted = cipher.doFinal(contentBytes);
        return bytesToHexString(encrypted);
    }

    /**
     * 解密字符串
     */
    public String decrypt(String hexContent) throws Exception {
        if (cipherMode == CipherMode.ECB) {
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParamSpec);
        }
        
        byte[] contentBytes = hexStringToBytes(hexContent);
        byte[] decrypted = cipher.doFinal(contentBytes);
        return new String(decrypted, UTF_8);
    }

    public CipherMode getCipherMode() {
        return cipherMode;
    }

    public PaddingMode getPaddingMode() {
        return paddingMode;
    }

    /**
     * 十六进制字符串转字节数组
     */
    public static byte[] hexStringToBytes(String hexString) {
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] bytes = new byte[length];
        
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            bytes[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        
        return bytes;
    }

    /**
     * 字节数组转十六进制字符串
     */
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 字符转字节
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * MD5摘要
     */
    public static String getMD5Str(String content) 
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        md.reset();
        md.update(content.getBytes(UTF_8));
        byte[] digest = md.digest();
        return bytesToHexString(digest);
    }

    public static String getAlogrithm() {
        return "AES";
    }
}
