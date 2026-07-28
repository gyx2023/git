package com.crm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

@Slf4j
@SpringBootApplication(exclude = {
        RabbitAutoConfiguration.class
})
public class JobServeApplication {

    public static void main(String[] args) {
        System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication application = new SpringApplication(JobServeApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        log.info("zqzt_backend_job启动成功");
    }
}
