package com.crm.askdata.security;

import com.crm.askdata.entity.AuthConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
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
     * SA_TOKEN 模式：携带当前请求的 Token 并转发额外 Header
     * <p>
     * auth_config_json 格式:
     * {
     *   "headerName": "Authorization",
     *   "headerPrefix": "Bearer ",
     *   "forwardHeaders": ["loginoa", "loginname"]
     * }
     * <p>
     * forwardHeaders 指定需要从当前请求上下文转发到目标 API 的 Header 名称列表。
     * 这些 Header 的值来自 HeaderContextHolder（由 RouterService 在异步前捕获）。
     * 常用于透传用户身份信息（OA、姓名等）到下游服务。
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

        // 转发额外 Header（如 loginoa、loginname 等用户身份信息）
        List<String> forwardHeaders = parseForwardHeaders(authConfig.getAuthConfigJson());
        if (!forwardHeaders.isEmpty()) {
            Map<String, String> ctxHeaders = HeaderContextHolder.getAll();
            for (String fh : forwardHeaders) {
                String value = ctxHeaders.get(fh);
                if (value != null && !value.isEmpty()) {
                    headers.set(fh, value);
                    log.debug("Forwarded header: {}={}", fh, value);
                }
            }
        }
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
     * 解析 auth_config_json 中的 forwardHeaders 数组
     * <p>
     * 从 SA_TOKEN 的 auth_config_json 中提取 forwardHeaders 配置，
     * 指定需要从当前请求转发到目标 API 的 Header 名称列表。
     *
     * @param json auth_config_json 字符串
     * @return forwardHeaders 列表，无配置时返回空列表
     */
    private static List<String> parseForwardHeaders(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);
            JsonNode fh = root.get("forwardHeaders");
            if (fh != null && fh.isArray()) {
                List<String> result = new ArrayList<>();
                for (JsonNode n : fh) {
                    result.add(n.asText());
                }
                return result;
            }
        } catch (Exception e) {
            log.warn("Failed to parse forwardHeaders from auth_config_json: {}", e.getMessage());
        }
        return Collections.emptyList();
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
