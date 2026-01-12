package com.crm.job.service.oppo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.crm.common.core.domain.dto.SalesupBakTblBidSuboppoDTO;
import com.crm.common.enums.OppoConversionStateEnum;
import com.crm.common.utils.BeanCopyUtils;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.topp.ToppOpportunityBo;
import com.crm.system.domain.topp.ToppBidSuboppoDo;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import com.crm.system.service.topp.IToppBidSuboppoService;
import com.crm.system.service.topp.IToppOpportunityService;
import com.crm.system.service.zbzq.ISalesupBakTblBidSuboppoService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商机合同同步任务
 * 功能：同步未关联商机的合同数据到ToppBidSuboppo表，并更新商机转换状态
 * @author 优化后
 */
@Slf4j
@Component
public class OppoSynchronousJob {

    @Resource
    private ISalesupBakTblBidSuboppoService salesupBakTblBidSuboppoService;

    @Resource
    private IToppBidSuboppoService toppBidSuboppoService;

    @Resource
    private IToppOpportunityService opportunityService;

    /**
     * 同步未关联商机的合同数据
     * XXL-Job任务标识：oppoUnboundSyncJob
     */
    @XxlJob("oppoUnboundSyncJob")
    public void excute() {
        log.info("开始同步未关联商机的合同数据，任务参数：{}", XxlJobHelper.getJobParam());
        // 定义返回结果，便于XXL-Job查看执行状态
        XxlJobHelper.handleSuccess("同步任务执行完成");

        try {
            String param = XxlJobHelper.getJobParam();
            // 1. 查询原始合同数据
            List<SalesupBakTblBidSuboppoDTO> originList = salesupBakTblBidSuboppoService.querySalesupBidList(param);
            if (CollUtil.isEmpty(originList)) {
                log.info("未查询到待同步的合同数据，任务结束");
                return;
            }

            // 2. 提取所有商机编码，并去重（避免重复查询）
            Set<String> allOppoCodes = originList.stream()
                .map(SalesupBakTblBidSuboppoDTO::getOppoCode)
                .filter(StringUtils::isNotEmpty) // 过滤空编码，避免无效查询
                .collect(Collectors.toSet());
            if (CollUtil.isEmpty(allOppoCodes)) {
                log.info("所有合同数据均无有效商机编码，任务结束");
                return;
            }

            // 3. 查询已存在的商机编码，筛选出待同步的商机编码（用Set提升查询效率）
            List<String> existOppoCodes = toppBidSuboppoService.selectBidSuboppoList(new ArrayList<>(allOppoCodes));
            Set<String> toSyncOppoCodes = new HashSet<>(allOppoCodes);
            toSyncOppoCodes.removeAll(existOppoCodes);
            if (CollUtil.isEmpty(toSyncOppoCodes)) {
                log.info("所有商机编码均已关联，无需同步，任务结束");
                return;
            }

            // 4. 筛选出待同步的合同数据
            List<SalesupBakTblBidSuboppoDTO> toSyncContractList = originList.stream()
                .filter(dto -> toSyncOppoCodes.contains(dto.getOppoCode()))
                .collect(Collectors.toList());

            // 5. 批量查询商机信息（核心优化：从循环查询改为批量查询，减少DB交互）
            Map<String, ToppOpportunityVo> oppoCodeToOpportunityMap = batchQueryOpportunityByOppoCodes(toSyncOppoCodes);
            if (CollUtil.isEmpty(oppoCodeToOpportunityMap)) {
                log.info("待同步的商机编码均无对应的商机信息，任务结束");
                return;
            }

            // 6. 组装待插入的ToppBidSuboppoDo列表，并批量更新商机状态
            List<ToppBidSuboppoDo> toInsertList = new ArrayList<>();
            List<ToppOpportunityBo> toUpdateOpportunityList = new ArrayList<>();
            for (SalesupBakTblBidSuboppoDTO dto : toSyncContractList) {
                String oppoCode = dto.getOppoCode();
                ToppOpportunityVo opportunityVo = oppoCodeToOpportunityMap.get(oppoCode);
                if (ObjectUtil.isNull(opportunityVo)) {
                    log.warn("商机编码{}无对应的商机信息，跳过同步", oppoCode);
                    continue;
                }

                // 组装插入对象
                ToppBidSuboppoDo bidSuboppoDo = BeanCopyUtils.copy(dto, ToppBidSuboppoDo.class);
                bidSuboppoDo.setProjectId(opportunityVo.getId());
                toInsertList.add(bidSuboppoDo);

                // 组装更新对象（商机转换状态）
                ToppOpportunityBo updateBo = buildOpportunityUpdateBo(opportunityVo);
                toUpdateOpportunityList.add(updateBo);
            }

            // 7. 批量执行数据库操作（减少事务次数，提升性能）
            if (CollUtil.isNotEmpty(toUpdateOpportunityList)) {
                opportunityService.batchUpdateByBo(toUpdateOpportunityList);
                log.info("批量更新商机状态完成，更新数量：{}", toUpdateOpportunityList.size());
            }

            if (CollUtil.isNotEmpty(toInsertList)) {
                toppBidSuboppoService.insertDoBatch(toInsertList);
                log.info("批量插入合同数据完成，插入数量：{}", toInsertList.size());
            }

            log.info("未关联商机的合同数据同步完成，总计处理{}条，成功插入{}条，更新商机状态{}条",
                toSyncContractList.size(), toInsertList.size(), toUpdateOpportunityList.size());

        } catch (Exception e) {
            log.error("同步未关联商机的合同数据异常", e);
            XxlJobHelper.handleFail("同步任务执行失败：" + e.getMessage());
            // 抛出异常，让XXL-Job感知任务失败
            throw new RuntimeException("同步任务执行异常", e);
        }
    }

    /**
     * 批量查询商机信息，返回「商机编码-商机VO」映射
     * 核心优化：替代循环内单次查询，减少数据库交互次数
     */
    private Map<String, ToppOpportunityVo> batchQueryOpportunityByOppoCodes(Set<String> oppoCodes) {
        List<ToppOpportunityVo> opportunityVoList = opportunityService.queryListByOppoCodes(oppoCodes);

        // 转换为Map，便于后续快速查询
        return opportunityVoList.stream()
            .collect(Collectors.toMap(
                ToppOpportunityVo::getOppCode, // key：商机编码
                vo -> vo,                      // value：商机VO
                (v1, v2) -> v1                 // 去重：保留第一个
            ));
    }

    /**
     * 构建商机状态更新对象
     * 封装状态更新逻辑，提升代码可读性
     */
    private ToppOpportunityBo buildOpportunityUpdateBo(ToppOpportunityVo opportunityVo) {
        ToppOpportunityBo updateBo = BeanCopyUtils.copy(opportunityVo, ToppOpportunityBo.class);
        String targetState = OppoConversionStateEnum.BID_INFO_ASSOCIATION.getCode();

        // 处理商机转换状态
        if (StringUtils.isEmpty(updateBo.getOppoConversionState())) {
            updateBo.setOppoConversionState(targetState);
        } else if (!StringUtils.contains(updateBo.getOppoConversionState(), targetState)) {
            updateBo.setOppoConversionState(updateBo.getOppoConversionState() + "," + targetState);
        }
        return updateBo;
    }
}
