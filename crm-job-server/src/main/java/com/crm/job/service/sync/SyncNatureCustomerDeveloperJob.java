package com.crm.job.service.sync;

import com.crm.system.domain.dao.sync.CustNatureCustomerDeveloper;
import com.crm.system.mapper.sync.CustNatureCustomerDeveloperTargetMapper;
import com.crm.system.mapper.zbzq.CustNatureCustomerDeveloperSourceMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 自然客户开发者数据同步任务
 * 从源库 zq_user_36.cust_t_nature_customer_developer（slave_1）
 * 同步到目标库 zj_sj_test.cust_t_nature_customer_developer（master）
 * <p>
 * 过滤条件: state='1' AND province_code='36'
 * 分页抽取 + 批量写入，避免内存溢出
 *
 * @author crm
 * @date 2026-06-18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SyncNatureCustomerDeveloperJob {

    private final CustNatureCustomerDeveloperSourceMapper sourceMapper;
    private final CustNatureCustomerDeveloperTargetMapper targetMapper;

    private static final int PAGE_SIZE = 2000;
    private static final int PROGRESS_LOG_INTERVAL = 10; // 每10页输出一次进度

    @XxlJob("syncNatureCustomerDeveloperJob")
    public void execute() {
        log.info("开始执行自然客户开发者数据同步任务");
        XxlJobHelper.log("开始执行自然客户开发者数据同步任务");

        try {
            // 1. 统计源表符合条件的记录总数（slave_1 源库，无事务）
            int totalCount = sourceMapper.countByCondition();
            log.info("源表符合条件记录总数: {}", totalCount);
            XxlJobHelper.log("源表符合条件(state='1', province_code='36')记录总数: {}", totalCount);

            if (totalCount == 0) {
                XxlJobHelper.log("源表无符合条件数据，同步终止");
                return;
            }

            // 2. 清空目标表
            targetMapper.truncateTable();
            log.info("清空目标表完成");
            XxlJobHelper.log("清空目标表完成");

            // 3. 分页抽取源表数据，批量写入目标表
            int totalPages = (totalCount + PAGE_SIZE - 1) / PAGE_SIZE;
            int processed = 0;

            for (int page = 0; page < totalPages; page++) {
                int offset = page * PAGE_SIZE;
                List<CustNatureCustomerDeveloper> pageList = sourceMapper.selectByPage(offset, PAGE_SIZE);

                if (pageList == null || pageList.isEmpty()) {
                    break;
                }

                // 批量插入，每批独立事务提交
                writeBatch(pageList);

                processed += pageList.size();

                // 每 PROGRESS_LOG_INTERVAL 页输出进度
                if ((page + 1) % PROGRESS_LOG_INTERVAL == 0 || page == totalPages - 1) {
                    log.info("同步进度: {}/{} ({}%)", processed, totalCount,
                            totalCount > 0 ? processed * 100 / totalCount : 100);
                    XxlJobHelper.log("同步进度: {}/{} ({}%)", processed, totalCount,
                            totalCount > 0 ? processed * 100 / totalCount : 100);
                }
            }

            XxlJobHelper.log("自然客户开发者数据同步完成，共同步 {} 条", processed);
            log.info("自然客户开发者数据同步完成，共同步 {} 条", processed);

        } catch (Exception e) {
            XxlJobHelper.log("自然客户开发者数据同步异常: {}", e.getMessage());
            log.error("自然客户开发者数据同步异常", e);
            throw new RuntimeException("自然客户开发者数据同步失败", e);
        }
    }

    /**
     * 批量写入目标表（事务保护，每批独立提交）
     */
    @Transactional(rollbackFor = Exception.class)
    public void writeBatch(List<CustNatureCustomerDeveloper> batch) {
        targetMapper.insertBatch(batch);
    }
}
