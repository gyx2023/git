package com.crm.system.utils;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HaoKaoSignUtil {

    public static final String _app_id = "APP_ID";
    public static final String _trans_id = "TRANS_ID";
    public static final String _timestamp = "TIMESTAMP";
    public static final String _token = "TOKEN";
    public static final String _key = "key";
    public static final SimpleDateFormat TIMESTAMP_SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
    public static final SimpleDateFormat SERIAL_NUMBER_SDF = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    public static void buildAppParams(Map<String, Object> map) throws NoSuchAlgorithmException, UnsupportedEncodingException{
        Date date = new Date();
        SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
        parameters.put(_app_id, map.get(_app_id));
        if(map.containsKey(_trans_id) && map.get(_trans_id) != null && !"".equals(map.get(_trans_id))){
            parameters.put(_trans_id, map.get(_trans_id));
        }else{
            String serialNumber = SERIAL_NUMBER_SDF.format(date) + String.valueOf(1000000 + (int)(Math.random()*1000000)).substring(1);
            parameters.put(_trans_id, serialNumber);
            map.put(_trans_id, serialNumber);
        }

        if(map.containsKey(_timestamp) && map.get(_timestamp) != null && !"".equals(map.get(_timestamp))){
            parameters.put(_timestamp, map.get(_timestamp));
        }else{
            String timestamp = TIMESTAMP_SDF.format(date);
            parameters.put(_timestamp, timestamp);
            map.put(_timestamp, timestamp);
        }
        parameters.put(_key, map.get(_key));

        String token = createSign("UTF-8",parameters);

        map.put(_token, token);
        map.remove(_key);
    }
    /**
     * @Description：sign签名
     * @param characterEncoding
     *            编码格式
     * @param parameters
     *            请求参数
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public static String createSign(String characterEncoding,
                                    SortedMap<Object, Object> parameters) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            Object v = entry.getValue();
            if (v instanceof String[]) {
                v = ((String[]) v)[0];
            }
            if (null != v && !"".equals(v) && !_key.equals(k)) {
                sb.append(k).append(v);
            }
        }
        sb.append(parameters.get(_key));
        return getMD5Encode(characterEncoding, sb.toString());
    }



    private static String byteArrayToHexString(byte b[]) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++)
            resultSb.append(byteToHexString(b[i]));

        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n += 256;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /**
     * md5 base64加密
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String getMD5Encode(String characterEncoding, String md5Code) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String resultString = null;
        resultString = new String(md5Code);
        MessageDigest md = MessageDigest.getInstance("MD5");
        resultString = byteArrayToHexString(md.digest(resultString.getBytes(characterEncoding)));
        return resultString;
    }

    private static final String hexDigits[] = { "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

//    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put(_app_id, "eSPtczfyiz");
//        map.put(_key, "hfLOXNVKIRPTx46I4UgpuZj1fmCojoiS");
//        HaoKaoSignUtil.buildAppParams(map);
//        ObjectMapper objectMapper = new ObjectMapper();
//        System.out.println(objectMapper.writeValueAsString(map));
//    }
}
