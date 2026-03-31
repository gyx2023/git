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
        String params = XxlJobHelper.getJobParam();

        try {
            custMaintInfoService.streamingQuery(params, batchData -> {
                // 获取当前批次的 user_id 列表
                List<String> userIds = batchData.stream()
                    .map(CustMaintInfo::getUserId)
                    .collect(Collectors.toList());

                // 先删除表中已存在的 user_id 数据
                if (!userIds.isEmpty()) {
                    zqztCustMaintInfoService.deleteByUserIds(userIds);
                }

                // 直接插入所有新数据
                if (!batchData.isEmpty()) {
                    zqztCustMaintInfoService.batchInsert(batchData);
                }
            });

            XxlJobHelper.log("客户维护信息数据同步任务执行完成");
            log.info("客户维护信息数据同步任务执行完成");
        } catch (Exception e) {
            XxlJobHelper.log("客户维护信息数据同步任务执行异常: {}", e.getMessage());
            log.error("客户维护信息数据同步任务执行异常", e);
            throw new RuntimeException("数据同步失败", e);
        }

    }
}
