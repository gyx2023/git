package com.crm.job.service.cust;

import com.crm.system.domain.dao.CustNecRelationDo;
import com.crm.system.domain.dao.CustTNecRelationDo;
import com.crm.system.service.cust.CustTNecRelationService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 实体客户id与自然客户id对应关系同步定时任务
 *
 * @author crm
 */
@Slf4j
@Component
public class CustTNecRelationSyncJob {

    @Resource
    private CustTNecRelationService custTNecRelationService;

    /**
     * 每批次插入条数
     */
    private static final int BATCH_SIZE = 1000;

    @XxlJob("custTNecRelationDownloadJob")
    public void execute() {
        log.info("【实体客户id与自然客户id对应关系同步】任务开始");
        XxlJobHelper.log("【实体客户id与自然客户id对应关系同步】任务开始");
        long startTime = System.currentTimeMillis();

        try {
            // 1.清空同步表数据
            custTNecRelationService.truncateTable();
            log.info("【实体客户id与自然客户id对应关系同步】清空历史数据完成");
            XxlJobHelper.log("【实体客户id与自然客户id对应关系同步】清空历史数据完成");

            // 2.查询源数据
            List<CustNecRelationDo> dataList = custTNecRelationService.getCustTNecRelationList();
            if (dataList == null || dataList.isEmpty()) {
                log.warn("【实体客户id与自然客户id对应关系同步】未查询到待同步数据");
                XxlJobHelper.log("【名单制自然客户关系同步】未查询到待同步数据");
                return;
            }
            log.info("【实体客户id与自然客户id对应关系同步】待同步数据总条数：{}", dataList.size());
            XxlJobHelper.log("【实体客户id与自然客户id对应关系同步】待同步数据总条数：{}", dataList.size());

            int batchNum = 0;// 批次数
            int zTotalNum = 0;// 总条数
            int zSuccessNum = 0;// 总成功条数
            int zFailureNum = 0;// 总失败条数

            // 3. 分批插入（正确写法）
            List<CustTNecRelationDo> batchList = new ArrayList<>(BATCH_SIZE);
            for (CustNecRelationDo source : dataList) {
                // 对象拷贝
                CustTNecRelationDo target = new CustTNecRelationDo();
                target.setNatureCustId(source.getNatureCustId());
                target.setProvinceCustId(source.getProvinceCustId());
                batchList.add(target);

                // 达到批次大小 → 批量插入
                if (batchList.size() >= BATCH_SIZE) {
                    batchNum++;
                    zTotalNum += batchList.size();
                    try {
                        custTNecRelationService.insertBatch(batchList);
                        zSuccessNum += batchList.size();
                        log.info("插入第 {} 批次，已插入 {} 条，已成功插入 {} 条", batchNum, batchList.size(), zSuccessNum);
                        XxlJobHelper.log("插入第 {} 批次，已插入 {} 条，已成功插入 {} 条", batchNum, batchList.size(), zSuccessNum);
                    } catch (Exception e) {
                        zFailureNum += batchList.size();
                        log.error("插入第 {} 批次异常，已失败插入条数{}，任务执行异常：", batchNum, zFailureNum, e);
                        XxlJobHelper.log("插入第 {} 批次异常，已失败插入条数{}，任务执行异常：{}", batchNum, zFailureNum, e.getMessage());
                    } finally {
                        batchList.clear();
                    }
                }
            }

            // 4. 插入最后一批不足1000的数据
            if (!batchList.isEmpty()) {
                batchNum++;
                zTotalNum += batchList.size();
                try {
                    custTNecRelationService.insertBatch(batchList);
                    zSuccessNum += batchList.size();
                    log.info("插入最后一批，已成功插入 {} 条", batchList.size());
                    XxlJobHelper.log("插入最后一批，已成功插入 {} 条", batchList.size());
                } catch (Exception e) {
                    zFailureNum += batchList.size();
                    log.error("插入最后一批异常", e);
                    XxlJobHelper.log("插入最后一批异常：{}", e.getMessage());
                } finally {
                    batchList.clear();
                }
            }

            long cost = System.currentTimeMillis() - startTime;
            log.info("【实体客户id与自然客户id对应关系同步】任务结束：耗时：{}，总批次：{}，总条数：{}，总成功数：{}，总失败数：{}",
                cost, batchNum, zTotalNum, zSuccessNum, zFailureNum);
            XxlJobHelper.log("【实体客户id与自然客户id对应关系同步】任务结束：耗时：{}，总批次：{}，总条数：{}，总成功数：{}，总失败数：{}",
                cost, batchNum, zTotalNum, zSuccessNum, zFailureNum);

            String successMsg = String.format("任务结束：耗时：%d，总批次：%d，总条数：%d，总成功数：%d，总失败数：%d",
                cost, batchNum, zTotalNum, zSuccessNum, zFailureNum);
            XxlJobHelper.handleSuccess(successMsg);
        } catch (Exception e) {
            log.error("【实体客户id与自然客户id对应关系同步】任务执行异常", e);
            XxlJobHelper.log("【实体客户id与自然客户id对应关系同步】任务执行异常: {}", e.getMessage());

            String errorMsg = String.format("任务执行异常:" + e .getMessage());
            XxlJobHelper.handleFail(errorMsg);
        }
    }

}
