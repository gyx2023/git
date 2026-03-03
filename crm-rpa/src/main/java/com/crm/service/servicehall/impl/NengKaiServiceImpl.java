package com.crm.service.servicehall.impl;

import cn.hutool.json.JSONUtil;
import com.crm.dto.BusinessResponseDTO;
import com.crm.dto.KafkaMessageRecordEntity;
import com.crm.dto.Response;
import com.crm.service.mq.ygcf.RabbitMQProducerService;
import com.crm.service.servicehall.NengKaiService;
import com.crm.system.domain.Sequence;
import com.crm.system.domain.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.domain.vo.servicehall.NengKaiRequestVo;
import com.crm.system.domain.vo.servicehall.NengKaiResponseVo;
import com.crm.system.mapper.SequenceMapper;
import com.crm.system.mapper.param.SysParamMapper;
import com.crm.system.mapper.servicehall.ZqztJtNoticeRecordMapper;
import com.crm.utils.HaoKaoSignUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.time.Duration;

import static com.crm.utils.HttpClientUtil.doPost_nlpt;

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

    @Value("${ability.url.UnmannedFactory}")
    private String UnmannedFactoryURL;

    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NengKaiResponseVo nengKaiNotice(NengKaiRequestVo requestVo) {
        //入集团通知记录表
        ZqztJtNoticeRecordDo zqztJtNoticeRecordDo = convert(requestVo);
        zqztJtNoticeRecordMapper.insert(zqztJtNoticeRecordDo);
        //发送mq消息
        //rabbitMQProducerService.sendMessage(JSONUtil.toJsonStr(zqztJtNoticeRecordDo));

        log.info("接入无人工厂,接口开始调用");
        String[] values = sysParam.selectValueByType(zqztJtNoticeRecordDo.getBizCode()).split(",");
        log.info("业务类型: bizType = {};流水线: lineCode = {}", values[0], values[1]);
        KafkaMessageRecordEntity kafkaMessage = new KafkaMessageRecordEntity();
        //获取唯一标识
        String messageId = uniquely(values[0], values[1]);
        kafkaMessage.setMessageId(messageId);
        kafkaMessage.setMessageBody(JSONUtil.toJsonStr(zqztJtNoticeRecordDo));
        kafkaMessage.setBizType(values[0]);
        kafkaMessage.setLineCode(values[1]);
        Response response = restTemplate.postForObject(UnmannedFactoryURL, kafkaMessage, Response.class);

        if (response != null) {
            if (response.getCode() == 20000) {
                log.info("接口调用成功");
            } else {
                log.error("接口调用失败: 状态码={}, 错误信息={}",
                    response.getCode(), response.getMessage());
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
    public String worksheetsComplete(ZqztJtNoticeRecordDo zqztJtNoticeRecordDo,String scene) {

        // 请求
        Map<String, Object> data = new HashMap<String, Object>();

        // 相应体
        String responseBody = "";
        try {
            // 获取当前年月日
            String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

            // 获取序号
            String sequenceId = getCode(scene);

            // 租户ID
            String nlpt_wm_tenant = "UC";

            // 触点编码
            String nlpt_wm_touchpoint = "37";

            // 请求流水
            String nlpt_wm_serial = "0" + nlpt_wm_touchpoint + today + sequenceId;

            // 请求头参数
            Map<String, Object> UNI_BSS_HEAD = new HashMap<>();
            UNI_BSS_HEAD.put("APP_ID", APP_ID);
            UNI_BSS_HEAD.put("key", APP_SECRET);
            HaoKaoSignUtil.buildAppParams(UNI_BSS_HEAD);

            // 报文体
            Map<String, Object> UNI_BSS_BODY = new HashMap<String, Object>();
            // 构建请求参数 - 符合指定的JSON格式
            Map<String, Object> WORKSHEETS_COMPLETE_REQ = new HashMap<>();

            // 构建worksheets数组
            List<Map<String, Object>> worksheetsList = new ArrayList<>();

            // 示例数据，您可以根据实际需求填充
            Map<String, Object> worksheetItem = new HashMap<>();
            worksheetItem.put("worksheetId", zqztJtNoticeRecordDo.getWorksheetId());
            worksheetItem.put("dealUser", zqztJtNoticeRecordDo.getDealUserCode());
            worksheetItem.put("dealResultCode", zqztJtNoticeRecordDo.getDealResultCode());
            if(!"0".equals(zqztJtNoticeRecordDo.getDealResultCode())){
                worksheetItem.put("failReason", zqztJtNoticeRecordDo.getFailReason());
            }

            // 构建orderUpdate对象
            Map<String, Object> orderUpdate = new HashMap<>();
            Map<String, Object> order = new HashMap<>();
            Map<String, Object> body = new HashMap<>();
            body = JSONUtil.toBean(zqztJtNoticeRecordDo.getBody(), Map.class);
            order.put("body", body);
            orderUpdate.put("order", order);
            worksheetItem.put("orderUpdate", orderUpdate);

            worksheetsList.add(worksheetItem);

            // 将worksheets列表添加到请求参数中
            WORKSHEETS_COMPLETE_REQ.put("worksheets", worksheetsList);

            UNI_BSS_BODY.put("WORKSHEETS_COMPLETE_REQ", WORKSHEETS_COMPLETE_REQ);

            // 拼接整个报文
            data.put("UNI_BSS_HEAD", UNI_BSS_HEAD);
            data.put("UNI_BSS_BODY", UNI_BSS_BODY);

            String jsonString = JSONUtil.toJsonStr(data);
            log.info("请求报文参数:{}", jsonString);

            String url = worksheetsCompleteURL;
            log.info("请求地址:{}", url);

            responseBody = doPost_nlpt(url, jsonString, "utf-8", nlpt_wm_serial);
            log.info("响应报文:{}", responseBody);
            return responseBody;

        } catch (Exception e) {
            log.error("服务大厅_申请单提交标准化接口", e);
            return responseBody;


        }
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
