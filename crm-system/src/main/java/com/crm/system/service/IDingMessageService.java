package com.crm.system.service;

import com.crm.system.domain.dao.DingMessage;

import java.util.List;

public interface IDingMessageService {
    void saveDingMessage(DingMessage dingMessage);

    List<DingMessage> findNotSendMsgs();

    void saveOrUpdate(DingMessage message);
}
