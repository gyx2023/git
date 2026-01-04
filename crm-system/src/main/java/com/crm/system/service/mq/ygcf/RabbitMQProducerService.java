package com.crm.system.service.mq.ygcf;

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
 *
 * @author crm
 */
@Slf4j
@Service
public class RabbitMQProducerService{

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送普通消息到指定交换机和路由键
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息内容
     */
    public void sendMessage(String exchange, String routingKey, String message) {
        // 生成唯一消息ID
        String messageId = UUID.randomUUID().toString();

        // 设置消息属性
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setMessageId(messageId);
        messageProperties.setTimestamp(new Date());
        // 创建CorrelationData
        CorrelationData correlationData = new CorrelationData(messageId);

        // 使用MessagePostProcessor设置消息属性
        rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
            msg.getMessageProperties().setMessageId(messageId);
            msg.getMessageProperties().setTimestamp(new Date());
            msg.getMessageProperties().setContentType("text/plain");
            msg.getMessageProperties().setContentEncoding(StandardCharsets.UTF_8.name());
            return msg;
        }, correlationData);

        log.info("生产者发送普通消息：{}", message);
        log.info("消息ID：{}", messageId);
        log.info("交换机：{}", exchange);
        log.info("路由键：{}", routingKey);
    }
    /**
     * 发送测试队列消息
     *
     * @param message 消息内容
     */
    public void sendMessage(String message) {
        sendMessage(RabbitMQConfig.YgcfConfig.RPA_EXCHANGE, RabbitMQConfig.YgcfConfig.RPA_ROUTING_KEY, message);
    }
}
