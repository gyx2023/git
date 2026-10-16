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
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

/**
 * 火焰卫士客户端
 * <p>
 * 调用网关「写入受理单信息」接口，把台账数据以覆盖方式写入火焰卫士。
 * 接口地址与凭证取自 sys_param（type = {@value #PARAM_TYPE}）
 * </p>
 * <p>
 * 鉴权方式：
 * {@code Authorization: Bearer <token>}。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlameGuardianApiClient {

    /** sys_param 配置类型 */
    private static final String PARAM_TYPE = "flame_guardian";
    private static final String PARAM_CODE_URL = "flame_guardian_url";
    private static final String PARAM_CODE_TOKEN = "flame_guardian_token";

    /** 鉴权请求头 */
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

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
        String token = loadParam(PARAM_CODE_TOKEN);

        String respJson = sysOperLogService.logApiCall(OPER_TITLE, url, JSON.toJSONString(req),
            () -> post(url, token, req));
        FlameGuardianRespDTO resp = JSON.parseObject(respJson, FlameGuardianRespDTO.class);
        if (resp == null) {
            throw new ServiceException("火焰卫士接口返回内容无法解析：" + respJson);
        }
        return resp;
    }

    /** 发起请求：token 随每次调用从 sys_param 读取，改库后无需重启 */
    private String post(String url, String token, FlameGuardianOrderReqDTO req) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HEADER_AUTHORIZATION, BEARER_PREFIX + token);
        // 取 byte[] 再按 UTF-8 解码，避免响应含中文时按 ISO-8859-1 解析出乱码
        byte[] body = restTemplate.postForObject(url, new HttpEntity<>(req, headers), byte[].class);
        return body == null ? null : new String(body, StandardCharsets.UTF_8);
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
