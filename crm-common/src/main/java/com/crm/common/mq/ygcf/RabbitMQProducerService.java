package com.crm.common.mq.ygcf;

import com.crm.common.mq.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * RabbitMQ 生产者服务
 */
@Slf4j
@Service
public class RabbitMQProducerService{

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送普通消息到指定交换机和路由键
     */
    public void sendMessage(String exchange, String routingKey, String message) {
        String messageId = UUID.randomUUID().toString();
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setMessageId(messageId);
        messageProperties.setTimestamp(new Date());
        CorrelationData correlationData = new CorrelationData(messageId);
        rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
            msg.getMessageProperties().setMessageId(messageId);
            msg.getMessageProperties().setTimestamp(new Date());
            return msg;
        }, correlationData);
        log.info("生产者发送普通消息：{}", message);
        log.info("消息ID：{}", messageId);
        log.info("交换机：{}", exchange);
        log.info("路由键：{}", routingKey);
    }

    public void sendMessage(String message) {
        sendMessage(RabbitMQConfig.YgcfConfig.RPA_YGCF_EXCHANGE, RabbitMQConfig.YgcfConfig.RPA_YGCF_ROUTING_KEY, message);
    }
}
