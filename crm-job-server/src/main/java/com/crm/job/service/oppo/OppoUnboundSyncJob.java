package com.crm.job.service.oppo;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.enums.OrderStatusEnum;
import com.crm.common.enums.SwitchStatus;
import com.crm.common.enums.order.ProcessEngineEnum;
import com.crm.common.utils.BeanCopyUtils;
import com.crm.system.domain.bo.SysUsersBo;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.vo.SysUsersVo;
import com.crm.system.domain.vo.topp.ToppContractEformVo;
import com.crm.system.domain.vo.topp.ToppContractVo;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import com.crm.system.service.ISequenceService;
import com.crm.system.service.sys.ISysDeptService;
import com.crm.system.service.sys.ISysParamService;
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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    @Resource
    private ISysParamService sysParamService;


    @XxlJob("oppoUnboundSyncJob")
    public void syncOppoUnbound() {
        log.info("开始同步未关联商机的合同数据");
        String startTime = "";
        String endTime = "";

        String bfStartTime = "";
        String bfEndTime = "";

        String param = XxlJobHelper.getJobParam();
        if (ObjectUtil.isEmpty(param)) {
            // 默认取7天内数据
            LocalDate targetDate = LocalDate.now().minusDays(7);
            String startTimeStr = DateUtil.format(targetDate.atStartOfDay(), "yyyy-MM-dd");
            startTime = startTimeStr + " 00:00:00";
            endTime = startTimeStr + " 23:59:59";

            LocalDate bfTargetDate = LocalDate.now().minusMonths(3).minusDays(7);
            String bfStartTimeStr = DateUtil.format(bfTargetDate.atStartOfDay(), "yyyy-MM-dd");
            bfStartTime = bfStartTimeStr + " 00:00:00";
            bfEndTime = bfStartTimeStr + " 23:59:59";

        } else if (ObjectUtil.contains(param, "|")) {
            startTime = param.split("\\|")[0] + " 00:00:00";
            endTime = param.split("\\|")[1] + " 23:59:59";

            bfStartTime = param.split("\\|")[0] + " 00:00:00";
            bfEndTime = param.split("\\|")[1] + " 23:59:59";

        } else {
            startTime = param + " 00:00:00";
            endTime = param + " 23:59:59";
            bfStartTime = param + " 00:00:00";
            bfEndTime = param + " 23:59:59";
        }
        // 构造指定日期的时间范围：00:00:00 到 23:59:59

        try {
            // 初始化结果列表
            List<ToppOpportunityVo> resultOppoList = new ArrayList<>();

            // 从sys_param表获取开关的状态
            Map<String, String> unboundOpportunitySwitchMap = sysParamService.findValueMapsByType(Constants.UNBOUND_OPPORTUNITY_OFF_SWITCH);
            if (SwitchStatus.ON.getValue().equals(unboundOpportunitySwitchMap.get(Constants.UNBOUND_OPPORTUNITY_OFF_SWITCH))) {
                // 获取需要更新状态的商机列表（不进行过滤）
                List<ToppOpportunityVo> signedOppoIdList = getDataToProcessWithoutFilter(startTime, endTime);
                // 添加原有查询的数据
                if (!signedOppoIdList.isEmpty()) {
                    signedOppoIdList.forEach(oppo -> oppo.setFinalEndOppTypeMemo("预计签约时间到期后7天，系统自动关闭！"));
                    resultOppoList.addAll(signedOppoIdList);
                }
            }

            // 查询超过3个月+7天未拜访的商机
            Map<String, String> lastVisitTimeSwitchMap = sysParamService.findValueMapsByType(Constants.VISIT_TIME_OFF_SWITCH);
            if (SwitchStatus.ON.getValue().equals(lastVisitTimeSwitchMap.get(Constants.VISIT_TIME_OFF_SWITCH))) {
                List<ToppOpportunityVo> longUnvisitedOppos = getDataOnLastVisitTimeWithoutFilter(bfStartTime, bfEndTime);
                log.info("查询到超过3个月+7天未拜访的商机数量：{}", longUnvisitedOppos != null ? longUnvisitedOppos.size() : 0);
                // 添加超过3个月+7天未拜访的商机
                if (longUnvisitedOppos != null && !longUnvisitedOppos.isEmpty()) {
                    longUnvisitedOppos.forEach(oppo -> oppo.setFinalEndOppTypeMemo("超过三个月未拜访未转化商机，系统自动关闭！"));
                    resultOppoList.addAll(longUnvisitedOppos);
                }
            }

            // 查询创建时间超过3个月+7天无拜访的商机
            Map<String, String> notVisitTimeSwitchMap = sysParamService.findValueMapsByType(Constants.NOT_VISIT_TIME_OFF_SWITCH);
            if (SwitchStatus.ON.getValue().equals(notVisitTimeSwitchMap.get(Constants.NOT_VISIT_TIME_OFF_SWITCH))) {
                List<ToppOpportunityVo> longUnvisitedOppos = getDataOnNotVisitTimeWithoutFilter(bfStartTime, bfEndTime);
                log.info("查询到创建时间超过3个月+7天无拜访的商机数量：{}", longUnvisitedOppos != null ? longUnvisitedOppos.size() : 0);
                // 添加超过3个月+7天未拜访的商机
                if (longUnvisitedOppos != null && !longUnvisitedOppos.isEmpty()) {
                    longUnvisitedOppos.forEach(oppo -> oppo.setFinalEndOppTypeMemo("超过三个月未拜访未转化商机，系统自动关闭！"));
                    resultOppoList.addAll(longUnvisitedOppos);
                }
            }

            if (resultOppoList.isEmpty()) {
                log.info("没有需要更新状态的商机");
                return;
            }

            // 统一过滤所有获取的数据
            List<ToppOpportunityVo> filteredResultList = filterUnboundData(resultOppoList);

            if (filteredResultList == null || filteredResultList.isEmpty()) {
                log.info("经过统一过滤后，没有需要更新状态的商机");
                return;
            }

            //取出需要处理的数据的商机ID
            List<Long> opportunityIds = filteredResultList.stream()
                .map(ToppOpportunityVo::getId)
                .collect(Collectors.toList());

            log.info("开始更新商机状态，商机ID列表：{}", opportunityIds);

            // 处理查询出的数据
            filteredResultList.forEach(toppOpportunityVo -> {
                log.info("更新商机 {} 的状态为终止", toppOpportunityVo.getId());
                toppOpportunityVo.setOppoState(Constants.NEW_OPP_PHASE_OPP_END_LINK);
                Boolean orderFlag = saveOrder(toppOpportunityVo);
                if (orderFlag) {
                    ToppOpportunityVo newVo = new ToppOpportunityVo();
                    BeanCopyUtils.copy(toppOpportunityVo, newVo);
                    newVo.setOppoState(Constants.NEW_OPP_PHASE_OPP_END_LINK);
                    newVo.setAuditStatus(Constants.OPP_STATUS_STOP);
                    newVo.setOppPhase(Constants.OPP_PHASE_OPPO_END);
                    newVo.setOppoState(Constants.OPPO_STATUS_END);
                    newVo.setNewOppPhase(Constants.NEW_OPP_PHASE_OPP_END_LINK);
                    String result = oppoUnBoundSyncService.dealOppoBoJsonStr(newVo, "终结商机");
                    log.info("同步商机状态结果：{}", result);
                    Boolean flag = toppOpportunityService.upppUnbound(toppOpportunityVo.getId(), toppOpportunityVo.getFinalEndOppTypeMemo());
                    log.info("更新商机 {} 的状态为终止结果：{}", toppOpportunityVo.getId(), flag);
                    if(ObjectUtil.equal("预计签约时间到期后7天，系统自动关闭！", toppOpportunityVo.getFinalEndOppTypeMemo())){
                        toppOrderService.updateByOppoId(toppOpportunityVo.getId(),"4");
                    }else{
                        toppOrderService.updateByOppoId(toppOpportunityVo.getId(),"5");
                    }
                }
            });

            log.info("同步未关联商机的合同数据完成");
        } catch (Exception e) {
            log.error("同步未关联商机的合同数据异常", e);
        }

    }

    private List<ToppOpportunityVo> getDataToProcessWithoutFilter(String startTime, String endTime) {
        // 计算查询时间范围
        log.info("查询商机未关闭时间范围：{} 到 {}", startTime, endTime);
        // 1. 查询未关联商机的商机数据
        List<ToppOpportunityVo> opportunityList = toppOpportunityService.queryOpportunityUnbound(startTime, endTime);
        log.info("查询到未关联商机的商机数量：{}", opportunityList.size());
        // 返回原始数据，不进行过滤
        return opportunityList;
    }

    private List<ToppOpportunityVo> getDataOnLastVisitTimeWithoutFilter(String bfStartTime, String bfEndTime) {
        log.info("查询商机超过3个月+7天未拜访时间范围：{} 到 {}", bfStartTime, bfEndTime);
        // 返回原始数据，不进行过滤
        return toppOpportunityService.queryOpportunityByLastVisitTime(bfStartTime, bfEndTime);
    }

    private List<ToppOpportunityVo> getDataOnNotVisitTimeWithoutFilter(String bfStartTime, String bfEndTime) {
        log.info("查询商机创建时间超过3个月+7天无拜访时间范围：{} 到 {}", bfStartTime, bfEndTime);
        // 返回原始数据，不进行过滤
        return toppOpportunityService.queryOpportunityByCreateTime(bfStartTime, bfEndTime);
    }

    /**
     * 过滤未绑定合同、表单和销售数据的商机
     *
     * @param initialOpportunityList 初始商机列表
     * @return 过滤后的商机列表
     */
    private List<ToppOpportunityVo> filterUnboundData(List<ToppOpportunityVo> initialOpportunityList) {
        if (initialOpportunityList.isEmpty()) {
            log.info("没有需要处理的数据");
            return null;
        }

        // 提取商机ID列表
        List<Long> opportunityIds = initialOpportunityList.stream()
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

        List<ToppOpportunityVo> list = initialOpportunityList.stream()
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

        signedOppoIdList = initialOpportunityList.stream()
            .filter(opportunity -> opportunityIds.contains(opportunity.getId()))
            .collect(Collectors.toList());

        log.info("最终需要更新状态的商机数量：{}", opportunityIds.size());

        if (ObjectUtil.isEmpty(opportunityIds)) {
            log.info("没有需要更新状态的商机");
            return null;
        }

        return signedOppoIdList;
    }

     @Transactional
     public Boolean saveOrder(ToppOpportunityVo oppotunity) {
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
        order.setStatus(OrderStatusEnum.END.getCode());
        order.setCustManagerId(su.getId());
        order.setActors(",," + oppotunity.getCustManagerId().toString() + ",,");
        order.setWhiteList("," + su.getId() + ",");
        order.setOrderType(Constants.ACT_OPP_END_AUDIT);
        order.setAreaId(Long.parseLong(dept.getAreaCode()));
        order.setOrgId(nextUser.getOrgId());
        order.setCustId(oppotunity.getCustId());
        order.setActorsInfo(nextUser.getNameCh() + ":" + nextUser.getRelationTel());
        order.setCustName(oppotunity.getCustName());
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setCreator(su.getId());
        order.setProcessEngine(ProcessEngineEnum.OTHER.name());
        //结束原因 0:客户经理已处理 1:未处理但已续约 2:用户离网 3:工单超时 4:预计签约时间到期未转化商机作废 5:未拜访未转化商机作废 6：三个月未支撑商机作废 7:客户经理做商机终止
         // 对应码表end_reason类型
         if(ObjectUtil.equal("预计签约时间到期后7天，系统自动关闭！", oppotunity.getFinalEndOppTypeMemo())){
             order.setEndReason("4");
         }else{
             order.setEndReason("5");
         }
        return toppOrderService.insertByBo(order);
    }
}
