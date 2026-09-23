package com.crm.system.service.rpa.servicehall.impl;

import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;
import com.crm.system.domain.dao.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.domain.dto.rpa.BusinessMessageDTO;
import com.crm.system.domain.dto.rpa.Response;
import com.crm.system.mapper.param.SysParamMapper;
import com.crm.system.service.jt.IZqztJtNoticeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ConnectTimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import javax.annotation.Resource;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class NengKaiNoticeAsyncService {
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private SysParamMapper sysParam;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private IZqztJtNoticeRecordService noticeRecordService;
    @Value("${ability.url.unmannedFactory:}")
    private String unmannedFactoryUrl;

    @Async("mdcTaskExecutor")
    public void send(ZqztJtNoticeRecordDo record) {
        Integer timeoutStatus = null;
        try {
            String[] values = sysParam.selectValueByType("rpa_biz_code").split(",");
            BusinessMessageDTO message = new BusinessMessageDTO();
            message.setMessageId(uniquely(values[0], values[1]));
            message.setBizType(values[0]);
            message.setLineCode(values[1]);
            JSONObject body = JSONUtil.parseObj(record);
            // 内部传输状态不属于无人工厂的业务报文。
            body.remove("requestTimeoutStatus");
            message.setMessageBody(body.toString());
            timeoutStatus = 0;
            Response response = restTemplate.postForObject(unmannedFactoryUrl, message, Response.class);
            if (response != null && Integer.valueOf(20000).equals(response.getCode())) {
                log.info("无人工厂接口调用成功, recordId={}", record.getId());
            } else {
                log.error("无人工厂接口调用失败, recordId={}, code={}, message={}", record.getId(),
                    response == null ? null : response.getCode(),
                    response == null ? "响应对象为空" : response.getMessage());
            }
        } catch (Exception e) {
            if (timeoutStatus != null && isTimeout(e)) {
                timeoutStatus = 1;
            }
            log.error("无人工厂接口调用异常, recordId={}, timeoutStatus={}", record.getId(), timeoutStatus, e);
        } finally {
            if (timeoutStatus != null) {
                try {
                    if (!noticeRecordService.updateRequestTimeoutStatus(record.getId(), timeoutStatus)) {
                        log.error("通知超时状态回写未命中记录, recordId={}, status={}", record.getId(), timeoutStatus);
                    }
                } catch (Exception e) {
                    log.error("通知超时状态回写失败, recordId={}, status={}", record.getId(), timeoutStatus, e);
                }
            }
        }
    }

    private boolean isTimeout(Throwable error) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        for (Throwable cause = error; cause != null && visited.add(cause); cause = cause.getCause()) {
            // ConnectionPoolTimeoutException 继承 ConnectTimeoutException。
            if (cause instanceof SocketTimeoutException || cause instanceof ConnectTimeoutException) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成唯一标识
     * @param bizType 业务类型
     * @param lineCode 流水线代码
     * @return 唯一标识
     */
    private String uniquely(String bizType, String lineCode) {
        try {
            // 获取当前时间:年月日时分
            String dateTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

            // 构造Redis键名，按业务类型和线路代码分区
            String redisKey = String.format("message:id:%s:%s:%s", bizType, lineCode,
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));

            // 使用Redis的INCR命令原子递增获取序列号
            Long sequence = redisTemplate.opsForValue().increment(redisKey);

            // 设置键的过期时间为明天凌晨，确保第二天自动重置为1
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tomorrow = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            long expireSeconds = Duration.between(now, tomorrow).getSeconds();
            redisTemplate.expire(redisKey, expireSeconds, TimeUnit.SECONDS);

            // 确保序列号为4位数字
            String sequenceStr = String.format("%04d", sequence > 9999 ? sequence % 10000 : sequence);

            // 拼接最终的messageId
            String messageId = dateTimeStr + bizType + lineCode + sequenceStr;

            log.info("生成唯一标识: {}, RedisKey: {}", messageId, redisKey);
            return messageId;

        } catch (Exception e) {
            log.error("Redis生成唯一标识失败，使用备用方案", e);
            // Redis失败时的降级处理
            return fallbackUniquely(bizType, lineCode);
        }
    }

    private String fallbackUniquely(String bizType, String lineCode) {
        String dateTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        // 使用纳秒时间戳+随机数作为备用序列号
        String backupSequence = String.valueOf(System.nanoTime() % 10000);
        String sequenceStr = String.format("%04d", Integer.parseInt(backupSequence));

        return dateTimeStr + bizType + lineCode + sequenceStr;
    }


}
