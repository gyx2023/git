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
     * 根据user_id查询iob_gov2.CUST_MAINT_INFO表中status=1的记录
     *
     * @param userId 用户ID
     * @return 包含scv_num等信息的Map，如果用户不存在或状态不是1则返回null
     */
    @Override
    public Map<String, Object> getCustMaintInfoByUserId(String userId) {
        // 使用QueryWrapper构建查询条件
        QueryWrapper<CustMaintInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("USER_ID", userId).eq("STATUS", "1");

        List<CustMaintInfo> results = custMaintInfoMapper.selectList(queryWrapper);
        if (results != null && !results.isEmpty()) {
            // 取第一条记录
            CustMaintInfo info = results.get(0);
            Map<String, Object> result = new HashMap<>();
            result.put("svcNum", info.getSvcNum());           // 业务号码
            result.put("userId", info.getUserId());           // 用户编号
            result.put("custName", info.getCustName());       // 归属客户名称
            result.put("custGrpName", info.getCustGrpName()); // 收入归集集团名称
            return result;
        }
        return null;
    }

    /**
     * 批量根据user_id查询iob_gov2.CUST_MAINT_INFO表中status=1的记录
     *
     * @param userIds 用户ID列表
     * @return key为userId，value为包含scv_num等信息的Map
     */
    @Override
    public Map<String, Map<String, Object>> batchGetCustMaintInfoByUserIds(List<String> userIds) {
        Map<String, Map<String, Object>> resultMap = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return resultMap;
        }

        // 使用QueryWrapper构建查询条件
        QueryWrapper<CustMaintInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("STATUS", "1").in("USER_ID", userIds);

        List<CustMaintInfo> results = custMaintInfoMapper.selectList(queryWrapper);
        if (results != null && !results.isEmpty()) {
            for (CustMaintInfo info : results) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("svcNum", info.getSvcNum());           // 业务号码
                userInfo.put("userId", info.getUserId());           // 用户编号
                userInfo.put("custName", info.getCustName());       // 归属客户名称
                userInfo.put("custGrpName", info.getCustGrpName()); // 收入归集集团名称
                resultMap.put(info.getUserId(), userInfo);
            }
        }

        return resultMap;
    }

    @Override
    public List<CustMaintInfo> queryByCustGrpIdAndCustId(String custGrpId, String custId) {
        QueryWrapper<CustMaintInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("CUST_GRP_ID", custGrpId)
                    .eq("CUST_ID", custId)
                    .eq("STATUS", "1");
        return custMaintInfoMapper.selectList(queryWrapper);
    }
     /* 批量根据用户ID查询有效的客户维护信息及服务号码
     *
     * @param userIds 用户ID列表
     * @return key为userId，value为服务号码的Map
     */
    @Override
    @DS("slave_2")  // 使用达梦数据库
    public Map<String, String> selectValidSvcNumByUserIds(List<String> userIds) {

        List<Map<String, Object>> results = custMaintInfoMapper.selectValidSvcNumByUserIds(userIds);

        Map<String, String> resultMap = new HashMap<>();
        if (results != null) {
            for (Map<String, Object> result : results) {
                String userId = (String) result.get("key");
                String svcNum = (String) result.get("value");
                if (userId != null && svcNum != null) {
                    resultMap.put(userId, svcNum);
                }
            }
        }
        return resultMap;
    }
    /**
     * 分页查询客户维护信息
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 当前页的客户维护信息列表
     */
    @Override
    public IPage<CustMaintInfo> selectBatch(int page, int size, String params) {
        // 使用QueryWrapper构建查询条件
        QueryWrapper<CustMaintInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("STATUS", "1"); // 只查询状态为1的记录
        // 如果params不为空且为数字，则根据params计算目标日期
        if (params != null && params.matches("\\d+")) {
            int days = Integer.parseInt(params);
            String formattedDate = DateUtils.getBeforeNDays(days);

            // 添加账期时间大于目标日期的条件
            queryWrapper.gt("ETL_TIME", formattedDate);
        }
        queryWrapper.orderByAsc("ETL_TIME");

        log.info("查询页数：{},数量：{}", page , size);
        Page<CustMaintInfo> pageInfo = new Page<>(page, size);
        IPage<CustMaintInfo> resultPage = custMaintInfoMapper.selectPage(pageInfo, queryWrapper);

        // 返回当前页的数据
        return resultPage;
    }
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
