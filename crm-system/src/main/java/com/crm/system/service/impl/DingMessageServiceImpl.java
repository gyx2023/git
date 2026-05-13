package com.crm.system.service.impl;

import com.crm.system.domain.DingMessage;
import com.crm.system.mapper.DingMessageMapper;
import com.crm.system.service.IDingMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class DingMessageServiceImpl implements IDingMessageService {
    
    @Resource
    private DingMessageMapper dingMessageMapper;

    @Override
    public void saveDingMessage(DingMessage dingMessage) {
        dingMessageMapper.insert(dingMessage);
    }
}
