package com.crm.job.service.sync;

import com.crm.system.domain.CustTRosterCustomerProvinceDo;
import com.crm.system.mapper.CustTRosterCustomerProvinceMapper;
import com.crm.system.service.ICustTRosterCustomerProvinceService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 名单制客户表跨数据源同步任务
 * 从slave_1读取数据，同步到master主库
 * @author xyj
 */
@Slf4j
@Component
public class CustTRosterCustomerJob {

    @Autowired
    private ICustTRosterCustomerProvinceService custTRosterCustomerProvinceService;

    @XxlJob("custTRosterCustomerJob")
    public void custTRostercustomerJob(){
        log.info("名单制客户表 跨数据源同步开始");
        long startTime = System.currentTimeMillis();

        try {
            // 1. 清空master主库表数据
            custTRosterCustomerProvinceService.truncateTable();

            // 2. 从slave_1读取数据
            List<CustTRosterCustomerProvinceDo> sourceData = custTRosterCustomerProvinceService.getCustTCustomerList();

            if (sourceData == null || sourceData.isEmpty()) {
                log.warn("未查询到需要同步的数据");
                return;
            }

            log.info("从slave_1查询到 {} 条数据", sourceData.size());

            // 3. 批量插入到master主库（1000条一批）
            insertBatchWithLimit(sourceData, 1000);

            long endTime = System.currentTimeMillis();
            log.info("名单制客户表 跨数据源同步完成，共同步 {} 条数据，耗时: {} ms",
                    sourceData.size(), (endTime - startTime));

        } catch (Exception e) {
            log.error("名单制客户表 跨数据源同步异常", e);
            throw new RuntimeException("名单制客户表跨数据源同步失败", e);
        }
    }

    /**
     * 分批插入数据到master主库
     *
     * @param sourceData 待插入的数据列表
     * @param batchSize 每批插入的数量
     */
    private void insertBatchWithLimit(List<CustTRosterCustomerProvinceDo> sourceData, int batchSize) {
        int totalCount = sourceData.size();
        int processedCount = 0;

        log.info("开始分批插入数据到master主库，总数据量: {}, 批次大小: {}", totalCount, batchSize);

        for (int i = 0; i < totalCount; i += batchSize) {
            int endIndex = Math.min(i + batchSize, totalCount);
            List<CustTRosterCustomerProvinceDo> batchList = sourceData.subList(i, endIndex);

            custTRosterCustomerProvinceService.insertBatch(batchList);
            processedCount += batchList.size();

            log.info("已插入 {}/{} 条数据到master主库", processedCount, totalCount);
        }

        log.info("分批插入完成，共同步 {} 条数据到master主库", processedCount);
    }
}
