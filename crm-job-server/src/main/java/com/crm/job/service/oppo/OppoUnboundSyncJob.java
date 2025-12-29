package com.crm.job.service.oppo;

import cn.hutool.core.util.ObjectUtil;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.vo.topp.ToppContractEformVo;
import com.crm.system.domain.vo.topp.ToppContractVo;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import com.crm.system.service.topp.IToppContractEformService;
import com.crm.system.service.topp.IToppContractService;
import com.crm.system.service.topp.IToppOpportunityService;
import com.crm.system.service.zbzq.ISalesupBakTblBidSuboppoService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 未关联商机，表数据更新为终止
 */
@Slf4j
@Component
public class OppoUnboundSyncJob {

    @Resource
    private IToppOpportunityService toppOpportunityService;

    @Resource
    private IToppContractService toppContractService;

    @Resource
    private IToppContractEformService toppContractEformService;

    @Resource
    private ISalesupBakTblBidSuboppoService salesupBakTblBidSuboppoService;


    @XxlJob("oppoUnboundSyncJob")
    public void syncOppoUnbound() {
        log.info("开始同步未关联商机的合同数据");
        String param = XxlJobHelper.getJobParam();
        if (StringUtils.isBlank(param)) {
            // 默认取7天内数据
            param = "7";
        }
        // 计算查询时间范围
        LocalDate targetDate = LocalDate.now().minusDays(Integer.parseInt(param));
        // 构造指定日期的时间范围：00:00:00 到 23:59:59
        LocalDateTime startTime = LocalDateTime.of(targetDate, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(targetDate, LocalTime.MAX);

        log.info("查询时间范围：{} 到 {}", startTime, endTime);

        // 1. 查询未关联商机的商机数据
        List<ToppOpportunityVo> opportunityList = toppOpportunityService.queryOpportunityUnbound(startTime, endTime);
        log.info("查询到未关联商机的商机数量：{}", opportunityList.size());
        if (opportunityList.isEmpty()) {
            log.info("没有需要处理的数据");
            return;
        }

        // 提取商机ID列表
        List<Long> opportunityIds = opportunityList.stream()
            .map(ToppOpportunityVo::getId)
            .collect(Collectors.toList());
        log.info("商机ID列表大小：{}", opportunityIds.size());

        // 2. 查询商机ID列表中已有关联合同的商机，从待处理列表中移除
        List<ToppContractVo> contractList = toppContractService.queryContractList(opportunityIds);
        List<Long> contractRelatedOpportunityIds = contractList.stream()
            .map(ToppContractVo::getProjectId)
            .collect(Collectors.toList());
        log.info("已关联合同的商机数量：{}", contractRelatedOpportunityIds.size());
        opportunityIds.removeAll(contractRelatedOpportunityIds);

        // 3. 查询商机ID列表中已有关联表单的商机，从待处理列表中移除
        List<ToppContractEformVo> contractEformList = toppContractEformService.queryContractList(opportunityIds);
        List<Long> eformRelatedOpportunityIds = contractEformList.stream()
            .map(ToppContractEformVo::getProjectId)
            .collect(Collectors.toList());
        log.info("已关联表单的商机数量：{}", eformRelatedOpportunityIds.size());
        opportunityIds.removeAll(eformRelatedOpportunityIds);

        // 4. 查询商机ID列表中已有关联销售数据的商机，从待处理列表中移除
        List<Long> signedOppoIds = salesupBakTblBidSuboppoService.selectSignedOppoData(opportunityIds);
        log.info("已关联销售数据的商机数量：{}", signedOppoIds.size());
        opportunityIds.removeAll(signedOppoIds);

        log.info("最终需要更新状态的商机数量：{}", opportunityIds.size());

        // 5. 批量更新未关联任何数据的商机状态为终止
        if (ObjectUtil.isNotEmpty(opportunityIds)) {
            log.info("开始更新商机状态，商机ID列表：{}", opportunityIds);
            Boolean flag = toppOpportunityService.upppUnbound(opportunityIds);
            if (flag != null && flag) {
                log.info("成功更新 {} 个商机的状态为终止", opportunityIds.size());
            } else {
                log.error("更新商机状态失败，商机ID列表：{}", opportunityIds);
            }
        } else {
            log.info("没有需要更新状态的商机");
        }

        log.info("同步未关联商机的合同数据完成");
    }
}
