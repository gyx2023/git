package com.crm.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.common.PartitionInfo;
import org.junit.jupiter.api.Test;

/**
 * Kafka 消费者测试类
 * 用于查看 Filebeat 发送到 Kafka 的 nginx 日志数据格式
 *
 * 执行方式：右键运行 testConsume() 方法
 */
@Slf4j
public class KafkaConsumerTest {

    // ==================== 配置信息 ====================
    // Kafka 集群地址（根据 CLAUDE.md 中的配置）
    private static final String BOOTSTRAP_SERVERS = "10.189.29.75:32005,10.189.29.80:32020,10.189.29.92:32011";

    // Topic 名称（Filebeat 默认发送到 filebeat 或指定的 topic）
    private static final String TOPIC = "test-interface-log";  // 如果不对，尝试: nginx-logs, logstash, nginx

    // 消费者组 ID
    private static final String GROUP_ID = "test-consumer-group-" + System.currentTimeMillis();

    // SASL 认证配置（如果 Kafka 启用了认证）
    // 参考 kafka 组件方提供的 ACL 认证示例代码配置
    private static final boolean USE_SASL = true;  // 根据实际情况修改为 true
    private static final String SASL_USERNAME = "test-interface-log-user";  // 替换为实际用户名
    private static final String SASL_PASSWORD = "test##ZJlt1116";  // 替换为实际密码

    // 消费消息数量限制（防止无限消费）
    private static final int MAX_MESSAGES = 10;

    // 消费超时时间（毫秒）
    private static final int POLL_TIMEOUT_MS = 1000000;

    /**
     * 消费 Kafka 数据并打印
     */
    @Test
    public void testConsume() {
        log.info("=== Kafka Consumer Test ===");
        log.info("Bootstrap Servers: {}", BOOTSTRAP_SERVERS);
        log.info("Topic: {}", TOPIC);
        log.info("Group ID: {}", GROUP_ID);

        // 创建消费者配置
        Properties props = createConsumerProps();

        // 创建消费者
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            // 订阅 Topic
            consumer.subscribe(Collections.singletonList(TOPIC));
            log.info("已订阅 Topic: {}", TOPIC);
            log.info("等待消费消息...");

            int messageCount = 0;
            long startTime = System.currentTimeMillis();

            // 循环消费消息
            while (messageCount < MAX_MESSAGES) {
                // 拉取消息
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(5000));

                if (records.isEmpty()) {
                    // 检查是否超时
                    if (System.currentTimeMillis() - startTime > POLL_TIMEOUT_MS) {
                        log.info("消费超时，未收到更多消息");
                        break;
                    }
                    continue;
                }

                // 处理消息
                for (ConsumerRecord<String, String> record : records) {
                    messageCount++;
                    log.info("\n========== 消息 #{} ==========", messageCount);
                    log.info("Topic: {}", record.topic());
                    log.info("Partition: {}", record.partition());
                    log.info("Offset: {}", record.offset());
                    log.info("Key: {}", record.key());
                    log.info("Value:\n{}", record.value());
                    log.info("Timestamp: {}", record.timestamp());
                    log.info("============================\n");

                    if (messageCount >= MAX_MESSAGES) {
                        break;
                    }
                }

                // 手动提交偏移量（可选）
                // consumer.commitSync();
            }

            log.info("=== 消费完成，共消费 {} 条消息 ===", messageCount);

        } catch (Exception e) {
            log.error("消费异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 创建消费者配置
     */
    private Properties createConsumerProps() {
        Properties props = new Properties();

        // 基础配置
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // 偏移量配置
        // earliest: 从最早的消息开始消费
        // latest: 从最新的消息开始消费（默认）
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // 自动提交偏移量
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");

        // 心跳和会话配置（与示例代码保持一致）
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");

        // 拉取配置
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");

        // SASL 认证配置（参考 kafka 组件方提供的 ACL 示例代码）
        if (USE_SASL) {
            // 安全协议
            props.put("security.protocol", "SASL_PLAINTEXT");
            // SASL 机制
            props.put("sasl.mechanism", "SCRAM-SHA-256");
            // JAAS 配置（用户名密码方式）
            props.put("sasl.jaas.config",
                "org.apache.kafka.common.security.scram.ScramLoginModule required " +
                "username=\"" + SASL_USERNAME + "\" " +
                "password=\"" + SASL_PASSWORD + "\";");
        }

        return props;
    }

    /**
     * 测试：列出所有可用的 Topic
     */
    @Test
    public void testListTopics() {
        log.info("=== 列出所有 Topic ===");

        Properties props = createConsumerProps();

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            Map<String, List<PartitionInfo>> topics = consumer.listTopics();

            log.info("可用 Topic 列表:");
            for (String topicName : topics.keySet()) {
                List<PartitionInfo> partitions = topics.get(topicName);
                log.info("  - {} (分区数: {})", topicName, partitions.size());
            }

        } catch (Exception e) {
            log.error("获取 Topic 列表失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 测试：查看指定 Topic 的分区信息
     */
    @Test
    public void testTopicInfo() {
        log.info("=== Topic 信息: {} ===", TOPIC);

        Properties props = createConsumerProps();

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            List<PartitionInfo> partitions = consumer.partitionsFor(TOPIC);

            log.info("Topic: {} 的分区信息:", TOPIC);
            for (PartitionInfo partition : partitions) {
                log.info("  Partition: {}, Leader: {}, Replicas: {}, ISR: {}",
                    partition.partition(),
                    partition.leader(),
                    partition.replicas(),
                    partition.inSyncReplicas());
            }

        } catch (Exception e) {
            log.error("获取 Topic 信息失败: {}", e.getMessage(), e);
        }
    }
}
