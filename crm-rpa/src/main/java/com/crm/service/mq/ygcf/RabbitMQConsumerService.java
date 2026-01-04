package com.crm.service.mq.ygcf;

import com.crm.common.mq.config.RabbitMQConfig;
import com.crm.common.utils.StringUtils;
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

    @Resource
    private RabbitTemplate rabbitTemplate;

    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 消费测试队列的消息（使用手动确认）
     *
     * @param message 消息内容
     * @param channel 通道
     */
    @RabbitListener(queues = RabbitMQConfig.YgcfConfig.RPA_QUEUE,
                   ackMode = "MANUAL")
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
            processMessage(messageBody);

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
        long delay = 10000L;
        // 复制消息头并添加重试次数
        Map<String, Object> headers = new java.util.HashMap<>(message.getMessageProperties().getHeaders());
        headers.put("x-retry-count", retryCount);

        // 发送消息到重试队列，带延迟
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.CommonConfig.RPA_RETRY_EXCHANGE,
            RabbitMQConfig.CommonConfig.RPA_RETRY_ROUTING_KEY,
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
     * 模拟处理消息的业务逻辑
     *
     * @param messageBody 消息内容
     * @throws Exception 处理异常
     */
    private void processMessage(String messageBody) throws Exception {
        // 模拟处理时间
        Thread.sleep(100);
        // 模拟业务逻辑错误，用于测试重试和死信机制
        // 故意抛出除以零异常，模拟处理失败
        log.info("消息处理成功：{}", messageBody);
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
}
