package com.crm.job.service.cust;

import com.crm.common.enums.RedisKeyConstants;
import com.crm.common.utils.BeanCopyUtils;
import com.crm.system.domain.CustRosterCustomerNatureMemberProvinceDo;
import com.crm.system.domain.CustTRosterCustomerNatureMemberProvinceDo;
import com.crm.system.service.cust.CustTRosterCustomerNatureMemberProvinceService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
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
            List<CustRosterCustomerNatureMemberProvinceDo> dataList = customerNatureService.getCustCustomerNatureList();
            if (dataList == null || dataList.isEmpty()) {
                log.warn("【名单制自然客户关系同步】未查询到待同步数据");
                return;
            }
            log.info("【名单制自然客户关系同步】待同步数据总条数：{}", dataList.size());

            // 3. 分批插入（正确写法）
            List<CustTRosterCustomerNatureMemberProvinceDo> batchList = new ArrayList<>(BATCH_SIZE);

            for (CustRosterCustomerNatureMemberProvinceDo source : dataList) {
                // 对象拷贝
                CustTRosterCustomerNatureMemberProvinceDo target = BeanCopyUtils.copy(source, CustTRosterCustomerNatureMemberProvinceDo.class);
                batchList.add(target);

                // 达到批次大小 → 批量插入
                if (batchList.size() >= BATCH_SIZE) {
                    customerNatureService.insertBatch(batchList);
                    log.info("【名单制自然客户关系同步】已插入 {} 条", batchList.size());
                    batchList.clear();
                }
            }

            // 4. 插入最后一批不足100的数据
            if (!batchList.isEmpty()) {
                customerNatureService.insertBatch(batchList);
                log.info("【名单制自然客户关系同步】插入最后一批 {} 条", batchList.size());
            }

            long cost = System.currentTimeMillis() - startTime;
            log.info("【名单制自然客户关系同步】任务执行完成，总耗时：{} ms", cost);

        } catch (Exception e) {
            log.error("【名单制自然客户关系同步】任务执行异常", e);
            throw new RuntimeException("同步任务异常", e);
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("【名单制自然客户关系同步】分布式锁已释放");
            }
        }
    }
}
