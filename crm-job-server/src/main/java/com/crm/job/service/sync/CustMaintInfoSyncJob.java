package com.crm.job.service.sync;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crm.system.domain.CustMaintInfo;
import com.crm.system.service.ICustMaintInfoService;
import com.crm.system.service.cust.IZqztCustMaintInfoService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 客户维护信息数据同步任务
 * @author fpp
 */
@Slf4j
@Component
public class CustMaintInfoSyncJob {

    @Autowired
    private ICustMaintInfoService custMaintInfoService;

    @Autowired
    private IZqztCustMaintInfoService zqztCustMaintInfoService;

    @XxlJob("custMaintInfoSyncJob")
    public void execute() {
        log.info("开始执行客户维护信息数据同步任务");
        XxlJobHelper.log("开始执行客户维护信息数据同步任务");
        String param = XxlJobHelper.getJobParam();

        int pageSize = 1000; // 每批次处理的数据量
        int currentPage = 1; // 当前页码
        boolean hasMoreData = true; // 是否还有更多数据

        try {
            while (hasMoreData) {
                // 分页查询 cust_maint_info 表数据
                IPage<CustMaintInfo> batchDataPage = custMaintInfoService.selectBatch(currentPage, pageSize, param);

                if (batchDataPage == null || batchDataPage.getTotal() == 0) {
                    // 无数据时退出循环
                    hasMoreData = false;
                    break;
                }
                List<CustMaintInfo> batchData = batchDataPage.getRecords();
                // 查询目标表中已存在的 user_id
                Set<String> existingUserIds = zqztCustMaintInfoService.selectExistingUserIds(batchData.stream()
                    .map(CustMaintInfo::getUserId)
                    .collect(Collectors.toList()));

                // 过滤掉已存在的数据
                List<CustMaintInfo> filteredData = batchData.stream()
                    .filter(info -> !existingUserIds.contains(info.getUserId()))
                    .collect(Collectors.toList());

                // 插入过滤后的数据
                if (!filteredData.isEmpty()) {
                    zqztCustMaintInfoService.batchInsert(filteredData);
                }

                // 日志记录
                XxlJobHelper.log("第 {} 页数据同步完成，共处理 {} 条数据", currentPage , batchData.size());
                log.info("第 {} 页数据同步完成，共处理 {} 条数据", currentPage , batchData.size());

                if (batchData.size() < pageSize) {
                    // 数据量不足一页时退出循环
                    hasMoreData = false;
                    break;
                }
                currentPage++; // 下一页
            }

            XxlJobHelper.log("客户维护信息数据同步任务执行完成");
            log.info("客户维护信息数据同步任务执行完成");
        } catch (Exception e) {
            XxlJobHelper.log("客户维护信息数据同步任务执行异常: {}", e.getMessage());
            log.error("客户维护信息数据同步任务执行异常", e);
            throw new RuntimeException("数据同步失败", e);
        }

    }
}
