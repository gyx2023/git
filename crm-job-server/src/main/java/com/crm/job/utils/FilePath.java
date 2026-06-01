package com.crm.job.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 文件路径配置类
 * 从 application.yml 读取 attach.root 配置
 */
@Data
@Component
public class FilePath {

    /**
     * 从yml读取配置
     */
    @Value("${attach.root}")
    private String root;

    private Long maxSize;

    @Value("${path.temp}")
    private String temp;

    /**
     * 静态变量，方便全局调用
     */
    public static String attachPath;
    public static Long attachMaxSize;
    public static String tempPath;
    public static String rootPath;

    /**
     * Spring 初始化完成后赋值给静态变量
     */
    @PostConstruct
    public void init() {
        attachPath = this.root;
        attachMaxSize = this.maxSize;
        tempPath = this.temp;
        rootPath = this.root;
    }
}
