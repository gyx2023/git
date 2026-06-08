package com.crm.job.service.grp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.system.domain.dao.ZqGrpGroupInfo;
import com.crm.system.mapper.ZqGrpGroupInfoMapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 实体客户经理数据同步任务
 * @author xyj
 */
@Slf4j
@Component
public class CustomerManagerSynchronizationJob {

    @Resource
    private ZqGrpGroupInfoMapper zqGrpGroupInfoMapper;

    @XxlJob("customerManagerSynchronizationJob")
    public void execute() {
        log.info("开始执行实体客户经理数据同步任务");

        try {
            // 分批处理参数
            int batchSize = 1000; // 每批处理1000条
            int currentPage = 1;
            int totalProcessed = 0;
            int totalUpdated = 0;

            while (true) {
                // 分页查询数据
                Page<ZqGrpGroupInfo> page = new Page<>(currentPage, batchSize);
                LambdaQueryWrapper<ZqGrpGroupInfo> queryWrapper = new LambdaQueryWrapper<>();
                Page<ZqGrpGroupInfo> resultPage = zqGrpGroupInfoMapper.selectPage(page, queryWrapper);
                List<ZqGrpGroupInfo> groupInfoList = resultPage.getRecords();

                // 如果没有数据，退出循环
                if (groupInfoList == null || groupInfoList.isEmpty()) {
                    break;
                }

                log.info("正在处理第 {} 批数据，本批数量: {}", currentPage, groupInfoList.size());

                // 用于收集需要更新的数据
                List<ZqGrpGroupInfo> needUpdateList = new ArrayList<>();

                // 遍历处理数据
                for (ZqGrpGroupInfo zqGrpGroupInfo : groupInfoList) {
                    String grpCustType = zqGrpGroupInfo.getGrpCustType();

                    if ("400".equals(grpCustType)) {
                        // 处理集团客户类别为400(大客户)的逻辑
                        fillCustomerManagerInfoForBigCustomer(zqGrpGroupInfo);
                    } else {
                        // 处理集团客户类别不为400的逻辑(包括null、空字符串、其他值)
                        fillCustomerManagerInfo(zqGrpGroupInfo);
                    }

                    // 检查是否需要更新(只要填充了客户经理信息就需要更新)
                    if (zqGrpGroupInfo.getCustManagerEmpId() != null ||
                        zqGrpGroupInfo.getCustManagerOauser() != null ||
                        zqGrpGroupInfo.getCustManagerName() != null) {
                        needUpdateList.add(zqGrpGroupInfo);
                    }
                }

                log.info("第 {} 批需要更新的记录数: {}", currentPage, needUpdateList.size());

                // 批量更新当前批次的数据
                if (!needUpdateList.isEmpty()) {
                    boolean result = zqGrpGroupInfoMapper.updateBatchById(needUpdateList);
                    if (result) {
                        totalUpdated += needUpdateList.size();
                        log.info("第 {} 批成功更新记录数: {}", currentPage, needUpdateList.size());
                    } else {
                        log.error("第 {} 批批量更新失败", currentPage);
                    }
                }

                totalProcessed += groupInfoList.size();
                log.info("累计已处理: {} 条，累计已更新: {} 条", totalProcessed, totalUpdated);

                // 判断是否还有下一页
                if (currentPage >= resultPage.getPages()) {
                    break;
                }

                currentPage++;
            }

            log.info("实体客户经理数据同步任务执行完成，总计处理: {} 条，总计更新: {} 条", totalProcessed, totalUpdated);
        } catch (Exception e) {
            log.error("实体客户经理数据同步任务执行异常", e);
            throw new RuntimeException("任务执行失败", e);
        }
    }

    /**
     * 填充实体客户经理信息
     * 优先级：县级 > 市级 > 省级
     * 如果县级客户经理信息不为空，则使用县级；否则使用市级；再否则使用省级
     *
     * @param groupInfo 政企集团客户信息对象
     */
    private void fillCustomerManagerInfo(ZqGrpGroupInfo groupInfo) {
        // 优先使用县级客户经理信息
        String countyManagerEmpId = groupInfo.getCountyManagerEmpId();
        if (countyManagerEmpId != null && !countyManagerEmpId.isEmpty()) {
            groupInfo.setCustManagerEmpId(countyManagerEmpId);
            groupInfo.setCustManagerOauser(groupInfo.getCountyManagerOauser());
            groupInfo.setCustManagerName(groupInfo.getCountyManagerName());
            return;
        }

        // 县级为空，使用市级客户经理信息
        String cityManagerEmpId = groupInfo.getCityManagerEmpId();
        if (cityManagerEmpId != null && !cityManagerEmpId.isEmpty()) {
            groupInfo.setCustManagerEmpId(cityManagerEmpId);
            groupInfo.setCustManagerOauser(groupInfo.getCityManagerOauser());
            groupInfo.setCustManagerName(groupInfo.getCityManagerName());
            return;
        }

        // 市级也为空，使用省级客户经理信息
        String provManagerEmpId = groupInfo.getProvManagerEmpId();
        if (provManagerEmpId != null && !provManagerEmpId.isEmpty()) {
            groupInfo.setCustManagerEmpId(provManagerEmpId);
            groupInfo.setCustManagerOauser(groupInfo.getProvManagerOauser());
            groupInfo.setCustManagerName(groupInfo.getProvManagerName());
        }

        // 省级也为空，保持CUST_MANAGER字段为null（不做任何操作）
    }

    /**
     * 为大客户填充实体客户经理信息
     * 根据集团客户拓展类型(GRP_EXPAND_TYPE)选择对应级别的客户经理：
     * 01-省直拓 -> 使用省级客户经理
     * 02-市直拓 -> 使用市级客户经理
     * 03-县直拓 -> 使用县级客户经理
     * 如果GRP_EXPAND_TYPE为null/空字符串，或对应级别客户经理工号为空，则按县->市->省优先级填充
     *
     * @param groupInfo 政企集团客户信息对象
     */
    private void fillCustomerManagerInfoForBigCustomer(ZqGrpGroupInfo groupInfo) {
        String grpExpandType = groupInfo.getGrpExpandType();

        // 如果拓展类型为null或空字符串，使用通用降级逻辑
        if (grpExpandType == null || grpExpandType.isEmpty()) {
            fillCustomerManagerInfo(groupInfo);
            return;
        }

        if ("01".equals(grpExpandType)) {
            // 省直拓 - 优先使用省级客户经理
            String provManagerEmpId = groupInfo.getProvManagerEmpId();
            if (provManagerEmpId != null && !provManagerEmpId.isEmpty()) {
                groupInfo.setCustManagerEmpId(provManagerEmpId);
                groupInfo.setCustManagerOauser(groupInfo.getProvManagerOauser());
                groupInfo.setCustManagerName(groupInfo.getProvManagerName());
            } else {
                // 省级客户经理为空，使用通用降级逻辑
                fillCustomerManagerInfo(groupInfo);
            }
        } else if ("02".equals(grpExpandType)) {
            // 市直拓 - 优先使用市级客户经理
            String cityManagerEmpId = groupInfo.getCityManagerEmpId();
            if (cityManagerEmpId != null && !cityManagerEmpId.isEmpty()) {
                groupInfo.setCustManagerEmpId(cityManagerEmpId);
                groupInfo.setCustManagerOauser(groupInfo.getCityManagerOauser());
                groupInfo.setCustManagerName(groupInfo.getCityManagerName());
            } else {
                // 市级客户经理为空，使用通用降级逻辑
                fillCustomerManagerInfo(groupInfo);
            }
        } else if ("03".equals(grpExpandType)) {
            // 县直拓 - 优先使用县级客户经理
            String countyManagerEmpId = groupInfo.getCountyManagerEmpId();
            if (countyManagerEmpId != null && !countyManagerEmpId.isEmpty()) {
                groupInfo.setCustManagerEmpId(countyManagerEmpId);
                groupInfo.setCustManagerOauser(groupInfo.getCountyManagerOauser());
                groupInfo.setCustManagerName(groupInfo.getCountyManagerName());
            } else {
                // 县级客户经理为空，使用通用降级逻辑
                fillCustomerManagerInfo(groupInfo);
            }
        }
        // 如果拓展类型不匹配，则保持CUST_MANAGER字段为null
    }
}
