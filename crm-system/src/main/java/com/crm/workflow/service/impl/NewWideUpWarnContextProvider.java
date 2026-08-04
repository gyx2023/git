package com.crm.workflow.service.impl;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * 宽带升级预警（new_wide_up）业务上下文提供者
 *
 * @author crm
 */
@Component
public class NewWideUpWarnContextProvider extends AbstractWideWarnContextProvider {

    @Override
    protected Set<String> getOrderTypes() { return Collections.singleton("new_wide_up"); }
    @Override
    protected String getSectionKey() { return "broadbandUpgrade"; }
    @Override
    protected String getSectionTitle() { return "宽带升级信息"; }
    @Override
    protected String getReminderText() { return "宽带升级预警"; }
}
