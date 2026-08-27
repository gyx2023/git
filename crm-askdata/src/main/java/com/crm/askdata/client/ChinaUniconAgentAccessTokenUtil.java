package com.crm.askdata.client;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

/**
 * ChinaUnicon 智能体 accessToken 签名工具。
 *
 * @author crm
 */
public final class ChinaUniconAgentAccessTokenUtil {

    private ChinaUniconAgentAccessTokenUtil() {
    }

    public static String generateAccessToken(String appId, String timestamp, String teamId,
                                             String sk, String conversationId, String content,
                                             Map<String, String> variable) {
        String input = safe(appId)
            + safe(timestamp)
            + safe(teamId)
            + safe(sk)
            + safe(teamId)
            + safe(conversationId)
            + safe(content)
            + convertVariableToString(variable);
        return md5(input);
    }

    public static String convertVariableToString(Map<String, String> variable) {
        if (variable == null || variable.isEmpty()) {
            return "";
        }

        Map<String, String> sorted = new TreeMap<>(variable);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm unavailable", e);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
