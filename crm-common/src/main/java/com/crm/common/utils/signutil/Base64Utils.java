package com.crm.common.utils.signutil;

import org.apache.commons.codec.binary.Base64;

import java.io.*;

/**
 * Base64工具类（从asiainfo-openplatform搬迁）
 */
public class Base64Utils {

    private static final int CACHE_SIZE = 2048;

    public Base64Utils() {
    }

    /**
     * Base64解码
     */
    public static byte[] decode(String base64String) throws Exception {
        return Base64.decodeBase64(base64String.getBytes());
    }

    /**
     * Base64编码
     */
    public static String encode(byte[] data) throws Exception {
        return new String(Base64.encodeBase64(data));
    }

    /**
     * 文件转Base64字符串
     */
    public static String encodeFile(String filePath) throws Exception {
        byte[] data = fileToByte(filePath);
        return encode(data);
    }

    /**
     * Base64字符串解码并保存到文件
     */
    public static void decodeToFile(String base64String, String filePath) throws Exception {
        byte[] data = decode(base64String);
        byteArrayToFile(data, filePath);
    }

    /**
     * 文件转字节数组
     */
    public static byte[] fileToByte(String filePath) throws Exception {
        byte[] data = new byte[0];
        File file = new File(filePath);
        
        if (file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(CACHE_SIZE);
            
            byte[] buffer = new byte[1024];
            int len;
            
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
                baos.flush();
            }
            
            baos.close();
            fis.close();
            data = baos.toByteArray();
        }
        
        return data;
    }

    /**
     * 字节数组保存为文件
     */
    public static void byteArrayToFile(byte[] data, String filePath) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        File file = new File(filePath);
        
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        
        file.createNewFile();
        
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int len;
        
        while ((len = bis.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
            fos.flush();
        }
        
        fos.close();
        bis.close();
    }
}
