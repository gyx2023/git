package com.crm.job.service.oppo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.util.DateUtils;
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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商机合同同步任务
 * 功能：同步未关联商机的合同数据到ToppBidSuboppo表，并更新商机转换状态
 *
 * @author 优化后
 */
@Slf4j
@Component
public class OppoSynchronousJob {

    // ========== 常量提取：避免硬编码，便于维护 ==========
    /**
     * XXL-Job任务标识
     */
    private static final String JOB_HANDLER_NAME = "oppoSynchronousJob";
    /**
     * 日期格式常量
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    /**
     * 投标信息关联状态码
     */
    private static final String BID_INFO_ASSOCIATION_CODE = OppoConversionStateEnum.BID_INFO_ASSOCIATION.getCode();

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
    @XxlJob(JOB_HANDLER_NAME)
    public void execute() {
        long startTime = System.currentTimeMillis();
        String jobParam = XxlJobHelper.getJobParam();
        log.info("【商机合同同步任务】开始执行，任务参数：{}，开始时间：{}", jobParam, DateUtil.now());

        try {
            // 1. 参数校验与处理：提前判断，减少后续无效逻辑
            String syncDate = this.validateAndProcessParam(jobParam);
            if (syncDate == null) {
                XxlJobHelper.handleSuccess("【商机合同同步任务】参数无效，任务结束");
                return;
            }

            // 2. 核心同步逻辑（事务控制：保证插入和更新原子性）
            this.doSync(syncDate);

            // 3. 任务执行成功
            long costTime = System.currentTimeMillis() - startTime;
            String successMsg = String.format("【商机合同同步任务】执行完成，耗时：%dms", costTime);
            log.info(successMsg);
            XxlJobHelper.handleSuccess(successMsg);

        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            String errorMsg = String.format("【商机合同同步任务】执行失败，耗时：%dms，异常信息：%s", costTime, e.getMessage());
            log.error(errorMsg, e);
            XxlJobHelper.handleFail(errorMsg);
            throw new RuntimeException("商机合同同步任务执行异常", e);
        }
    }

    /**
     * 参数校验与处理
     *
     * @param jobParam 任务参数
     * @return 处理后的同步日期（null表示参数无效）
     */
    private String validateAndProcessParam(String jobParam) {
        // 空参数：默认使用当前日期
        if (StringUtils.isEmpty(jobParam)) {
            String defaultDate = DateUtils.format(new Date(), DATE_FORMAT);
            log.info("【商机合同同步任务】任务参数为空，默认同步日期：{}", defaultDate);
            return defaultDate;
        }
        return jobParam;
    }

    /**
     * 核心同步逻辑（加事务，保证数据一致性）
     *
     * @param syncDate 同步日期
     */
    @Transactional(rollbackFor = Exception.class)
    public void doSync(String syncDate) {
        // 1. 查询原始合同数据
        List<SalesupBakTblBidSuboppoDTO> originContractList = salesupBakTblBidSuboppoService.querySalesupBidList(syncDate);
        if (CollUtil.isEmpty(originContractList)) {
            log.info("【商机合同同步任务】未查询到{}日期的待同步合同数据，任务结束", syncDate);
            return;
        }
        log.info("【商机合同同步任务】查询到{}日期的原始合同数据共{}条", syncDate, originContractList.size());

        // 2. 提取并去重商机编码
        Set<String> allOppoCodes = originContractList.stream()
            .map(SalesupBakTblBidSuboppoDTO::getOppoCode)
            .filter(StringUtils::isNotEmpty) // 过滤空编码，避免无效查询
            .collect(Collectors.toSet());
        if (CollUtil.isEmpty(allOppoCodes)) {
            log.info("【商机合同同步任务】所有合同数据均无有效商机编码，任务结束");
            return;
        }
        log.info("【商机合同同步任务】提取到有效商机编码共{}个", allOppoCodes.size());

        // 3. 筛选待同步的商机编码（排除已关联的）
        List<String> existOppoCodes = toppBidSuboppoService.selectBidSuboppoList(new ArrayList<>(allOppoCodes));
        Set<String> toSyncOppoCodes = new HashSet<>(allOppoCodes);
        existOppoCodes.forEach(toSyncOppoCodes::remove);
        if (CollUtil.isEmpty(toSyncOppoCodes)) {
            log.info("【商机合同同步任务】所有商机编码均已关联，无需同步");
            return;
        }
        log.info("【商机合同同步任务】待同步的商机编码共{}个", toSyncOppoCodes.size());

        // 4. 筛选待同步的合同数据
        List<SalesupBakTblBidSuboppoDTO> toSyncContractList = originContractList.stream()
            .filter(dto -> toSyncOppoCodes.contains(dto.getOppoCode()))
            .collect(Collectors.toList());
        log.info("【商机合同同步任务】待同步的合同数据共{}条", toSyncContractList.size());

        // 5. 批量查询商机信息（减少DB交互）
        Map<String, ToppOpportunityVo> oppoCode2OpportunityMap = this.batchQueryOpportunityByOppoCodes(toSyncOppoCodes);
        if (CollUtil.isEmpty(oppoCode2OpportunityMap)) {
            log.info("【商机合同同步任务】待同步的商机编码均无对应商机信息，任务结束");
            return;
        }

        // 6. 组装插入/更新数据
        List<ToppBidSuboppoDo> toInsertList = new ArrayList<>();
        List<ToppOpportunityBo> toUpdateList = new ArrayList<>();
        for (SalesupBakTblBidSuboppoDTO dto : toSyncContractList) {
            String oppoCode = dto.getOppoCode();
            ToppOpportunityVo opportunityVo = oppoCode2OpportunityMap.get(oppoCode);

            if (ObjectUtil.isNull(opportunityVo)) {
                log.warn("【商机合同同步任务】商机编码{}无对应商机信息，跳过同步", oppoCode);
                continue;
            }

            // 组装插入对象
            ToppBidSuboppoDo bidSuboppoDo = this.buildToppBidSuboppoDo(dto, opportunityVo);
            toInsertList.add(bidSuboppoDo);

            // 组装更新对象
            ToppOpportunityBo updateBo = this.buildOpportunityUpdateBo(opportunityVo);
            toUpdateList.add(updateBo);
        }

        // 7. 批量执行数据库操作（分段处理，避免大数据量导致性能问题）
        this.batchInsertBidSuboppo(toInsertList);
        this.batchUpdateOpportunity(toUpdateList);

        // 8. 最终结果日志
        log.info("【商机合同同步任务】同步完成：待处理{}条，成功插入{}条，成功更新商机状态{}条",
            toSyncContractList.size(), toInsertList.size(), toUpdateList.size());
    }

    /**
     * 批量查询商机信息，返回「商机编码-商机VO」映射
     */
    private Map<String, ToppOpportunityVo> batchQueryOpportunityByOppoCodes(Set<String> oppoCodes) {
        List<ToppOpportunityVo> opportunityVoList = opportunityService.queryListByOppoCodes(oppoCodes);
        log.info("【商机合同同步任务】批量查询到商机信息共{}条", opportunityVoList.size());

        return opportunityVoList.stream()
            .collect(Collectors.toMap(
                ToppOpportunityVo::getOppCode,
                vo -> vo,
                (v1, v2) -> {
                    log.warn("【商机合同同步任务】商机编码{}存在重复数据，保留第一条", v1.getOppCode());
                    return v1; // 去重：保留第一个
                }
            ));
    }

    /**
     * 组装待插入的ToppBidSuboppoDo对象
     */
    private ToppBidSuboppoDo buildToppBidSuboppoDo(SalesupBakTblBidSuboppoDTO dto, ToppOpportunityVo opportunityVo) {
        ToppBidSuboppoDo bidSuboppoDo = BeanCopyUtils.copy(dto, ToppBidSuboppoDo.class);
        bidSuboppoDo.setProjectId(opportunityVo.getId());
        // 可扩展：补充创建人/创建时间等公共字段
        bidSuboppoDo.setCreateTime(new Date());
        return bidSuboppoDo;
    }

    /**
     * 构建商机状态更新对象
     */
    private ToppOpportunityBo buildOpportunityUpdateBo(ToppOpportunityVo opportunityVo) {
        ToppOpportunityBo updateBo = BeanCopyUtils.copy(opportunityVo, ToppOpportunityBo.class);
        String currentState = updateBo.getOppoConversionState();

        // 状态处理逻辑：空值则直接设置，非空则追加（避免重复追加）
        if (StringUtils.isEmpty(currentState)) {
            updateBo.setOppoConversionState(BID_INFO_ASSOCIATION_CODE);
        } else if (!currentState.contains(BID_INFO_ASSOCIATION_CODE)) {
            updateBo.setOppoConversionState(currentState + "," + BID_INFO_ASSOCIATION_CODE);
        }
        // 可扩展：补充更新人/更新时间等公共字段
        updateBo.setUpdateTime(new Date());
        return updateBo;
    }

    /**
     * 批量插入合同数据（分段处理，避免单次插入过多）
     */
    private void batchInsertBidSuboppo(List<ToppBidSuboppoDo> toInsertList) {
        if (CollUtil.isEmpty(toInsertList)) {
            log.info("【商机合同同步任务】无待插入的合同数据");
            return;
        }

        // 分段插入：每500条一批（可根据数据库性能调整）
        List<List<ToppBidSuboppoDo>> partitionList = CollUtil.split(toInsertList, 500);
        for (List<ToppBidSuboppoDo> partition : partitionList) {
            toppBidSuboppoService.insertDoBatch(partition);
            log.info("【商机合同同步任务】批量插入合同数据{}条", partition.size());
        }
    }

    /**
     * 批量更新商机状态（分段处理）
     */
    private void batchUpdateOpportunity(List<ToppOpportunityBo> toUpdateList) {
        if (CollUtil.isEmpty(toUpdateList)) {
            log.info("【商机合同同步任务】无待更新的商机数据");
            return;
        }

        // 分段更新：每500条一批
        List<List<ToppOpportunityBo>> partitionList = CollUtil.split(toUpdateList, 500);
        for (List<ToppOpportunityBo> partition : partitionList) {
            opportunityService.batchUpdateByBo(partition);
            log.info("【商机合同同步任务】批量更新商机状态{}条", partition.size());
        }
    }
}
