package com.crm.job.service.sync;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.CustTRosterCustomerProvinceDo;
import com.crm.system.mapper.CustTRosterCustomerProvinceMapper;
import com.crm.system.mapper.CustTRosterCustomerProvinceWriteMapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 名单制客户表跨数据源同步任务
 * 从slave_1读取数据，同步到master主库
 * @author xyj
 */
@Slf4j
@Component
public class CustTRostercustomerJob {
    
    @Autowired
    private CustTRosterCustomerProvinceMapper readMapper;
    
    @Autowired
    private CustTRosterCustomerProvinceWriteMapper writeMapper;

    @XxlJob("custTRostercustomerJob")
    public void custTRostercustomerJob(){
        log.info("名单制客户表 跨数据源同步开始");
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 清空master主库表数据
            truncateMasterTable();
            
            // 2. 从slave_1读取数据
            List<CustTRosterCustomerProvinceDo> sourceData = readFromSlave();
            
            if (sourceData == null || sourceData.isEmpty()) {
                log.warn("未查询到需要同步的数据");
                return;
            }
            
            log.info("从slave_1查询到 {} 条数据", sourceData.size());
            
            // 3. 批量插入到master主库
            insertToMaster(sourceData);
            
            long endTime = System.currentTimeMillis();
            log.info("名单制客户表 跨数据源同步完成，共同步 {} 条数据，耗时: {} ms", 
                    sourceData.size(), (endTime - startTime));
            
        } catch (Exception e) {
            log.error("名单制客户表 跨数据源同步异常", e);
            throw new RuntimeException("名单制客户表跨数据源同步失败", e);
        }
    }
    
    /**
     * 从slave_1读取数据
     */
    @DS("slave_1")
    public List<CustTRosterCustomerProvinceDo> readFromSlave() {
        log.info("开始从slave_1读取数据");
        List<CustTRosterCustomerProvinceDo> data = readMapper.getCustTCustomerList();
        log.info("从slave_1读取到 {} 条数据", data != null ? data.size() : 0);
        return data;
    }
    
    /**
     * 清空master主库表数据
     */
    @DS("master")
    public void truncateMasterTable() {
        log.info("开始清空master主库 cust_t_roster_customer_province 表数据");
        writeMapper.truncateTable();
        log.info("清空master主库表数据完成");
    }
    
    /**
     * 批量插入到master主库
     */
    @DS("master")
    public void insertToMaster(List<CustTRosterCustomerProvinceDo> data) {
        log.info("开始将数据批量插入到master主库");
        
        int batchSize = 1000;
        int totalCount = 0;
        List<CustTRosterCustomerProvinceDo> batchList = new ArrayList<>();
        
        for (CustTRosterCustomerProvinceDo item : data) {
            batchList.add(item);
            totalCount++;
            
            // 每1000条批量插入一次
            if (batchList.size() >= batchSize) {
                writeMapper.insertBatch(batchList);
                log.info("已同步 {}/{} 条数据到master", totalCount, data.size());
                batchList = new ArrayList<>();
            }
        }
        
        // 处理剩余数据
        if (!batchList.isEmpty()) {
            writeMapper.insertBatch(batchList);
            log.info("已同步 {}/{} 条数据到master", totalCount, data.size());
        }
        
        log.info("批量插入到master主库完成，共同步 {} 条数据", totalCount);
    }
}
