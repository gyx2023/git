package com.crm.service.mq.ygcf;

import com.crm.common.utils.StringUtils;
import com.crm.dto.BusinessMessageDTO;
import com.crm.dto.BusinessResponseDTO;
import com.crm.system.domain.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.mapper.servicehall.ZqztJtNoticeRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Service
public class RpaConsumerService {

    @Resource
    private ZqztJtNoticeRecordMapper zqztJtNoticeRecordMapper;

    @Resource
    private RabbitMQConsumerService consumerService;

    /**
     * 消费RPA业务消息
     *
     * @param businessMessage 业务消息
     */
    public BusinessResponseDTO receiveRpaMessage(BusinessMessageDTO businessMessage) throws IOException {

        BusinessResponseDTO response = new BusinessResponseDTO();

        try {
            // 获取信息
            String messageBody = businessMessage.getMessageBody();
            log.info("接收到消息ID：{}", businessMessage.getMessageId());
            log.info("接收到流水线编号：{}", businessMessage.getLineCode());
            log.info("接收到业务类型：{}", businessMessage.getBizType());
            log.info("接收到业务数据：{}", messageBody);

            ZqztJtNoticeRecordDo jtNoticeRecordDo = consumerService.processMessage(messageBody);
            String failReason = consumerService.extractFailReason(jtNoticeRecordDo.getResBody());
            ZqztJtNoticeRecordDo noticeRecordDo = new ZqztJtNoticeRecordDo();
            noticeRecordDo.setId(jtNoticeRecordDo.getId());
            noticeRecordDo.setResBody(jtNoticeRecordDo.getResBody());
            noticeRecordDo.setFailReason(StringUtils.isNotEmpty(failReason) ? failReason : null);
            zqztJtNoticeRecordMapper.updateById(noticeRecordDo);

            log.info("更新成功");
            response.setStatus("success");
            response.setMessage("处理成功");

        } catch (Exception e) {
            log.error("处理消息时发生错误：{}", e.getMessage(),e);
            response.setStatus("failed");
            response.setMessage(e.getMessage());
        }
        return response;
    }
}
