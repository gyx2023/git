package com.crm.adapter.admin.healthe;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;

@RestController
@RequestMapping("/zqzt_backend_rpa/health")
@Slf4j
public class HealtheController {
    @SaIgnore
    @GetMapping("/check")
    public String check() {
        return "@@success@@";
    }
}
