package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.DateUtils;
import com.crm.system.domain.dao.DingMessage;
import com.crm.system.mapper.DingMessageMapper;
import com.crm.system.service.IDingMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class DingMessageServiceImpl implements IDingMessageService {

    @Resource
    private DingMessageMapper dingMessageMapper;

    @Override
    public void saveDingMessage(DingMessage dingMessage) {
        dingMessageMapper.insert(dingMessage);
    }

    /**
     * 获取要发送的短信
     */
    @Override
    public List<DingMessage> findNotSendMsgs() {
        LambdaQueryWrapper<DingMessage> queryWrapper = new LambdaQueryWrapper<DingMessage>()
            .isNull(DingMessage::getShortSend)
            .le(DingMessage::getSettingSendTime, DateUtils.getNowDate())
            .orderByAsc(DingMessage::getId);
        return dingMessageMapper.selectList(queryWrapper);
    }

    @Override
    public void saveOrUpdate(DingMessage message) {
        dingMessageMapper.insertOrUpdate(message);
    }
}
