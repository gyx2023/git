package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.utils.DateUtils;
import com.crm.system.domain.CustMaintInfo;
import com.crm.system.mapper.CustMaintInfoMapper;
import com.crm.system.service.ICustMaintInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 客户维护信息查询服务实现
 * 用于查询iob_gov2.CUST_MAINT_INFO表
 *
 * @author 肖英杰
 */
@Service
@Slf4j
public class CustMaintInfoServiceImpl implements ICustMaintInfoService {

    @Autowired
    private CustMaintInfoMapper custMaintInfoMapper;

    /**
     * 新增：流式查询所有数据（推荐使用这个）
     * @param params 参数
     * @param consumer 每批数据的消费函数
     */
    @Override
    public void streamingQuery(String params, Consumer<List<CustMaintInfo>> consumer) {
        String lastEtlTime = null;
        String lastUserId = null;
        boolean hasMore = true;
        int batchSize = 1000; // 每批大小
        int totalCount = 0;

        while (hasMore) {
            List<CustMaintInfo> batch;
            if (lastEtlTime == null || lastUserId == null) {
                // 第一页
                batch = custMaintInfoMapper.selectFirstPage(batchSize, params);
            } else {
                // 后续页
                batch = custMaintInfoMapper.selectNextPage(lastEtlTime, lastUserId, batchSize, params);
            }

            if (batch == null || batch.isEmpty()) {
                hasMore = false;
                log.info("数据抽取完成，共抽取 {} 条记录", totalCount);
            } else {
                // 处理这一批数据
                consumer.accept(batch);

                // 获取最后一条记录作为下一批的起点
                CustMaintInfo lastRecord = batch.get(batch.size() - 1);
                // 注意：这里需要处理ETL_TIME为null的情况
                if (lastRecord.getEtlTime() == null) {
                    log.error("ETL_TIME为null，记录：{}", lastRecord.getUserId());
                    // 如果ETL_TIME为null，可以设置一个默认值或特殊处理
                    lastEtlTime = "1970-01-01 00:00:00";
                } else {
                    // 根据实际类型转换
                    if (lastRecord.getEtlTime() instanceof Date) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        lastEtlTime = sdf.format((Date) lastRecord.getEtlTime());
                    } else {
                        lastEtlTime = String.valueOf(lastRecord.getEtlTime());
                    }
                }
                lastUserId = lastRecord.getUserId();

                totalCount += batch.size();
                log.debug("已处理 {} 条记录，最后时间: {}, USER_ID: {}", totalCount, lastEtlTime, lastUserId);
            }
        }
    }
}
