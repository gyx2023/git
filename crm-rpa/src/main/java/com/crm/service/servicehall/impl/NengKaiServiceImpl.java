package com.crm.service.servicehall.impl;

import cn.hutool.json.JSONUtil;
import com.crm.dto.BusinessMessageDTO;
import com.crm.dto.Response;
import com.crm.service.mq.ygcf.RabbitMQProducerService;
import com.crm.service.servicehall.NengKaiService;
import com.crm.system.domain.dao.Sequence;
import com.crm.system.domain.dao.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.domain.vo.servicehall.NengKaiRequestVo;
import com.crm.system.domain.vo.servicehall.NengKaiResponseVo;
import com.crm.system.mapper.SequenceMapper;
import com.crm.system.mapper.param.SysParamMapper;
import com.crm.system.mapper.servicehall.ZqztJtNoticeRecordMapper;
import com.crm.system.service.jt.IZqztJtNoticeRecordService;
import com.crm.system.service.servicehall.INengKaiCallbackService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class NengKaiServiceImpl implements NengKaiService {

    @Resource
    private ZqztJtNoticeRecordMapper zqztJtNoticeRecordMapper;

    @Resource
    private RabbitMQProducerService rabbitMQProducerService;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private SysParamMapper sysParam;

    @Resource
    private SequenceMapper sequenceMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${ability.app_id}")
    private String APP_ID;

    @Value("${ability.app_secret}")
    private String APP_SECRET;

    @Value("${ability.url.worksheetsComplete}")
    private String worksheetsCompleteURL;

    @Value("${ability.url.unmannedFactory}")
    private String UnmannedFactoryURL;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private IZqztJtNoticeRecordService iZqztJtNoticeRecordService;

    @Resource
    private INengKaiCallbackService nengKaiCallbackCoreService;

    @Override
    public NengKaiResponseVo nengKaiNotice(NengKaiRequestVo requestVo) {
        //入集团通知记录表
        ZqztJtNoticeRecordDo zqztJtNoticeRecordDo = convert(requestVo);
        iZqztJtNoticeRecordService.saveNoticeRecord(zqztJtNoticeRecordDo);
        //发送mq消息
        //rabbitMQProducerService.sendMessage(JSONUtil.toJsonStr(zqztJtNoticeRecordDo));

        log.info("接入无人工厂,接口开始调用");
        String[] values = sysParam.selectValueByType("rpa_biz_code").split(",");
        log.info("业务类型: bizType = {};流水线: lineCode = {}", values[0], values[1]);
        BusinessMessageDTO businessMessageDTO = new BusinessMessageDTO();
        //获取唯一标识
        String messageId = uniquely(values[0], values[1]);
        businessMessageDTO.setMessageId(messageId);
        businessMessageDTO.setBizType(values[0]);
        businessMessageDTO.setLineCode(values[1]);
        businessMessageDTO.setMessageBody(JSONUtil.toJsonStr(zqztJtNoticeRecordDo));
        Response response = restTemplate.postForObject(UnmannedFactoryURL, businessMessageDTO, Response.class);

        if (response != null) {
            if (response.getCode() == 20000) {
                log.info("接口调用成功");
            } else {
                log.error("接口调用失败: 状态码={}, 错误信息={}", response.getCode(), response.getMessage());
            }
        } else {
            log.error("接口调用失败: 响应对象为空");
        }

        return NengKaiResponseVo.success();
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


    @Override
    public String worksheetsComplete(ZqztJtNoticeRecordDo zqztJtNoticeRecordDo, String scene) {
        return nengKaiCallbackCoreService.callWorksheetsComplete(zqztJtNoticeRecordDo, scene);
    }


    /**
     * 将OrderProcessInfoRequestVo的属性复制到OrderProcessInfo
     * @param requestVo 请求参数对象
     * @return 实体对象
     */
    public static ZqztJtNoticeRecordDo convert(NengKaiRequestVo requestVo) {
        if (requestVo == null) {
            return null;
        }

        ZqztJtNoticeRecordDo processInfo = new ZqztJtNoticeRecordDo();

        // 逐一设置属性
        processInfo.setOrderId(requestVo.getOrderId());
        processInfo.setOrderOperatorId(requestVo.getOrderOperatorId());
        processInfo.setOrderOperatorName(requestVo.getOrderOperatorName());
        processInfo.setOrderProvinceCode(requestVo.getOrderProvinceCode());
        processInfo.setOrderCityCode(requestVo.getOrderCityCode());
        processInfo.setOrderStatus(requestVo.getOrderStatus());
        processInfo.setWorksheetId(requestVo.getWorksheetId());
        processInfo.setTaskCode(requestVo.getTaskCode());
        processInfo.setTaskName(requestVo.getTaskName());
        processInfo.setBizCode(requestVo.getBizCode());
        processInfo.setDealUserCode(requestVo.getDealUserCode());
        processInfo.setDealUserName(requestVo.getDealUserName());
        processInfo.setDealResultCode(requestVo.getDealResultCode());
        Map<String, Object> body = requestVo.getBody();
        if (body != null) {
            processInfo.setBody(JSONUtil.toJsonStr(body));
        }
        Map<String, Object> formTemplate = requestVo.getFormTemplate();
        if (formTemplate != null) {
            processInfo.setFormTemplate(JSONUtil.toJsonStr(formTemplate));
        }
        processInfo.setCreateTime(new Date());
        processInfo.setUpdateTime(new Date());

        return processInfo;
    }


    private String getCode(String seqName) {
        String lockKey = "sequence_lock:" + seqName;
        long expireTime = 60; // 锁过期时间60秒

        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 获取分布式锁，设置锁超时时间为60秒
            boolean acquired = lock.tryLock(expireTime, expireTime, TimeUnit.SECONDS);

            if (acquired) {
                // 获取当前日期
                String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

                // 查询sequence表中当天的记录
                // 这里需要根据实际的sequence表结构和mapper进行查询
                // 模拟逻辑：查询数据库中指定序列名和日期的记录
                Integer currentVal = getCurrentValueFromDB(seqName, currentDate);

                if (currentVal != null) {
                    // 如果当天已有记录，返回current_val+1，并更新current_val
                    int nextVal = currentVal + 1;
                    // 更新数据库中的current_val
                    updateCurrentValueInDB(seqName, currentDate, nextVal);
                    return String.format("%09d", currentVal);
                } else {
                    // 如果当天没有记录，说明这个序列当天第一次使用，将search_date更新为当天，current_val更新为1
                    updateSequenceForNewDay(seqName, currentDate, 1);
                    return String.format("%09d", 1);
                }
            } else {
                throw new RuntimeException("获取序列号锁失败");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取序列号过程中线程被中断", e);
        } finally {
            // 释放分布式锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 从数据库获取当前值
    private Integer getCurrentValueFromDB(String seqName, String searchDate) {
        // 使用LambdaQueryWrapper进行精确查询
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Sequence> lambdaQueryWrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Sequence::getSeqName, seqName)
            .eq(Sequence::getSearchDate, searchDate);

        Sequence sequence = sequenceMapper.selectOne(lambdaQueryWrapper);

        return sequence != null ? sequence.getCurrentVal().intValue() : null;
    }

    // 更新数据库中的当前值
    private void updateCurrentValueInDB(String seqName, String searchDate, int newValue) {
        // 使用LambdaUpdateWrapper更新sequence表中的current_val字段
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Sequence> lambdaUpdateWrapper =
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Sequence::getSeqName, seqName)
            .eq(Sequence::getSearchDate, searchDate)
            .set(Sequence::getCurrentVal, Long.valueOf(newValue));

        sequenceMapper.update(null, lambdaUpdateWrapper);
    }

    // 更新序列记录，将日期更新为当天，current_val更新为1
    private void updateSequenceForNewDay(String seqName, String searchDate, int initValue) {
        // 使用LambdaUpdateWrapper更新sequence表中的search_date和current_val字段
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Sequence> lambdaUpdateWrapper =
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Sequence::getSeqName, seqName)
            .set(Sequence::getSearchDate, searchDate)
            .set(Sequence::getCurrentVal, Long.valueOf(initValue));

        sequenceMapper.update(null, lambdaUpdateWrapper);
    }


}
