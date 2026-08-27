package com.crm.system.service.oldsyssyn;

import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface OldSysSynService {


    void autoLogin(String ssoToken, String userId, String redirect, HttpServletRequest request, HttpServletResponse response);

    /**
     * 按老系统用户ID执行登录（同步权限+构建登录信息+Sa-Token登录），返回登录token
     * 供集团平台统一跳转 loginCheck 复用
     *
     * @param userId 老系统用户ID（sys_user.id）
     * @return Sa-Token 登录token
     */
    String loginByOldUserId(Long userId);
}
