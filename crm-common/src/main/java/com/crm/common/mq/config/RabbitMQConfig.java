package com.crm.common.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 配置类
 * 配置交换机、队列和绑定关系
 *
 * @author crm
 */
@Configuration
@ConditionalOnProperty(name = "crm.mq.enabled", havingValue = "true", matchIfMissing = false)
@Deprecated
public class RabbitMQConfig {

    // ======================== 公共组件配置 ========================
    @Configuration
    public static class CommonConfig {
        // 死信队列名称
        public static final String RPA_DLQ = "rpa.dlq";
        public static final String RPA_DLX = "rpa.dlx";

        // 死信队列路由键
        public static final String RPA_DLQ_ROUTING_KEY = "rpa.dlq.routing.key";

        /**
         * 创建死信队列
         */
        @Bean
        public Queue rpaDLQ() {
            Map<String, Object> args = new HashMap<>();
            // 设置死信队列最大长度
            args.put("x-max-length", 50000);
            return new Queue(RPA_DLQ, true); // 持久化死信队列
        }

        /**
         * 创建死信交换机
         */
        @Bean
        public DirectExchange rpaDLX() {
            return new DirectExchange(RPA_DLX, true, false);
        }

        /**
         * 绑定死信队列到死信交换机
         */
        @Bean
        public Binding bindingTestDLQ() {
            return BindingBuilder.bind(rpaDLQ()).to(rpaDLX()).with(RPA_DLQ_ROUTING_KEY);
        }
    }


    // ======================== 阳光厨房 ========================
    @Configuration
    public static class YgcfConfig {
        // 队列名称
        public static final String RPA_YGCF_QUEUE = "rpa.ygcf.queue";
        // 交换机名称
        public static final String RPA_YGCF_EXCHANGE = "rpa.ygcf.exchange";

        // 路由键
        public static final String RPA_YGCF_ROUTING_KEY = "rpa.ygcf.routing.key";

        // 重试队列名称
        public static final String RPA_YGCF_RETRY_QUEUE = "rpa.ygcf.retry.queue";
        public static final String RPA_YGCF_RETRY_EXCHANGE = "rpa.ygcf.retry.exchange";
        public static final String RPA_YGCF_RETRY_ROUTING_KEY = "rpa.ygcf.retry.routing.key";

        /**
         * 创建测试队列
         */
        @Bean
        public Queue rpaYgcfQueue() {
            Map<String, Object> args = new HashMap<>();
            // 设置死信交换机
            args.put("x-dead-letter-exchange", CommonConfig.RPA_DLX);
            // 设置死信路由键
            args.put("x-dead-letter-routing-key", CommonConfig.RPA_DLQ_ROUTING_KEY);
            // 设置最大队列长度，防止消息堆积
            args.put("x-max-length", 10000);
            // 设置消息存活时间
            //args.put("x-message-ttl", 3600000); // 10分钟
            return new Queue(RPA_YGCF_QUEUE, true, false, false, args); // 持久化队列
        }

        /**
         * 创建测试交换机 (Direct Exchange)
         */
        @Bean
        public DirectExchange rpaExchange() {
            return new DirectExchange(RPA_YGCF_EXCHANGE, true, false);
        }


        /**
         * 绑定测试队列到 Direct 交换机
         */
        @Bean
        public Binding bindingTestQueue() {
            return BindingBuilder.bind(rpaYgcfQueue()).to(rpaExchange()).with(RPA_YGCF_ROUTING_KEY);
        }

        /**
         * 创建重试队列
         */
        @Bean
        public Queue rpaRetryQueue() {
            Map<String, Object> args = new HashMap<>();
            // 设置死信交换机，当消息过期后会发送到这个交换机
            args.put("x-dead-letter-exchange", YgcfConfig.RPA_YGCF_EXCHANGE);
            args.put("x-dead-letter-routing-key", YgcfConfig.RPA_YGCF_ROUTING_KEY);
            return new Queue(RPA_YGCF_RETRY_QUEUE, true, false, false, args);
        }

        /**
         * 创建重试交换机
         */
        @Bean
        public DirectExchange rpaRetryExchange() {
            return new DirectExchange(RPA_YGCF_RETRY_EXCHANGE, true, false);
        }

        /**
         * 绑定重试队列到重试交换机
         */
        @Bean
        public Binding bindingRetryQueue() {
            return BindingBuilder.bind(rpaRetryQueue()).to(rpaRetryExchange()).with(RPA_YGCF_RETRY_ROUTING_KEY);
        }
    }

}
