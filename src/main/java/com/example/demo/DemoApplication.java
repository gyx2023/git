package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 程序入口。
 *
 * @SpringBootApplication 是一个"三合一"的注解：
 *   1. 声明这是 Spring Boot 应用，自动帮你配置好一大堆默认设置（自动配置）；
 *   2. 开启组件扫描：自动把 com.example.demo 包及其子包下
 *      带 @Component / @Service / @RestController 的类创建成对象（术语叫 Bean）交给 Spring 管理；
 *   3. 声明这是一个配置类。
 *
 * 注意：这个类要放在所有代码的最外层包（这里是 com.example.demo），
 * 否则扫描不到子包里的类，这是新手最常见的"接口莫名其妙 404"的原因。
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        // 这一行会启动 Spring 容器，并顺带启动内嵌的 Tomcat 服务器（默认端口 8080）。
        // 项目跑起来后，进程不会退出，会一直在这里等着接收浏览器/客户端的请求。
        SpringApplication.run(DemoApplication.class, args);
    }
}
