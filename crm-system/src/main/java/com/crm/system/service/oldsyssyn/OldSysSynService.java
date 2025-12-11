package com.crm.system.service.oldsyssyn;

import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface OldSysSynService {


    void autoLogin(String ssoToken, String userId, String redirect, HttpServletRequest request, HttpServletResponse response);
}
