package com.crm.workflow.service.impl;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * 红名单升级预警（new_hmd_up）业务上下文提供者
 *
 * @author crm
 */
@Component
public class NewHmdUpWarnContextProvider extends AbstractWideWarnContextProvider {

    @Override
    protected Set<String> getOrderTypes() { return Collections.singleton("new_hmd_up"); }
    @Override
    protected String getSectionKey() { return "hmdUp"; }
    @Override
    protected String getSectionTitle() { return "红名单升级信息"; }
    @Override
    protected String getReminderText() { return "红名单到期升级预警"; }
}
