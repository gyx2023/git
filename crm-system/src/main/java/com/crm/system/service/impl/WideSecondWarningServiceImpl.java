package com.crm.system.service.impl;

import com.crm.common.enums.OrderStatusEnum;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.mapper.WideSecondWarningDetailMapper;
import com.crm.system.service.IWideSecondWarningService;
import com.crm.system.service.sys.ISysCodeService;
import com.crm.system.service.topp.IToppOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 宽带二次预警 Service 实现
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WideSecondWarningServiceImpl implements IWideSecondWarningService {

    private final WideSecondWarningDetailMapper mapper;
    private final IToppOrderService orderService;
    private final ISysCodeService sysCodeService;
    private final TaskService taskService;


    @Override
    public Map<String, Object> pageRecords(Long orderId, int pageNum, int pageSize, String targetDate) {
        List<Map<String, Object>> all = queryAllRecords(orderId, targetDate);
        int total = all.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<Map<String, Object>> pageRecords;
        if (fromIndex >= total) {
            pageRecords = new ArrayList<>();
        } else {
            pageRecords = all.subList(fromIndex, toIndex);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", pageRecords);
        result.put("total", total);
        return result;
    }

    @Override
    public List<Map<String, Object>> queryAllRecords(Long orderId, String targetDate) {
        String mainOrderFlowNo = resolveMainOrderFlowNo(orderId);
        if (mainOrderFlowNo == null || targetDate == null) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> rows = mapper.selectDetailByOrderFlowNo(mainOrderFlowNo, targetDate);
        log.info("【wide_second】查询明细: orderId={}, mainOrderFlowNo={}, targetDate={}, 记录数={}",
            orderId, mainOrderFlowNo, targetDate, rows.size());
        return rows;
    }

    @Override
    public List<Map<String, Object>> queryExportRecords(Long orderId, String targetDate) {
        String mainOrderFlowNo = resolveMainOrderFlowNo(orderId);
        if (mainOrderFlowNo == null || targetDate == null) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> rows = mapper.selectAllDetailByOrderFlowNo(mainOrderFlowNo, targetDate);
        log.info("【wide_second】查询导出明细: orderId={}, mainOrderFlowNo={}, targetDate={}, 记录数={}",
            orderId, mainOrderFlowNo, targetDate, rows.size());
        return rows;
    }

    @Override
    public int countRecords(Long orderId, String targetDate) {
        String mainOrderFlowNo = resolveMainOrderFlowNo(orderId);
        if (mainOrderFlowNo == null || targetDate == null) {
            return 0;
        }
        return mapper.countDetailByOrderFlowNo(mainOrderFlowNo, targetDate);
    }

    @Override
    public Map<String, String> getFollowUpOptions() {
        return sysCodeService.getCodeMapByType("wide_follow_up");
    }


    /**
     * 完成（通过）—— 事务性业务操作。
     * 调用方（Controller）负责流程编排：preserveDisplayVars → 本方法 → isProcessRunning/syncOrder。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> completeOrder(Long orderId, Task task, String comment,
                                             String followUp, String otherReason) {
        String taskId = task.getId();
        String piId = task.getProcessInstanceId();

        if (comment != null) {
            taskService.addComment(taskId, piId, comment);
        }

        // 保存跟踪情况到工单
        ToppOrderBo bo = new ToppOrderBo();
        bo.setId(orderId);
        bo.setEndReason("0");
        if (followUp != null) {
            if ("其他".equals(followUp) && otherReason != null) {
                bo.setWideResult(otherReason);
            } else {
                bo.setWideResult(followUp);
            }
        }
        orderService.updateByBo(bo);

        // 完成任务
        Map<String, Object> vars = new HashMap<>();
        vars.put("action", "agree");
        taskService.setVariableLocal(taskId, "action", "agree");
        taskService.complete(taskId, vars);

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        return result;
    }

    /**
     * 跟踪中 —— 工单置 tracking 状态，任务不完成。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> trackOrder(Long orderId, Task task, String comment) {
        if (comment != null) {
            taskService.addComment(task.getId(), task.getProcessInstanceId(), comment);
        }

        ToppOrderBo bo = new ToppOrderBo();
        bo.setId(orderId);
        bo.setStatus(OrderStatusEnum.TRACKING.getCode());
        orderService.updateByBo(bo);

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("msg", "该流程正在跟踪中");
        return result;
    }


    private String resolveMainOrderFlowNo(Long orderId) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null || order.getInfoId() == null) {
            log.warn("【wide_second】工单不存在或无关联记录, orderId={}", orderId);
            return null;
        }
        String mainOrderFlowNo = mapper.selectMainOrderFlowNoById(order.getInfoId());
        if (mainOrderFlowNo == null) {
            log.warn("【wide_second】未找到主订单流水号, infoId={}", order.getInfoId());
        }
        return mainOrderFlowNo;
    }
}
