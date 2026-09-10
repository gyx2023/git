package com.crm.adapter.health;
import cn.dev33.satoken.annotation.SaIgnore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/zqzt_backend/health")
@Slf4j
public class HealthController {
    @SaIgnore
    @GetMapping("/check")
    public String check() {
        return "@@success@@";
    }
}
