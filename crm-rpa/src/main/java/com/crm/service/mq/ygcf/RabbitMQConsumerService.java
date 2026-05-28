package com.crm.service.mq.ygcf;

import cn.hutool.json.JSONUtil;
import com.crm.common.mq.config.RabbitMQConfig;
import com.crm.common.utils.StringUtils;
import com.crm.service.mq.ygcf.strategy.MessageStrategyFactory;
import com.crm.service.mq.ygcf.strategy.RpaMessageProcessStrategy;
import com.crm.system.domain.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.mapper.servicehall.ZqztJtNoticeRecordMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * RabbitMQ 消费者服务
 *
 * @author crm
 */
@Slf4j
@Service
public class RabbitMQConsumerService {
    private static final int MAX_RETRY_COUNT = Integer.MAX_VALUE;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private ZqztJtNoticeRecordMapper zqztJtNoticeRecordMapper;

    @Resource
    private MessageStrategyFactory messageStrategyFactory;

    /**
     * 消费测试队列的消息（使用手动确认）
     *
     * @param message 消息内容
     * @param channel 通道
     */
//    @RabbitListener(queues = RabbitMQConfig.YgcfConfig.RPA_YGCF_QUEUE,
//                   ackMode = "MANUAL")
    public void receiveTestMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            // 获取消息ID
            String messageId = getMessageId(message);

            // 转换消息体
            String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
            log.info("消费者接收到测试队列消息：{}", messageBody);
            log.info("消息ID：{}", messageId);
            log.info("消息路由键：{}", message.getMessageProperties().getReceivedRoutingKey());
            // 模拟业务逻辑处理
            ZqztJtNoticeRecordDo jtNoticeRecordDo = processMessage(messageBody);
            String failReason = this.extractFailReason(jtNoticeRecordDo.getResBody());
            ZqztJtNoticeRecordDo noticeRecordDo = new ZqztJtNoticeRecordDo();
            noticeRecordDo.setId(jtNoticeRecordDo.getId());
            noticeRecordDo.setResBody(jtNoticeRecordDo.getResBody());
            noticeRecordDo.setFailReason(StringUtils.isNotEmpty(failReason)?failReason:null);
            zqztJtNoticeRecordMapper.updateById(noticeRecordDo);
            // 处理成功后手动确认消息
            channel.basicAck(deliveryTag, false);
            log.info("消息处理成功，手动确认");
        } catch (Exception e) {
            log.error("处理消息时发生错误：{}", e.getMessage(), e);
            // 获取重试次数
            Integer retryCount = (Integer) message.getMessageProperties().getHeaders().get("x-retry-count");
            if (retryCount == null) {
                retryCount = 0;
            }

            if (retryCount < MAX_RETRY_COUNT) {
                // 增加重试次数并重新发送到重试队列
                retryMessage(message, channel, deliveryTag, retryCount + 1);
            } else {
                // 达到最大重试次数，直接进入死信队列
                channel.basicReject(deliveryTag, false);
                log.warn("消息处理失败，已达到最大重试次数，移至死信队列");
            }
        }
    }


    public String extractFailReason(String responseBody) {
        try {
            // 解析完整的响应JSON
            Map<String, Object> responseMap = JSONUtil.toBean(responseBody, Map.class);

            // 获取 UNI_BSS_BODY
            Map<String, Object> uniBssBody = (Map<String, Object>) responseMap.get("UNI_BSS_BODY");

            // 获取 WORKSHEETS_COMPLETE_RSP
            Map<String, Object> worksheetsCompleteRsp = (Map<String, Object>) uniBssBody.get("WORKSHEETS_COMPLETE_RSP");

            // 获取 data
            Map<String, Object> data = (Map<String, Object>) worksheetsCompleteRsp.get("data");

            // 获取 fworksheetIds 列表
            List<Map<String, Object>> fworksheetIds = (List<Map<String, Object>>) data.get("fworksheetIds");

            // 遍历 fworksheetIds 查找 failReason 不为空的项
            for (Map<String, Object> fworksheet : fworksheetIds) {
                String failReason = (String) fworksheet.get("failReason");
                if (failReason != null && !failReason.isEmpty()) {
                    return failReason; // 返回第一个非空的 failReason
                }
            }

            return null; // 如果没有找到非空的 failReason

        } catch (Exception e) {
            log.error("解析响应体失败", e);
            return null;
        }
    }

    /**
     * 重试消息
     *
     * @param message 原始消息
     * @param channel 通道
     * @param deliveryTag 交付标签
     * @param retryCount 重试次数
     * @throws IOException IO异常
     */
    private void retryMessage(Message message, Channel channel, long deliveryTag, int retryCount) throws IOException {
        long delay = 20000L;
        // 复制消息头并添加重试次数
        Map<String, Object> headers = new java.util.HashMap<>(message.getMessageProperties().getHeaders());
        headers.put("x-retry-count", retryCount);

        // 发送消息到重试队列，带延迟
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.YgcfConfig.RPA_YGCF_RETRY_EXCHANGE,
            RabbitMQConfig.YgcfConfig.RPA_YGCF_RETRY_ROUTING_KEY,
            new String(message.getBody(), StandardCharsets.UTF_8),
            msg -> {
                msg.getMessageProperties().getHeaders().putAll(headers);
                // 设置消息过期时间（TTL），实现延迟
                msg.getMessageProperties().setExpiration(String.valueOf(delay));
                return msg;
            }
        );

        // 确认原消息，从原队列中移除
        channel.basicAck(deliveryTag, false);
        log.info("消息已发送到重试队列，当前重试次数：{}", retryCount);
    }

    /**
     * 消费死信队列的消息（手动确认）
     *
     * @param message 消息内容
     * @param channel 通道
     */
    @RabbitListener(queues = RabbitMQConfig.CommonConfig.RPA_DLQ)
    public void receiveDLQMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);

        log.error("死信队列接收到消息：{}", messageBody);
        log.error("死信原因：{}", message.getMessageProperties().getReceivedRoutingKey());
        log.error("Delivery Tag：{}", deliveryTag);

        try {
            // 可以在这里进行死信消息的处理，比如记录日志、发送告警等
            // 实际应用中可以考虑将死信消息存入数据库，以便后续人工处理
            log.error("死信消息已记录，等待人工处理");

            // 手动确认消息，从死信队列中移除
            channel.basicAck(deliveryTag, false);
            log.info("死信消息手动确认成功");
        } catch (Exception e) {
            log.error("处理死信消息时发生错误：{}", e.getMessage(), e);

            // 死信消息处理失败，拒绝并从队列中移除
            channel.basicReject(deliveryTag, false);
            log.error("死信消息处理失败，已从队列移除");
        }
    }

    /**
     * 处理消息 - 委托给策略工厂路由到对应策略
     *
     * @param messageBody 消息内容
     * @return 处理结果
     */
    public ZqztJtNoticeRecordDo processMessage(String messageBody) throws Exception {
        ZqztJtNoticeRecordDo recordDo = JSONUtil.toBean(messageBody, ZqztJtNoticeRecordDo.class);
        String bizCode = recordDo.getBizCode();

        RpaMessageProcessStrategy strategy = messageStrategyFactory.resolve(bizCode);
        log.info("[RPA消息] bizCode={} 使用策略: {}", bizCode, strategy.getClass().getSimpleName());
        return strategy.process(recordDo);
    }

    /**
     * 从消息中获取消息ID，如果没有则生成一个
     *
     * @param message 消息
     * @return 消息ID
     */
    private String getMessageId(Message message) {
        MessageProperties properties = message.getMessageProperties();
        String messageId = properties.getMessageId();
        if (StringUtils.isBlank(messageId)) {
            messageId = UUID.randomUUID().toString();
            properties.setMessageId(messageId);
        }

        return messageId;
    }

    /**
     * 数据校验（保留原有逻辑）
     */
    private Boolean checkData(ZqztJtNoticeRecordDo recordDo) {
        return true;
    }
}
