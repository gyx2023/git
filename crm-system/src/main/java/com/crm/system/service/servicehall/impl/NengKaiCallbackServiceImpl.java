package com.crm.system.service.servicehall.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.crm.system.domain.dao.Sequence;
import com.crm.system.domain.dao.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.mapper.SequenceMapper;
import com.crm.system.service.servicehall.INengKaiCallbackService;
import com.crm.system.utils.HaoKaoSignUtil;
import com.crm.system.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
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

            Map<String, Object> UNI_BSS_HEAD = new HashMap<>();
            UNI_BSS_HEAD.put("APP_ID", APP_ID);
            UNI_BSS_HEAD.put("key", APP_SECRET);
            HaoKaoSignUtil.buildAppParams(UNI_BSS_HEAD);

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
            return responseBody;
        }
    }

    private String getCode(String seqName) {
        String lockKey = "sequence_lock:" + seqName;
        long expireTime = 60;

        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(expireTime, expireTime, TimeUnit.SECONDS);
            if (acquired) {
                try {
                    return doGenerateSequence(seqName);
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            } else {
                throw new RuntimeException("获取序列号锁失败");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取序列号过程中线程被中断", e);
        }
    }

    private String doGenerateSequence(String seqName) {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        LambdaQueryWrapper<Sequence> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Sequence::getSeqName, seqName)
                    .eq(Sequence::getSearchDate, currentDate);

        Sequence sequence = sequenceMapper.selectOne(queryWrapper);
        int currentValue;
        if (sequence != null) {
            currentValue = sequence.getCurrentVal().intValue();
        } else {
            currentValue = 0;
        }

        int nextVal = currentValue + 1;
        LambdaUpdateWrapper<Sequence> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Sequence::getSeqName, seqName)
                     .eq(Sequence::getSearchDate, currentDate)
                     .set(Sequence::getCurrentVal, nextVal);
        sequenceMapper.update(null, updateWrapper);

        if (currentValue == 0) {
            updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Sequence::getSeqName, seqName)
                         .eq(Sequence::getSearchDate, currentDate)
                         .set(Sequence::getCurrentVal, nextVal);
            int affected = sequenceMapper.update(null, updateWrapper);
            if (affected == 0) {
                Sequence newSeq = new Sequence();
                newSeq.setSeqName(seqName);
                newSeq.setSearchDate(currentDate);
                newSeq.setCurrentVal(nextVal);
                sequenceMapper.insert(newSeq);
            }
        }

        return String.format("%09d", currentValue);
    }
}
