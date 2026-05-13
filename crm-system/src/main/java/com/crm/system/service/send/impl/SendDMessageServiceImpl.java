package com.crm.system.service.send.impl;

import com.crm.system.service.send.ISendDMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SendDMessageServiceImpl implements ISendDMessageService {
    @Override
    public void initNoticeMessage(List<Integer> userIds, String message) {

    }
}
