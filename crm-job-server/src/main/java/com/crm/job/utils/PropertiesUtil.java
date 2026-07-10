package com.crm.job.utils;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class PropertiesUtil {
    public static Map<String, String> getPropertiesMap(String fileName) {
        ResourceBundle resourcesTable = ResourceBundle.getBundle(fileName);
        Enumeration<?> en = resourcesTable.getKeys();
        Map<String, String> map = new HashMap<String, String>();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            String Property = resourcesTable.getString(key);
            try {
                map.put(key, new String((Property).getBytes("ISO-8859-1"), "UTF-8"));
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }
        return map;

    }

    public static String getPropertyValueByKey(String propertyFileName, String key) {
        String value = "";
        try {
            ResourceBundle resourcesTable = ResourceBundle.getBundle(propertyFileName);
            String Property = resourcesTable.getString(key);
            value = new String((Property).getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value;
    }

    public static void main(String args[]) {
        Map<String, String> map = PropertiesUtil.getPropertiesMap("itf_ftp");
        String ftpUserName = map.get("ftp_default_pwd");
        log.info(ftpUserName);
    }
}
