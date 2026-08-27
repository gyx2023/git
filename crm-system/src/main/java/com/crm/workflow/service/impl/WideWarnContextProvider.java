package com.crm.workflow.service.impl;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 宽带/双线到期预警（Wide / double_wide / double_wide_second）业务上下文提供者
 *
 * @author crm
 */
@Component
public class WideWarnContextProvider extends AbstractWideWarnContextProvider {

    @Override
    protected Set<String> getOrderTypes() {
        return new HashSet<>(Arrays.asList("Wide", "double_wide", "double_wide_second"));
    }
    @Override
    protected String getSectionKey() { return "broadband"; }
    @Override
    protected String getSectionTitle() { return "宽带信息"; }
    @Override
    protected String getReminderText() { return "宽带/双线服务即将到期"; }
}
