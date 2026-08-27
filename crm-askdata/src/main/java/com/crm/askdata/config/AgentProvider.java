package com.crm.askdata.config;

/**
 * 智能问数可选智能体平台。
 *
 * @author crm
 */
public enum AgentProvider {
    HIAGENT("hiagent"),
    CHINA_UNICON("china_unicon");

    private final String configValue;

    AgentProvider(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigValue() {
        return configValue;
    }
}
