package com.crm.askdata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能问数嵌入模式配置。
 *
 * @author crm
 */
@Data
@Component
@ConfigurationProperties(prefix = "askdata.embed")
public class AskDataEmbedProperties {

    /** 是否允许无 zqzt 登录态的嵌入模式访问 */
    private boolean enabled = true;

    /** 嵌入模式下是否要求 Authorization */
    private boolean requireAuthHeader = true;

    /** 嵌入模式下是否要求 OA */
    private boolean requireOa = false;

    /** 宿主 Origin 白名单；为空时不校验，便于内网联调 */
    private List<String> allowedOrigins = new ArrayList<>();
}
