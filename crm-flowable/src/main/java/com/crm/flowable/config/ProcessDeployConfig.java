package com.crm.flowable.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.hutool.core.io.IoUtil;

/**
 * 部署老系统迁移过来的BPMN流程定义
 * 通过classpath:processes/目录下的.bpmn文件自动部署
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessDeployConfig implements ApplicationRunner {

    private final RepositoryService repositoryService;

    /** 提取BPMN XML中的process id（兼容文件名与process id不一致的情况） */
    private static final Pattern PROCESS_ID_PATTERN = Pattern.compile("<process[^>]+id=\"([^\"]+)\"");

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 扫描 classpath:processes/ 目录下的所有 BPMN 文件
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:processes/*.bpmn");

        for (Resource resource : resources) {
            String filename = resource.getFilename();
            log.info("正在部署BPMN流程定义: {}", filename);

            try (InputStream inputStream = resource.getInputStream()) {
                // 读取XML内容到字节数组（需要两次使用：解析Key + 部署）
                byte[] content = IoUtil.readBytes(inputStream);
                String xml = new String(content, StandardCharsets.UTF_8);

                // 从XML中提取真实的process id，避免文件名≠process id导致重复部署
                Matcher matcher = PROCESS_ID_PATTERN.matcher(xml);
                String processKey = matcher.find() ? matcher.group(1)
                        : filename.replace(".bpmn20.xml", "").replace(".bpmn", "");

                long count = repositoryService.createProcessDefinitionQuery()
                        .processDefinitionKey(processKey)
                        .count();

                if (count > 0) {
                    log.info("流程定义 [{}] 已存在，跳过部署", processKey);
                    continue;
                }

                // 部署流程定义
                Deployment deployment = repositoryService.createDeployment()
                        .name("老系统迁移-" + filename)
                        .addInputStream(filename, new ByteArrayInputStream(content))
                        .category("migrated")
                        .deploy();

                log.info("流程定义 [{}] 部署成功, 部署ID: {}", processKey, deployment.getId());
            } catch (Exception e) {
                log.error("部署BPMN流程定义 [{}] 失败", filename, e);
            }
        }
    }
}
