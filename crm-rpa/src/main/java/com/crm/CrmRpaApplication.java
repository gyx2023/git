package com.crm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

/**
 * 启动程序
 *
 * @author crm
 */
@Slf4j
@SpringBootApplication
@EnableRabbit
public class CrmRpaApplication {

    public static void main(String[] args) {
        System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication application = new SpringApplication(CrmRpaApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        log.info("zqzt_backend_rpa启动成功");
    }

}
