package com.crm.job.service.oppo;

import cn.hutool.core.util.ObjectUtil;
import com.crm.common.utils.BeanCopyUtils;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import com.crm.system.service.topp.IToppOpportunityService;
import com.crm.system.service.unbound.OppoUnBoundSyncService;
import com.crm.system.utils.Constants;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CompensateOppoSyncJob {

    @Resource
    private IToppOpportunityService toppOpportunityService;

    @Resource
    private OppoUnBoundSyncService oppoUnBoundSyncService;

    @XxlJob("compensateOppoSyncJob")
    public void syncOppoUnbound() {
        String param = XxlJobHelper.getJobParam();
        List<ToppOpportunityVo> list = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(param)) {
            ToppOpportunityVo toppOpportunityVo = toppOpportunityService.queryById(Long.valueOf(param));
            list.add(toppOpportunityVo);
        } else {
            //  list=toppOpportunityService.
        }
        ToppOpportunityVo newVo = new ToppOpportunityVo();
        for (ToppOpportunityVo toppOpportunityVo : list) {
            BeanCopyUtils.copy(toppOpportunityVo, newVo);
            newVo.setOppoState(Constants.NEW_OPP_PHASE_OPP_END_LINK);
            newVo.setAuditStatus(Constants.OPP_STATUS_STOP);
            newVo.setOppPhase(Constants.OPP_PHASE_OPPO_END);
            newVo.setOppoState(Constants.OPPO_STATUS_END);
            newVo.setNewOppPhase(Constants.NEW_OPP_PHASE_OPP_END_LINK);
            try {
                String result = oppoUnBoundSyncService.dealOppoBoJsonStr(newVo, "终结商机");
                log.info("同步商机状态结果：{}", result);
            } catch (Exception e) {
                log.error("同步未关联商机的合同数据异常", e);
                throw new RuntimeException(e);
            }
        }
        log.info("同步未关联商机的合同数据完成");

    }
}
