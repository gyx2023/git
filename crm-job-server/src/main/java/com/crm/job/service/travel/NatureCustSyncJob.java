package com.crm.job.service.travel;

import com.crm.system.domain.dao.travel.ZqztTravelNatureCust;
import com.crm.system.domain.vo.NatureCustVo;
import com.crm.system.mapper.travel.ZqztTravelNatureCustMapper;
import com.crm.system.mapper.zbzq.NatureCustSyncQueryMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 浙江省自然客户数据同步任务
 * 从源表 cust_t_nature_cust (REG_PROVINCE_CODE='36') 同步到省内缓存表 zqzt_travel_nature_cust
 * <p>
 * 分页抽取 + 批量写入，避免内存溢出
 *
 * @author crm
 * @date 2026-06-17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NatureCustSyncJob {

    private final NatureCustSyncQueryMapper natureCustSyncQueryMapper;
    private final ZqztTravelNatureCustMapper zqztTravelNatureCustMapper;

    private static final int PAGE_SIZE = 2000;
    private static final int PROGRESS_LOG_INTERVAL = 10; // 每10页输出一次进度

    @XxlJob("natureCustSyncJob")
    public void execute() {
        log.info("开始执行浙江省自然客户数据同步任务");
        XxlJobHelper.log("开始执行浙江省自然客户数据同步任务");

        try {
            // 1. 统计源表符合条件的记录总数（slave_1 源库，无事务）
            int totalCount = natureCustSyncQueryMapper.countByCondition();
            log.info("源表符合条件记录总数: {}", totalCount);
            XxlJobHelper.log("源表符合条件(REG_PROVINCE_CODE='36')记录总数: {}", totalCount);

            if (totalCount == 0) {
                XxlJobHelper.log("源表无符合条件数据，同步终止");
                return;
            }

            // 2. 清空本地缓存表
            zqztTravelNatureCustMapper.truncateTable();
            log.info("清空缓存表完成");
            XxlJobHelper.log("清空缓存表完成");

            // 3. 分页抽取源表数据，批量写入目标表
            int totalPages = (totalCount + PAGE_SIZE - 1) / PAGE_SIZE;
            int processed = 0;
            Date now = new Date();

            for (int page = 0; page < totalPages; page++) {
                int offset = page * PAGE_SIZE;
                List<NatureCustVo> pageList = natureCustSyncQueryMapper.selectByPage(offset, PAGE_SIZE);

                if (pageList == null || pageList.isEmpty()) {
                    break;
                }

                // 转换为目标表实体
                List<ZqztTravelNatureCust> batch = new ArrayList<>(pageList.size());
                for (NatureCustVo vo : pageList) {
                    ZqztTravelNatureCust entity = new ZqztTravelNatureCust();
                    entity.setNatureCustId(vo.getNatureCustId());
                    entity.setNatureCustName(vo.getNatureCustName());
                    entity.setCreateTime(now);
                    batch.add(entity);
                }

                // 批量插入
                writeBatch(batch);

                processed += pageList.size();

                // 每 PROGRESS_LOG_INTERVAL 页输出进度
                if ((page + 1) % PROGRESS_LOG_INTERVAL == 0 || page == totalPages - 1) {
                    log.info("同步进度: {}/{} ({}%)", processed, totalCount,
                            totalCount > 0 ? processed * 100 / totalCount : 100);
                    XxlJobHelper.log("同步进度: {}/{} ({}%)", processed, totalCount,
                            totalCount > 0 ? processed * 100 / totalCount : 100);
                }
            }

            XxlJobHelper.log("浙江省自然客户数据同步完成，共同步 {} 条", processed);
            log.info("浙江省自然客户数据同步完成，共同步 {} 条", processed);
        } catch (Exception e) {
            XxlJobHelper.log("浙江省自然客户数据同步异常: {}", e.getMessage());
            log.error("浙江省自然客户数据同步异常", e);
            throw new RuntimeException("浙江省自然客户数据同步失败", e);
        }
    }

    /**
     * 批量写入缓存表（事务保护，每批独立提交）
     */
    @Transactional(rollbackFor = Exception.class)
    public void writeBatch(List<ZqztTravelNatureCust> batch) {
        zqztTravelNatureCustMapper.insertBatch(batch);
    }
}
