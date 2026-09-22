package com.crm.system.client.flameguardian;

import com.alibaba.fastjson.JSON;
import com.crm.common.exception.ServiceException;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dto.FlameGuardianOrderReqDTO;
import com.crm.system.domain.dto.FlameGuardianRespDTO;
import com.crm.system.service.sys.ISysOperLogService;
import com.crm.system.service.sys.ISysParamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 火焰卫士客户端
 * <p>
 * 调用网关「写入受理单信息」接口，把台账数据以覆盖方式写入火焰卫士。
 * 接口地址与凭证取自 sys_param（type = {@value #PARAM_TYPE}）
 * </p>
 * <p>
 * 签名规则：把 path、timestamp、version 按字段名自然序拼成
 * {@code key + value} 的串，末尾直接追加 secretKey，再取 MD5 十六进制大写。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlameGuardianApiClient {

    /** sys_param 配置类型 */
    private static final String PARAM_TYPE = "flame_guardian";
    private static final String PARAM_CODE_URL = "flame_guardian_url";
    private static final String PARAM_CODE_APP_KEY = "flame_guardian_appKey";
    private static final String PARAM_CODE_SECRET_KEY = "flame_guardian_secretKey";

    /** 签名版本号，网关当前为固定值 */
    private static final String SIGN_VERSION = "1.0.0";

    /** 请求头 / 签名字段名 */
    private static final String FIELD_PATH = "path";
    private static final String FIELD_TIMESTAMP = "timestamp";
    private static final String FIELD_VERSION = "version";

    /** 操作日志标题 */
    private static final String OPER_TITLE = "火焰卫士-写入受理单信息";

    private final RestTemplate restTemplate;
    private final ISysParamService sysParamService;
    private final ISysOperLogService sysOperLogService;

    /**
     * 写入受理单信息（覆盖方式）
     *
     * @param req 受理单信息
     * @return 响应，含 code 与 message
     */
    public FlameGuardianRespDTO applyOrder(FlameGuardianOrderReqDTO req) {
        String url = loadParam(PARAM_CODE_URL);
        String appKey = loadParam(PARAM_CODE_APP_KEY);
        String secretKey = loadParam(PARAM_CODE_SECRET_KEY);
        String path = resolvePath(url);

        String respJson = sysOperLogService.logApiCall(OPER_TITLE, url, JSON.toJSONString(req),
            () -> post(url, path, appKey, secretKey, req));
        FlameGuardianRespDTO resp = JSON.parseObject(respJson, FlameGuardianRespDTO.class);
        if (resp == null) {
            throw new ServiceException("火焰卫士接口返回内容无法解析：" + respJson);
        }
        return resp;
    }

    /** 发起请求：签名随每次调用重新生成（timestamp 为 13 位毫秒） */
    private String post(String url, String path, String appKey, String secretKey, FlameGuardianOrderReqDTO req) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("appKey", appKey);
        headers.set(FIELD_TIMESTAMP, timestamp);
        headers.set(FIELD_VERSION, SIGN_VERSION);
        headers.set("sign", buildSign(secretKey, timestamp, path));
        // 取 byte[] 再按 UTF-8 解码，避免响应含中文时按 ISO-8859-1 解析出乱码
        byte[] body = restTemplate.postForObject(url, new HttpEntity<>(req, headers), byte[].class);
        return body == null ? null : new String(body, StandardCharsets.UTF_8);
    }

    /**
     * 生成 sign：path + timestamp + version 按字段名排序后拼接 key 与 value，末尾追加 secretKey，MD5 大写
     */
    private String buildSign(String secretKey, String timestamp, String path) {
        Map<String, String> fields = new HashMap<>(4);
        fields.put(FIELD_PATH, path);
        fields.put(FIELD_TIMESTAMP, timestamp);
        fields.put(FIELD_VERSION, SIGN_VERSION);
        String signString = fields.keySet().stream()
            .sorted()
            .map(key -> key + fields.get(key))
            .collect(Collectors.joining())
            .concat(secretKey);
        return DigestUtils.md5DigestAsHex(signString.getBytes(StandardCharsets.UTF_8)).toUpperCase();
    }

    /** 解析 path */
    private String resolvePath(String url) {
        URI uri;
        try {
            uri = URI.create(url.trim());
        } catch (IllegalArgumentException e) {
            throw new ServiceException("火焰卫士接口地址不合法，请检查 sys_param 配置：" + url);
        }
        String path = uri.getPath();
        if (StringUtils.isBlank(path)) {
            throw new ServiceException("火焰卫士接口地址缺少 path，请检查 sys_param 配置：" + url);
        }
        return path;
    }

    /** 读取 sys_param 配置 */
    private String loadParam(String code) {
        String value = sysParamService.findValueByCode(code);
        if (StringUtils.isBlank(value)) {
            throw new ServiceException("火焰卫士接口配置缺失，请检查 sys_param 配置（type=" + PARAM_TYPE
                + ", code=" + code + "）");
        }
        return value.trim();
    }
}
