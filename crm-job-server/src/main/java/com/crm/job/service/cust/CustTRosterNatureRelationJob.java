package com.crm.job.service.cust;

import com.crm.common.constant.RedisKeyConstants;
import com.crm.system.domain.dao.CustRosterCustomerNatureMemberProvinceDo;
import com.crm.system.domain.dao.CustTRosterCustomerNatureMemberProvinceDo;
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

@Slf4j
@Component
public class CustTRosterNatureRelationJob {

    @Resource
    private CustTRosterCustomerNatureMemberProvinceService customerNatureService;

    @Resource
    private RedissonClient redissonClient;

    private static final int BATCH_SIZE = 1000;

    @XxlJob("custTRosterNatureRelationJob")
    public void syncOppoUnbound() {
        log.info("【名单制自然客户关系同步】任务开始");
        long startTime = System.currentTimeMillis();
        String redisKey = RedisKeyConstants.CUST_TROSTER_NATURE_RELATION_TASK;
        RLock lock = redissonClient.getLock(redisKey);

        try {
            boolean lockSuccess = lock.tryLock(RedisKeyConstants.MAX_LOCK_TIME, TimeUnit.SECONDS);
            if (!lockSuccess) {
                log.info("【名单制自然客户关系同步】其他节点正在执行，跳过");
                return;
            }

            // 清空旧数据
            customerNatureService.deleteBatch();
            log.info("【名单制自然客户关系同步】清空完成");

            int pageNum = 1;
            long totalInsert = 0;

            while (true) {
                // ========== 分页查询（正确逻辑：(pageNum-1)*1000） ==========
                List<CustRosterCustomerNatureMemberProvinceDo> dataList =
                    customerNatureService.getCustCustomerNatureList((pageNum - 1) * BATCH_SIZE, BATCH_SIZE);

                if (dataList == null || dataList.isEmpty()) {
                    break;
                }

                // ========== 整页组装完，再统一插入 ==========
                List<CustTRosterCustomerNatureMemberProvinceDo> batchList = new ArrayList<>();
                for (CustRosterCustomerNatureMemberProvinceDo source : dataList) {
                    CustTRosterCustomerNatureMemberProvinceDo target = new CustTRosterCustomerNatureMemberProvinceDo();
                    target.setNatureCustId(source.getNatureCustId().toString());
                    target.setRosterCustomerId(source.getRosterCustomerId().toString());
                    batchList.add(target);
                }

                // ========== 整页插入一次 ==========
                customerNatureService.insertBatch(batchList);
                totalInsert += batchList.size();
                log.info("【同步进度】本页：{} 条，累计：{} 条", batchList.size(), totalInsert);

                pageNum++;
            }

            long cost = System.currentTimeMillis() - startTime;
            log.info("【名单制自然客户关系同步】任务完成！总插入：{} 条，耗时：{} ms", totalInsert, cost);

        } catch (Exception e) {
            log.error("【名单制自然客户关系同步】异常", e);
            throw new RuntimeException("同步任务异常", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
