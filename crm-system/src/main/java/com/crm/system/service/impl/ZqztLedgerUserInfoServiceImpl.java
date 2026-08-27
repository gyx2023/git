package com.crm.system.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.crm.common.utils.StringUtils;
import com.crm.system.client.unibss.UniBssApiClient;
import com.crm.system.client.unibss.UniBssApiClient.UniBssResponse;
import com.crm.system.service.IZqztLedgerUserInfoService;
import com.crm.system.service.sys.ISysParamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 台账 用户信息查询 服务实现
 * 调用总部数字化能力开放平台「用户信息查询接口」
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZqztLedgerUserInfoServiceImpl implements IZqztLedgerUserInfoService {

    /** sys_param 配置 type：用户信息查询 URL */
    private static final String LEDGER_QUERY_TYPE = "ledger_query";
    /** 用户信息查询 URL 的 code_ */
    private static final String URL_KEY = "ledger_userInfo_query_url";
    /** 通用 APP_ID/APP_SECRET 的 type */
    private static final String GROUP_SYNC_TYPE = "group_sync_param";
    /** APP_ID 的 code_ */
    private static final String APP_ID_KEY = "api_appId";
    /** APP_SECRET 的 code_ */
    private static final String APP_KEY_KEY = "api_appKey";

    /** 入参业务节点名 */
    private static final String REQ_NODE = "QRYPE_INFO_REQ";
    /** 出参业务节点名 */
    private static final String RSP_NODE = "QRYPE_INFO_RSP";

    private final ISysParamService sysParamService;
    private final UniBssApiClient uniBssApiClient;

    @Override
    public String queryUserInDate(String serialNumber) {
        if (StringUtils.isBlank(serialNumber)) {
            return null;
        }
        try {
            Map<String, String> queryCfg = sysParamService.findValueMapsByType(LEDGER_QUERY_TYPE);
            Map<String, String> groupCfg = sysParamService.findValueMapsByType(GROUP_SYNC_TYPE);
            String url = queryCfg.get(URL_KEY);
            String appId = groupCfg.get(APP_ID_KEY);
            String appKey = groupCfg.get(APP_KEY_KEY);
            if (StringUtils.isBlank(url) || StringUtils.isBlank(appId) || StringUtils.isBlank(appKey)) {
                log.warn("用户信息查询接口配置缺失, url={}, appId={}", url, appId);
                return null;
            }

            // 1. 签名头（APP_ID/TRANS_ID/TIMESTAMP/TOKEN）
            Map<String, Object> head = UniBssApiClient.buildHead(appId, appKey);

            // 2. 报文体：QRYPE_INFO_REQ.REQ.SERIAL_NUMBER
            JSONObject req = new JSONObject();
            req.put("SERIAL_NUMBER", serialNumber.trim());
            JSONObject reqNode = new JSONObject();
            reqNode.put("REQ", req);
            Map<String, Object> bodyMap = new java.util.HashMap<>();
            bodyMap.put(REQ_NODE, reqNode);

            // 3. POST
            UniBssResponse resp = uniBssApiClient.post(url, head, bodyMap);
            return parseInDate(resp);
        } catch (Exception e) {
            log.error("用户信息查询失败, serialNumber={}", serialNumber, e);
            return null;
        }
    }

    /** 解析 IN_DATE：HEAD.RESP_CODE=00000 时，按文档结构直接取 DATA[0].IN_DATE（原样返回） */
    private String parseInDate(UniBssResponse resp) {
        if (resp == null || resp.getRawJson() == null) {
            return null;
        }
        JSONObject raw = resp.getRawJson();
        JSONObject head = raw.getJSONObject("UNI_BSS_HEAD");
        if (head == null || !"00000".equals(head.getString("RESP_CODE"))) {
            return null;
        }
        // 出参结构：UNI_BSS_BODY.QRYPE_INFO_RSP.RSP.DATA[].IN_DATE
        JSONObject body = raw.getJSONObject("UNI_BSS_BODY");
        if (body == null) {
            return null;
        }
        JSONObject rspNode = body.getJSONObject(RSP_NODE);
        if (rspNode == null) {
            return null;
        }
        JSONObject rsp = rspNode.getJSONObject("RSP");
        if (rsp == null) {
            return null;
        }
        JSONArray data = rsp.getJSONArray("DATA");
        if (data == null || data.isEmpty()) {
            return null;
        }
        JSONObject first = data.getJSONObject(0);
        return first == null ? null : first.getString("IN_DATE");
    }
}
