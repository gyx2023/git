package com.crm.askdata.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.askdata.config.AgentProvider;
import com.crm.askdata.config.ChinaUniconAgentProperties;
import com.crm.system.domain.dao.SysParamDo;
import com.crm.system.mapper.param.SysParamMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 智能问数平台选择服务。
 * <p>
 * 优先读取 sys_param.code_=ASKDATA_AGENT_PROVIDER，支持运行时切换；
 * 表中未配置或配置非法时，回退到 application-askdata.yml 中的 ChinaUnicon 开关。
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AskDataAgentProviderService {

    public static final String PROVIDER_PARAM_CODE = "ASKDATA_AGENT_PROVIDER";

    private final SysParamMapper sysParamMapper;
    private final ChinaUniconAgentProperties chinaUniconAgentProperties;

    public AgentProvider resolveIntentProvider() {
        AgentProvider configured = resolveFromSysParam();
        if (configured != null) {
            return configured;
        }
        return chinaUniconAgentProperties.isIntentEnabled()
            ? AgentProvider.CHINA_UNICON
            : AgentProvider.HIAGENT;
    }

    public AgentProvider resolveResultProvider() {
        AgentProvider configured = resolveFromSysParam();
        if (configured != null) {
            return configured;
        }
        return chinaUniconAgentProperties.isResultEnabled()
            ? AgentProvider.CHINA_UNICON
            : AgentProvider.HIAGENT;
    }

    private AgentProvider resolveFromSysParam() {
        String value = loadProviderValue();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalized = value.trim().toLowerCase();
        if (AgentProvider.HIAGENT.getConfigValue().equals(normalized)) {
            return AgentProvider.HIAGENT;
        }
        if (AgentProvider.CHINA_UNICON.getConfigValue().equals(normalized)) {
            return AgentProvider.CHINA_UNICON;
        }

        log.warn("Invalid sys_param {}={}, fallback to yaml config. Expected: hiagent/china_unicon",
            PROVIDER_PARAM_CODE, value);
        return null;
    }

    private String loadProviderValue() {
        try {
            List<SysParamDo> params = sysParamMapper.selectList(
                new LambdaQueryWrapper<SysParamDo>()
                    .eq(SysParamDo::getCode, PROVIDER_PARAM_CODE)
            );
            if (params == null || params.isEmpty()) {
                return null;
            }
            return params.get(0).getValue();
        } catch (Exception e) {
            log.warn("Failed to load sys_param {}, fallback to yaml config: {}",
                PROVIDER_PARAM_CODE, e.getMessage());
            return null;
        }
    }
}
