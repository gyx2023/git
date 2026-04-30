package com.crm.job.service.cust;

import com.crm.common.enums.RedisKeyConstants;
import com.crm.system.domain.CustTRosterCustomerNatureMemberProvinceDo;
import com.crm.system.service.cust.CustTRosterCustomerNatureMemberProvinceService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 名单制和自然客户关系表 同步定时任务
 */
@Slf4j
@Component
public class CustTRosterNatureRelationJob {

    @Resource
    private CustTRosterCustomerNatureMemberProvinceService customerNatureService;

    @Resource
    private RedissonClient redissonClient;

    /** 每批次插入条数 */
    private static final int BATCH_SIZE = 100;

    @XxlJob("custTRosterNatureRelationJob")
    @Transactional(rollbackFor = Exception.class)
    public void syncOppoUnbound() {
        log.info("【名单制自然客户关系同步】任务开始");
        long startTime = System.currentTimeMillis();
        String redisKey = RedisKeyConstants.CUST_TROSTER_NATURE_RELATION_TASK;
        RLock lock = redissonClient.getLock(redisKey);

        try {
            // 加分布式锁，防止多节点重复执行
            lock.lock(RedisKeyConstants.MAX_LOCK_TIME, TimeUnit.SECONDS);

            // 1. 清空表旧数据
            customerNatureService.deleteBatch();
            log.info("【名单制自然客户关系同步】清空历史数据完成");

            // 2. 查询源数据
            List<Map<String, Object>> dataList = customerNatureService.getCustCustomerNatureList();
            if (dataList == null || dataList.isEmpty()) {
                log.warn("【名单制自然客户关系同步】未查询到待同步数据");
                return;
            }
            log.info("【名单制自然客户关系同步】待同步数据总条数：{}", dataList.size());

            // 3. 分批插入
            List<CustTRosterCustomerNatureMemberProvinceDo> batchList = new ArrayList<>(BATCH_SIZE);
            for (Map<String, Object> itemMap : dataList) {
                CustTRosterCustomerNatureMemberProvinceDo entity = mapToEntity(itemMap);
                batchList.add(entity);

                if (batchList.size() >= BATCH_SIZE) {
                    customerNatureService.insertBatch(batchList);
                    batchList.clear();
                }
            }

            // 插入剩余数据
            if (!batchList.isEmpty()) {
                customerNatureService.insertBatch(batchList);
            }

            long cost = System.currentTimeMillis() - startTime;
            log.info("【名单制自然客户关系同步】任务执行完成，总耗时：{} ms", cost);

        } catch (Exception e) {
            log.error("【名单制自然客户关系同步】任务执行异常", e);
            throw new RuntimeException("同步任务异常，事务已回滚");
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * Map 转实体（防空指针）
     */
    private CustTRosterCustomerNatureMemberProvinceDo mapToEntity(Map<String, Object> map) {
        CustTRosterCustomerNatureMemberProvinceDo entity = new CustTRosterCustomerNatureMemberProvinceDo();

        // 安全取值，防止空指针
        Object natureCustId = map.get("nature_cust_id");
        Object rosterCustomerId = map.get("roster_customer_id");

        entity.setNatureCustId(natureCustId != null ? natureCustId.toString() : null);
        entity.setRosterCustomerId(rosterCustomerId != null ? rosterCustomerId.toString() : null);

        return entity;
    }
}
