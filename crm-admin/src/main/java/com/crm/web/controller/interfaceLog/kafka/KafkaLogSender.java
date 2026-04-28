package com.crm.web.controller.interfaceLog.kafka;

import com.crm.common.interfaceLog.domain.InterfaceLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Kafka日志发送器
 * 负责初始化Kafka生产者和线程池，提供异步发送日志的方法
 */
@Component
@Slf4j
public class KafkaLogSender {
    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.interface-log}")
    private String kafkaTopic;

    // 生产者基础参数
    @Value("${kafka.producer.retries:1}")
    private int retries;
    @Value("${kafka.producer.acks:-1}")
    private String acks;
    @Value("${kafka.producer.batch-size:16384}")
    private int batchSize;
    @Value("${kafka.producer.linger-ms:0}")
    private int lingerMs;
    @Value("${kafka.producer.buffer-memory:33554432}")
    private int bufferMemory;

    // 安全认证参数
    @Value("${kafka.security.protocol}")
    private String securityProtocol;
    @Value("${kafka.security.sasl.mechanism}")
    private String saslMechanism;
    @Value("${kafka.security.sasl.jaas-config}")
    private String saslJaasConfig;

    private KafkaProducer<String, String> kafkaProducer;
    private ThreadPoolExecutor sendExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 初始化Kafka生产者和线程池
     */
    @PostConstruct
    public void init() {
        try {
            // 1. 初始化Kafka生产者配置
            Properties props = new Properties();

            // 1.1 基础连接配置
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

            // 1.2 生产者性能与可靠性配置 (参考您的示例)
            props.put(ProducerConfig.RETRIES_CONFIG, retries);
            props.put(ProducerConfig.ACKS_CONFIG, acks);
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
            props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
            props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
            // 1. 连接Broker的超时时间（默认10秒，增大到30秒）
            props.put(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 30000);
            // 2. Socket连接建立超时（关键！覆盖默认的10秒，设为20秒）
            props.put("socket.connection.setup.timeout.ms", 20000);
            // 3. Socket连接建立超时的重试次数（默认1次，增加到3次）
            props.put("socket.connection.setup.timeout.max.ms", 60000);
            // 5. 重试间隔（避免频繁重试，设为1秒）
            props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
            // 1.3 SASL/SCRAM 安全认证配置 (核心！)
            props.put("security.protocol", securityProtocol);
            props.put("sasl.mechanism", saslMechanism);
            props.put("sasl.jaas.config", saslJaasConfig);

            // 2. 创建 Kafka 生产者
            kafkaProducer = new KafkaProducer<>(props);

            // 3. 手动创建线程池
            sendExecutor = new ThreadPoolExecutor(
                    5,                           // 核心线程数
                    10,                          // 最大线程数
                    60L, TimeUnit.SECONDS,       // 线程空闲超时时间
                    new ArrayBlockingQueue<>(1000), // 有界队列
                    r -> {
                        Thread t = new Thread(r, "interface-log-sender-" + System.currentTimeMillis());
                        t.setDaemon(true);
                        return t;
                    },
                    new ThreadPoolExecutor.DiscardPolicy()
            );

            log.info("KafkaLogSender 初始化成功. 服务器: {}, Topic: {}, 认证机制: {}",
                    bootstrapServers, kafkaTopic, saslMechanism);

        } catch (Exception e) {
            log.error("KafkaLogSender 初始化失败，接口日志功能将不可用。请检查Kafka配置和网络连接。", e);
            kafkaProducer = null;
            sendExecutor = null;
        }
    }

    /**
     * 异步发送接口日志
     */
    public void sendAsync(InterfaceLog interfaceLog) {
        if (kafkaProducer == null || sendExecutor == null) {
            log.warn("KafkaLogSender 未正确初始化，跳过发送日志");
            return;
        }
        sendExecutor.submit(() -> sendSyncInternal(interfaceLog));
    }

    /**
     * 内部同步发送方法
     */
    private void sendSyncInternal(InterfaceLog interfaceLog) {
        try {
            String logJson = objectMapper.writeValueAsString(interfaceLog);
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    kafkaTopic,
                    interfaceLog.getSystemName(), // 分区键
                    logJson
            );

            kafkaProducer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("发送接口日志到Kafka失败. Topic: {}, Key: {}, Error: {}",
                            kafkaTopic, interfaceLog.getSystemName(), exception.getMessage());
                } else {
                    log.debug("接口日志发送成功. Topic: {}, Partition: {}, Offset: {}",
                            metadata.topic(), metadata.partition(), metadata.offset());
                }
            });

        } catch (Exception e) {
            log.error("序列化或发送接口日志时异常: {}", e.getMessage());
        }
    }

    /**
     * 关闭资源
     */
    @PreDestroy
    public void destroy() {
        if (sendExecutor != null) {
            sendExecutor.shutdown();
            log.info("日志发送线程池已关闭");
        }
        if (kafkaProducer != null) {
            kafkaProducer.close();
            log.info("Kafka生产者已关闭");
        }
    }
}
