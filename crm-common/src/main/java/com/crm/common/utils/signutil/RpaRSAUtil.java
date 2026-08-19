package com.crm.common.utils.signutil;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * Comments:  RSA非对称加密工具类，支持长度超出117的字符串做分段加密处理
 *
 * @author <a href="mailto:wangb108@chinaunicom.cn">wangbing</a>
 * @version 1.0 2020-05-09 15:39
 */
public class RpaRSAUtil {
    private final static Logger logger = LoggerFactory.getLogger(RpaRSAUtil.class);

    /**

     加解密算法关键字
     */
    public static final String KEY_ALGORITHM = "RSA";
    /**

     /**
     默认编码
     */
    public static final String CHARSET = "UTF-8";

    /**
     最大加密字节数，超出最大字节数需要分组加密
     */
    private static int MAX_ENCRYPT_BLOCK = 117;
    /**
     最大解密字节数，超出最大字节数需要分组解密
     */
    private static int MAX_DECRYPT_BLOCK = 256;

    public static String decryptByShort(String inputData, String privateKey) throws Exception {
        //64位解码加密后的字符串
        byte[] inputBytes = Base64.decodeBase64(inputData.getBytes(CHARSET));
        //base64编码的私钥
        byte[] keyBytes = Base64.decodeBase64(privateKey.getBytes(CHARSET));
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        //RSA解密
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        byte[] resultBytes = doFinalBySegment(inputBytes, cipher, MAX_DECRYPT_BLOCK);
        return new String(resultBytes, CHARSET);
    }

    public static String encryptByShort(String inputData, String publicKey) throws Exception {
        // 取得公钥
//        byte[] bytes1 = publicKey.getBytes(CHARSET);
//        byte[] bytes2 = Base64.decodeBase64(publicKey.getBytes(CHARSET));
//        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decodeBase64(publicKey.getBytes(CHARSET)));
        Key key = KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(publicKey.getBytes(CHARSET))));
        // 对数据加密
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] resultBytes = doFinalBySegment(inputData.getBytes(CHARSET), cipher, MAX_ENCRYPT_BLOCK);
        return new String(Base64.encodeBase64(resultBytes), CHARSET);
    }

    private static byte[] doFinalBySegment(byte[] inputBytes, Cipher cipher, int maxLength) throws Exception {
        int inputLenth = inputBytes.length;
        // 标识
        int offSet = 0;
        byte[] cache;
        byte[] resultBytes = {};
        while (inputLenth - offSet > 0) {
            //超出最大字节数分组加密
            if (inputLenth - offSet > maxLength) {
                cache = cipher.doFinal(inputBytes, offSet, maxLength);
                offSet += maxLength;
            } else {
                //直接加密
                cache = cipher.doFinal(inputBytes, offSet, inputLenth - offSet);
                offSet = inputLenth;
            }
            resultBytes = Arrays.copyOf(resultBytes, resultBytes.length + cache.length);
            System.arraycopy(cache, 0, resultBytes, resultBytes.length - cache.length, cache.length);
        }
        return resultBytes;
    }

    /**
     * 加密
     *
     * @param message   需要加密的字符串
     * @param publicKey 公钥
     * @return
     */
    public static String encrypt(final String message, final String publicKey) {
        //base64编码的公钥
        try {
            final byte[] decoded = Base64.decodeBase64(publicKey);
            final RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
            //RSA加密
            final Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);

            final byte[] bytes = message.getBytes("UTF-8");
            final int len = bytes.length;//字符串长度
            int offset = 0;//偏移量
            int i = 0;//所分的段数
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();

            while (len > offset) {
                byte[] cache;
                if (len - offset > 117) {
                    cache = cipher.doFinal(bytes, offset, 117);
                } else {
                    cache = cipher.doFinal(bytes, offset, len - offset);
                }
                bos.write(cache);
                i++;
                offset = 117 * i;
            }
            bos.close();

            return Base64.encodeBase64String(bos.toByteArray());
        } catch (NoSuchAlgorithmException e) {
            logger.error("无此加密算法", e);
        } catch (InvalidKeyException e) {
            logger.error("非法密钥", e);
        } catch (NoSuchPaddingException e) {
            logger.error("无明文数据", e);
        } catch (BadPaddingException e) {
            logger.error("明文数据损坏", e);
        } catch (UnsupportedEncodingException e) {
            logger.error("不支持的编码", e);
        } catch (InvalidKeySpecException e) {
            logger.error("密文格式不正确", e);
        } catch (IllegalBlockSizeException e) {
            logger.error("密文长度非法", e);
        } catch (IOException e) {
            logger.error("分段解密异常", e);
        }
        return "";
    }

    /**
     * 解密
     *
     * @param message    需要解密的密文
     * @param privateKey 私钥
     * @return
     */
    public static String decrypt(String message, final String privateKey) {
        try {
            //base64编码的私钥
            final byte[] decoded = Base64.decodeBase64(privateKey);
            RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            //64位解码加密后的字符串
            final byte[] inputByte = Base64.decodeBase64(message.getBytes("UTF-8"));

            final int len = inputByte.length;//密文
            int offset = 0;//偏移量
            int i = 0;//段数
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while (len - offset > 0) {
                byte[] cache;
                if (len - offset > 128) {
                    cache = cipher.doFinal(inputByte, offset, 128);
                } else {
                    cache = cipher.doFinal(inputByte, offset, len - offset);
                }
                bos.write(cache);
                i++;
                offset = 128 * i;
            }
            bos.close();

            return new String(bos.toByteArray(), "UTF-8");
        } catch (NoSuchAlgorithmException e) {
            logger.error("无此加密算法", e);
        } catch (InvalidKeyException e) {
            logger.error("非法密钥", e);
        } catch (NoSuchPaddingException e) {
            logger.error("无明文数据", e);
        } catch (BadPaddingException e) {
            logger.error("明文数据损坏", e);
        } catch (UnsupportedEncodingException e) {
            logger.error("不支持的编码", e);
        } catch (InvalidKeySpecException e) {
            logger.error("密文格式不正确", e);
        } catch (IllegalBlockSizeException e) {
            logger.error("密文长度非法", e);
        } catch (IOException e) {
            logger.error("分段解密异常", e);
        }
        return "";
    }


}
