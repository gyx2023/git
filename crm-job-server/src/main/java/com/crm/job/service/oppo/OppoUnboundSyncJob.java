package com.crm.job.service.oppo;

import com.crm.common.utils.StringUtils;
import com.crm.system.domain.vo.topp.ToppContractEformVo;
import com.crm.system.domain.vo.topp.ToppContractVo;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import com.crm.system.domain.vo.zbzq.SalesupCollOppoSignVo;
import com.crm.system.service.topp.IToppContractEformService;
import com.crm.system.service.topp.IToppContractService;
import com.crm.system.service.topp.IToppOpportunityService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Autowired
    private IToppOpportunityService toppOpportunityService;

    @Autowired
    private IToppContractService toppContractService;

    @Autowired
    private IToppContractEformService toppContractEformService;


    @XxlJob("oppoUnboundSyncJob")
    public void sync() {
        log.info("开始同步合同数据");
        String param = XxlJobHelper.getJobParam();
        if (StringUtils.isBlank(param)) {
            //默认取7天内数据
            param = "7";
        }
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(Integer.parseInt(param));
        // 2. 构造7天前当天的时间范围：00:00:00 到 23:59:59
        LocalDateTime startTime = LocalDateTime.of(sevenDaysAgo, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(sevenDaysAgo, LocalTime.MAX);

        List<ToppOpportunityVo> list = toppOpportunityService.queryOpportunityUnbound(startTime, endTime);
        List<Long> ids = list.stream().map(ToppOpportunityVo::getId).collect(Collectors.toList());
        List<ToppContractVo> toppContractVos =toppContractService.queryContractList(ids);
        List<Long> projectIds = toppContractVos.stream().map(ToppContractVo::getProjectId).collect(Collectors.toList());
        ids.removeAll(projectIds);

        List<ToppContractEformVo> toppContractEformVos = toppContractEformService.queryContractList(ids);
        List<Long> formIdList = toppContractEformVos.stream().map(ToppContractEformVo::getProjectId).collect(Collectors.toList());
        ids.removeAll(formIdList);



    }
}
