package com.crm.system.service;

import com.crm.system.domain.DingMessage;

import java.util.List;

public interface IDingMessageService {
    void saveDingMessage(DingMessage dingMessage);

    List<DingMessage> findNotSendMsgs();

    void saveOrUpdate(DingMessage message);
}
