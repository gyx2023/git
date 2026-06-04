<<<<<<<< HEAD:crm-admin/src/main/java/com/crm/web/health/HealtheController.java
package com.crm.web.health;
========
package com.crm.adapter.health;
>>>>>>>> origin/fix/package_rename:crm-admin/src/main/java/com/crm/adapter/health/HealthController.java

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;

@RestController
@RequestMapping("/zqzt_backend/health")
@Slf4j
public class HealthController {
    @PermitAll
    @GetMapping("/check")
    public String check() {
        return "@@success@@";
    }
}
