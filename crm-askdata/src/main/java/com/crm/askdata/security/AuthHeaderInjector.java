package com.crm.askdata.security;

import com.crm.askdata.entity.AuthConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * 认证 Header 注入工具类
 * <p>
 * 根据 AuthConfig 的 auth_type 和 auth_config_json，向 HttpHeaders 注入认证信息。
 * 无框架依赖，静态方法，各模块可直接调用。
 * <p>
 * 支持的认证方式:
 * - NONE: 不注入
 * - SA_TOKEN: 携带当前用户 Token（需外部传入）
 * - API_KEY: 静态 API Key
 * - BASIC: HTTP Basic Auth
 * - OAUTH2_CLIENT: OAuth2 客户端凭证模式
 *
 * @author crm
 */
@Slf4j
public final class AuthHeaderInjector {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private AuthHeaderInjector() {
        // 工具类，禁止实例化
    }

    /**
     * 向 HttpHeaders 注入认证信息
     *
     * @param headers       目标 HttpHeaders
     * @param authConfig    认证配置
     * @param currentToken  当前用户 Token（SA_TOKEN 模式需传入，可 null）
     */
    public static void inject(HttpHeaders headers, AuthConfig authConfig, String currentToken) {
        if (authConfig == null || authConfig.getAuthType() == null) {
            return;
        }

        String authType = authConfig.getAuthType();

        try {
            switch (authType) {
                case "SA_TOKEN":
                    injectSaToken(headers, authConfig, currentToken);
                    break;
                case "API_KEY":
                    injectApiKey(headers, authConfig);
                    break;
                case "BASIC":
                    injectBasicAuth(headers, authConfig);
                    break;
                case "OAUTH2_CLIENT":
                    injectOAuth2Bearer(headers, authConfig);
                    break;
                case "NONE":
                    // 无需认证
                    break;
                default:
                    log.warn("Unsupported auth_type: {}, system={}", authType, authConfig.getSystemName());
            }
        } catch (Exception e) {
            log.error("Failed to inject auth header: auth_type={}, system={}, error={}",
                authType, authConfig.getSystemName(), e.getMessage());
        }
    }

    /**
     * SA_TOKEN 模式：携带当前请求的 Token
     * <p>
     * auth_config_json 格式:
     * {"headerName": "Authorization", "headerPrefix": "Bearer "}
     */
    private static void injectSaToken(HttpHeaders headers, AuthConfig authConfig, String currentToken) {
        if (currentToken == null || currentToken.isEmpty()) {
            log.warn("SA_TOKEN mode but currentToken is null, skip auth injection, system={}",
                authConfig.getSystemName());
            return;
        }

        Map<String, String> cfg = parseConfig(authConfig.getAuthConfigJson());
        String headerName = cfg != null ? cfg.getOrDefault("headerName", "Authorization") : "Authorization";
        String headerPrefix = cfg != null ? cfg.getOrDefault("headerPrefix", "Bearer ") : "Bearer ";

        headers.set(headerName, headerPrefix + currentToken);
    }

    /**
     * API_KEY 模式：使用静态 API Key
     * <p>
     * auth_config_json 格式:
     * {"headerName": "X-API-Key", "apiKey": "sk-xxxx"}
     */
    private static void injectApiKey(HttpHeaders headers, AuthConfig authConfig) {
        Map<String, String> cfg = parseConfig(authConfig.getAuthConfigJson());
        if (cfg == null || !cfg.containsKey("apiKey")) {
            log.warn("API_KEY mode but apiKey not configured, system={}", authConfig.getSystemName());
            return;
        }

        String headerName = cfg.getOrDefault("headerName", "X-API-Key");
        String apiKey = cfg.get("apiKey");

        headers.set(headerName, apiKey);
    }

    /**
     * BASIC 模式：HTTP Basic Auth
     * <p>
     * auth_config_json 格式:
     * {"username": "admin", "password": "pass"}
     */
    private static void injectBasicAuth(HttpHeaders headers, AuthConfig authConfig) {
        Map<String, String> cfg = parseConfig(authConfig.getAuthConfigJson());
        if (cfg == null || !cfg.containsKey("username") || !cfg.containsKey("password")) {
            log.warn("BASIC mode but username/password not configured, system={}", authConfig.getSystemName());
            return;
        }

        String username = cfg.get("username");
        String password = cfg.get("password");
        String auth = username + ":" + password;
        byte[] encoded = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));

        headers.set("Authorization", "Basic " + new String(encoded, StandardCharsets.UTF_8));
    }

    /**
     * OAUTH2_CLIENT 模式：Bearer Token
     * <p>
     * auth_config_json 格式:
     * {"tokenUrl": "...", "clientId": "...", "clientSecret": "...", "scopes": ["read"]}
     * <p>
     * 注意：当前实现直接使用配置的 accessToken 字段
     * 生产环境建议集成 Token 管理（获取 + 缓存 + 刷新）
     */
    private static void injectOAuth2Bearer(HttpHeaders headers, AuthConfig authConfig) {
        Map<String, String> cfg = parseConfig(authConfig.getAuthConfigJson());
        if (cfg == null || !cfg.containsKey("accessToken")) {
            log.warn("OAUTH2_CLIENT mode but accessToken not configured, system={}", authConfig.getSystemName());
            return;
        }

        headers.set("Authorization", "Bearer " + cfg.get("accessToken"));
    }

    /**
     * 解析 auth_config_json
     */
    private static Map<String, String> parseConfig(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse auth_config_json: {}", e.getMessage());
            return null;
        }
    }
}
