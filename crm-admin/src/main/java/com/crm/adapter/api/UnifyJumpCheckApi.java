package com.crm.adapter.api;

import cn.dev33.satoken.annotation.SaIgnore;
import com.crm.common.core.domain.R;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.utils.StringUtils;
import com.crm.system.client.unibss.UniBssApiClient;
import com.crm.system.service.oldsyssyn.OldSysSynService;
import com.crm.system.service.sys.ISysParamService;
import com.crm.system.service.sys.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 总部政企运营平台统一跳转 - 前端免登接口
 * <p>
 * 与 {@link UnifyJumpApi#loginCheck} 同一免登链路（authSessionId → 总部取手机号 → 查新框架用户 → Sa-Token 登录），
 * 差异：统一返回标准前后端格式 R&lt;T&gt;（code/msg/data），免登失败返回 {@code R.fail(msg)}，
 * 由前端自行跳转登录失败页；后端不构造前端页面地址（页面统一走 zj_gec_sp_new_frontend，接口走 zj_gec_sp_new）。
 * </p>
 * <p>
 * 供前端 /sso/checkLogin 入口页调用，拿到 token 后由前端保存并自行跳转处理页。
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/unifyJump")
public class UnifyJumpCheckApi {

    private final OldSysSynService oldSysSynService;
    private final UniBssApiClient uniBssApiClient;
    private final ISysUserService sysUserService;
    private final ISysParamService sysParamService;

    /**
     * 前端免登校验（对应老框架 UnifyJumpAction.loginCheck 的免登逻辑）
     * <p>
     * 成功：{@code R.ok(data={token})}；失败：{@code R.fail(msg)}，由前端跳转登录失败页展示原因。
     * </p>
     */
    @SaIgnore
    @GetMapping("/checkLogin")
    public R<Map<String, Object>> checkLogin(@RequestParam("authSessionId") String authSessionId) {
        // 1. 总部查询登录信息，取手机号
        String phone = queryLoginPhone(authSessionId);
        if (StringUtils.isBlank(phone)) {
            return R.fail("获取登录用户信息失败");
        }

        // 2. 按手机号查新框架用户（sys_user.phonenumber，主键 user_id 即老系统用户ID）
        SysUser sysUser = sysUserService.selectUserByPhonenumber(phone);
        if (sysUser == null) {
            return R.fail("无用户信息");
        }

        // 3. 免登（同步权限 + Sa-Token 登录）
        String token = oldSysSynService.loginByOldUserId(sysUser.getUserId());
        log.info("unifyJump checkLogin 免登成功: userId={}", sysUser.getUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        return R.ok(data);
    }

    /**
     * 总部查询登录信息：authSessionId → 手机号（与 UnifyJumpApi.queryLoginPhone 同一链路）
     */
    private String queryLoginPhone(String authSessionId) {
        try {
            Map<String, String> params = sysParamService.findValueMapsByType("oppSyncGroupParam");
            Map<String, Object> head = UniBssApiClient.buildHead(
                    params.getOrDefault("appId", ""), params.getOrDefault("appKey", ""));
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.put("authSessionId", authSessionId);
            reqMap.put("appId", "10202");
            Map<String, Object> body = new HashMap<>();
            body.put("QRY_LOGIN_INFO_REQ", reqMap);
            String qryUrl = sysParamService.findValueByCode("grpSpLoginQryUrlNew");
            log.info("总部政企运营平台_查询登录信息请求: url={}, authSessionId={}", qryUrl, authSessionId);
            UniBssApiClient.UniBssResponse resp = uniBssApiClient.post(qryUrl, head, body);
            if (!resp.isSuccess()) {
                log.warn("总部查询登录信息失败: respCode={}, busiCode={}", resp.getRespCode(), resp.getBusiCode());
                return null;
            }
            return resp.getValueByPath("UNI_BSS_BODY.QRY_LOGIN_INFO_RSP.data.govAccountInfo.phone");
        } catch (Exception e) {
            log.error("总部查询登录信息异常", e);
            return null;
        }
    }
}
