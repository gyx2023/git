package com.crm.system.service.servicehall.impl;

import cn.hutool.json.JSONUtil;
import com.crm.system.domain.dao.Sequence;
import com.crm.system.domain.dao.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.mapper.SequenceMapper;
import com.crm.system.service.servicehall.INengKaiCallbackService;
import com.crm.system.client.unibss.UniBssApiClient;
import com.crm.system.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 能开回调核心实现（System层）
 * 负责调用能开 worksheetsComplete 接口，供 RPA 和 Job 复用
 */
@Slf4j
@Service
public class NengKaiCallbackServiceImpl implements INengKaiCallbackService {

    @Resource
    private SequenceMapper sequenceMapper;

    @Resource
    private RedissonClient redissonClient;

    @Value("${ability.app_id:}")
    private String APP_ID;

    @Value("${ability.app_secret:}")
    private String APP_SECRET;

    @Value("${ability.url.worksheetsComplete:}")
    private String worksheetsCompleteURL;

    @Override
    public String callWorksheetsComplete(ZqztJtNoticeRecordDo recordDo, String scene) {
        Map<String, Object> data = new HashMap<>();
        String responseBody = "";
        try {
            String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String sequenceId = getCode(scene);
            String nlpt_wm_serial = "0" + "37" + today + sequenceId;

            Map<String, Object> UNI_BSS_HEAD = UniBssApiClient.buildHead(APP_ID, APP_SECRET);

            Map<String, Object> UNI_BSS_BODY = new HashMap<>();
            Map<String, Object> WORKSHEETS_COMPLETE_REQ = new HashMap<>();
            List<Map<String, Object>> worksheetsList = new ArrayList<>();

            Map<String, Object> worksheetItem = new HashMap<>();
            worksheetItem.put("worksheetId", recordDo.getWorksheetId());
            worksheetItem.put("dealUser", recordDo.getDealUserCode());
            worksheetItem.put("dealResultCode", recordDo.getDealResultCode());
            if (!"0".equals(recordDo.getDealResultCode())) {
                worksheetItem.put("failReason", recordDo.getFailReason());
            }

            Map<String, Object> orderUpdate = new HashMap<>();
            Map<String, Object> order = new HashMap<>();
            Map<String, Object> body = new HashMap<>();
            String bodyStr = recordDo.getBody();
            if (bodyStr != null && !bodyStr.isEmpty()) {
                body = JSONUtil.toBean(bodyStr, Map.class);
            }
            order.put("body", body);
            orderUpdate.put("order", order);
            worksheetItem.put("orderUpdate", orderUpdate);

            worksheetsList.add(worksheetItem);
            WORKSHEETS_COMPLETE_REQ.put("worksheets", worksheetsList);
            UNI_BSS_BODY.put("WORKSHEETS_COMPLETE_REQ", WORKSHEETS_COMPLETE_REQ);

            data.put("UNI_BSS_HEAD", UNI_BSS_HEAD);
            data.put("UNI_BSS_BODY", UNI_BSS_BODY);

            String jsonString = JSONUtil.toJsonStr(data);
            log.info("请求报文参数:{}", jsonString);
            log.info("请求地址:{}", worksheetsCompleteURL);

            responseBody = HttpClientUtil.doPost_nlpt(worksheetsCompleteURL, jsonString, "utf-8", nlpt_wm_serial);
            log.info("响应报文:{}", responseBody);
            return responseBody;
        } catch (Exception e) {
            log.error("能开回调失败: noticeRecordId={}, scene={}", recordDo != null ? recordDo.getId() : null, scene, e);
            throw new RuntimeException("能开回调失败");
        }
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
