package com.crm.web.controller.healthe;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;

@RestController
@RequestMapping("/zqzt_backend/health")
@Slf4j
public class HealtheController {
    @PermitAll
    @GetMapping("/check")
    public String check() {
        return "@@success@@";
    }
}
