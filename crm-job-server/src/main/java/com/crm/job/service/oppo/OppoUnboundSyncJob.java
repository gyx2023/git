package com.crm.job.service.oppo;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.SysUsersBo;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.vo.SysUsersVo;
import com.crm.system.domain.vo.topp.ToppContractEformVo;
import com.crm.system.domain.vo.topp.ToppContractVo;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import com.crm.system.service.ISequenceService;
import com.crm.system.service.ISysDeptService;
import com.crm.system.service.ISysUsersService;
import com.crm.system.service.topp.IToppContractEformService;
import com.crm.system.service.topp.IToppContractService;
import com.crm.system.service.topp.IToppOpportunityService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.system.service.unbound.OppoUnBoundSyncService;
import com.crm.system.service.zbzq.ISalesupBakTblBidSuboppoService;
import com.crm.system.utils.Constants;
import com.crm.system.utils.OppClueCodeUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
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

    @Resource
    protected ISequenceService sequenceService;

    @Resource
    private IToppOrderService toppOrderService;

    @Resource
    private ISysUsersService usersService;
    @Resource
    private ISysDeptService deptService;

    @Resource
    private OppoUnBoundSyncService oppoUnBoundSyncService;


    @XxlJob("oppoUnboundSyncJob")
    public void syncOppoUnbound() {
        log.info("开始同步未关联商机的合同数据");
        String param = XxlJobHelper.getJobParam();
        if (StringUtils.isBlank(param)) {
            // 默认取7天内数据
            param = "7";
        }
        try {
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


            List<ToppOpportunityVo> list = opportunityList.stream()
                    .filter(opportunity -> opportunityIds.contains(opportunity.getId()))
                    .collect(Collectors.toList());

            List<String> oppCodes = list.stream()
                    .map(ToppOpportunityVo::getOppCode)
                    .collect(Collectors.toList());

            // 4. 查询商机ID列表中已有关联销售数据的商机，从待处理列表中移除
            List<String> signedOppoCodes = salesupBakTblBidSuboppoService.selectSignedOppoData(oppCodes);
            log.info("已关联销售数据的商机数量：{}", signedOppoCodes.size());

            List<ToppOpportunityVo> signedOppoIdList = list.stream()
                    .filter(opportunity -> signedOppoCodes.contains(opportunity.getOppCode()))
                    .collect(Collectors.toList());

            List<Long> lastSignedOppoIds = signedOppoIdList.stream()
                    .map(ToppOpportunityVo::getId)
                    .collect(Collectors.toList());

            opportunityIds.removeAll(lastSignedOppoIds);

            signedOppoIdList = opportunityList.stream()
                    .filter(opportunity -> opportunityIds.contains(opportunity.getId()))
                    .collect(Collectors.toList());

            log.info("最终需要更新状态的商机数量：{}", opportunityIds.size());

            if (ObjectUtil.isEmpty(opportunityIds)) {
                log.info("没有需要更新状态的商机");
                return;
            }
            log.info("开始更新商机状态，商机ID列表：{}", opportunityIds);
            // 5. 批量更新未关联任何数据的商机状态为终止
            signedOppoIdList.forEach(toppOpportunityVo -> {
                log.info("更新商机 {} 的状态为终止", toppOpportunityVo.getId());
                String result = oppoUnBoundSyncService.syncOppoStatus(toppOpportunityVo, "oppo_abandon");
                log.info("同步商机状态结果：{}", result);
                Boolean orderFlag = saveOrder(toppOpportunityVo);
                if (orderFlag) {
                    Boolean flag = toppOpportunityService.upppUnbound(toppOpportunityVo.getId());
                    log.info("更新商机 {} 的状态为终止结果：{}", toppOpportunityVo.getId(), flag);
                }
            });
            log.info("同步未关联商机的合同数据完成");
        }catch (Exception e) {
            log.error("同步未关联商机的合同数据异常", e);
        }

    }

    private Boolean saveOrder(ToppOpportunityVo oppotunity) {
        SysUsersBo usersBo = new SysUsersBo();
        usersBo.setName("systemUser");
        SysUsersVo su = usersService.queryList(usersBo).get(0);
        SysUsersVo nextUser = usersService.queryById(oppotunity.getCustManagerId());
        SysDept dept = deptService.selectDeptById(nextUser.getOrgId());

        // 创建订单
        StringBuffer orderName = new StringBuffer();
        String orderNumber = sequenceService.getCode(OppClueCodeUtil.getOrderCode());
        orderName.append(DateUtil.format(new Date(), "yyyyMMdd")).append("_").append(oppotunity.getOppName()).append("_").append(Constants.ACT_OPP_END_AUDIT_NAME);
        ToppOrderBo order = new ToppOrderBo();
        order.setOrderName(orderName.toString());
        order.setOrderNumber(orderNumber);
        order.setOppoId(oppotunity.getId());
        order.setOrderType(Constants.ACT_OPP_END_AUDIT);
        order.setClueId(oppotunity.getClueId());
        order.setContractId(oppotunity.getContractId());
//        order.setPiId(piId);
        order.setStatus(Constants.ORDER_STATUS_END);
        order.setCustManagerId(su.getId());
        order.setActors(",," + oppotunity.getCustManagerId().toString() + ",,");
        order.setWhiteList("," + su.getId() + ",");
        order.setOrderType(Constants.ACT_OPP_END_AUDIT);
//        order.setPhase(task.getTaskDefinitionKey());
//        order.setPhaseName(task.getName());
        order.setAreaId(Long.parseLong(dept.getAreaCode()));
        order.setOrgId(nextUser.getOrgId());
        order.setCustId(oppotunity.getCustId());
        order.setActorsInfo(nextUser.getNameCh() + ":" + nextUser.getRelationTel());
        order.setCustName(oppotunity.getCustName());
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setCreator(su.getId());
        //结束原因 0:客户经理已处理 1:未处理但已续约 2:用户离网 3:工单超时  对应码表end_reason类型
        order.setEndReason("3");
        return toppOrderService.insertByBo(order);
    }

}
