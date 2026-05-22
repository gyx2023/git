package com.crm.job.service.cust;

import com.crm.common.enums.RedisKeyConstants;
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
 * 名单制和自然客户关系表 同步定时任务（优化版：分页 + 批量插入 + 无OOM）
 */
@Slf4j
@Component
public class CustTRosterNatureRelationJob {

    @Resource
    private CustTRosterCustomerNatureMemberProvinceService customerNatureService;

    @Resource
    private RedissonClient redissonClient;

    /**
     * / 每批次插入大小（推荐 500~1000，性能最优）
     */
    private static final int BATCH_SIZE = 1000;
    /***
     * 分页查询大小
     */
    private static final int PAGE_SIZE = 1000;

    @XxlJob("custTRosterNatureRelationJob")
    public void syncOppoUnbound() {
        log.info("【名单制自然客户关系同步】任务开始");
        long startTime = System.currentTimeMillis();
        String redisKey = RedisKeyConstants.CUST_TROSTER_NATURE_RELATION_TASK;
        RLock lock = redissonClient.getLock(redisKey);

        try {
            // 加锁：防止多节点重复执行
            boolean lockSuccess = lock.tryLock(RedisKeyConstants.MAX_LOCK_TIME, TimeUnit.SECONDS);
            if (!lockSuccess) {
                log.info("【名单制自然客户关系同步】其他节点正在执行，本次跳过");
                return;
            }

            // 1. 清空旧数据
            customerNatureService.deleteBatch();
            log.info("【名单制自然客户关系同步】清空历史数据完成");

            int pageNum = 1;
            long totalInsert = 0;

            // 2. 分页查询 + 分批插入
            while (true) {
                List<CustRosterCustomerNatureMemberProvinceDo> dataList =
                    customerNatureService.getCustCustomerNatureList(pageNum, PAGE_SIZE);

                if (dataList == null || dataList.isEmpty()) {
                    break;
                }

                // 3. 转换对象
                List<CustTRosterCustomerNatureMemberProvinceDo> batchList = new ArrayList<>(BATCH_SIZE);
                for (CustRosterCustomerNatureMemberProvinceDo source : dataList) {
                    CustTRosterCustomerNatureMemberProvinceDo target = new CustTRosterCustomerNatureMemberProvinceDo();
                    target.setNatureCustId(source.getNatureCustId().toString());
                    target.setRosterCustomerId(source.getRosterCustomerId().toString());
                    batchList.add(target);

                    // 达到批次大小 → 批量插入
                    if (batchList.size() >= BATCH_SIZE) {
                        customerNatureService.insertBatch(batchList);
                        totalInsert += batchList.size();
                        log.info("【名单制自然客户关系同步】已插入累计：{} 条", totalInsert);
                        batchList.clear();
                    }
                }

                // 最后一批不足1000的也要插入
                if (!batchList.isEmpty()) {
                    customerNatureService.insertBatch(batchList);
                    totalInsert += batchList.size();
                    batchList.clear();
                }

                pageNum++;
            }

            long cost = System.currentTimeMillis() - startTime;
            log.info("【名单制自然客户关系同步】任务全部完成！总插入：{} 条，总耗时：{} ms", totalInsert, cost);

        } catch (Exception e) {
            log.error("【名单制自然客户关系同步】任务执行异常", e);
            throw new RuntimeException("同步任务异常", e);
        } finally {
            // 安全释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("【名单制自然客户关系同步】分布式锁已释放");
            }
        }
    }
}
