package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.enums.OrderStatusEnum;
import com.crm.common.utils.Desensitization;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.dao.sys.SysCode;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.mapper.WideSecondWarningDetailMapper;
import com.crm.system.mapper.sys.SysCodeMapper;
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
    private final SysCodeMapper sysCodeMapper;
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
        applyDesensitization(rows);
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
        applyDesensitization(rows);
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


    /**
     * 对用户明细记录进行脱敏处理。
     * 对业务号码、用户姓名、联系人、联系电话、安装地址五个字段进行脱敏。
     * 脱敏受 sys_code 表中 type_='masking_switch' AND name_='脱敏开关' 记录控制，
     * code_ 为 "1" 时生效。
     *
     * @param records 明细记录列表，key 为数据库字段名（svc_num、cust_name 等）
     */
    private void applyDesensitization(List<Map<String, Object>> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        // 查询脱敏开关
        boolean desensitizeEnabled = false;
        try {
            SysCode sysCode = sysCodeMapper.selectOne(
                new LambdaQueryWrapper<SysCode>()
                    .eq(SysCode::getType, "masking_switch")
                    .eq(SysCode::getName, "脱敏开关")
            );
            if (sysCode != null && "1".equals(sysCode.getCode())) {
                desensitizeEnabled = true;
            }
        } catch (Exception e) {
            log.warn("【wide_second】查询脱敏开关配置失败，默认不脱敏: {}", e.getMessage());
        }

        if (!desensitizeEnabled) {
            log.debug("【wide_second】脱敏开关未开启，不进行脱敏处理");
            return;
        }

        log.debug("【wide_second】脱敏开关已开启，开始脱敏处理，记录数={}", records.size());

        try {
            for (Map<String, Object> row : records) {
                // 业务号码：保留前3后3（旧框架 accNbr → strMasking(value, 3, 3)）
                maskField(row, "svc_num", v -> Desensitization.maskMiddle(v, 3, 3));

                // 用户姓名：保留前3后3（旧框架 cname → strMasking(value, 3, 3)）
                maskField(row, "cust_name", v -> Desensitization.maskMiddle(v, 3, 3));

                // 联系人：保留首字（旧框架 uname → chineseName）
                maskField(row, "contact_man", Desensitization::desensitizeName);

                // 联系电话：保留前3后4（旧框架 phone → mobilePhone）
                maskField(row, "contact_phone", Desensitization::desensitizePhone);

                // 安装地址：末尾20字符脱敏（旧框架 addr → address(value, 20)）
                maskField(row, "user_addr", v -> Desensitization.desensitizeAddress(v, 20));
            }
        } catch (Exception e) {
            log.error("【wide_second】脱敏处理失败: {}", e.getMessage(), e);
            // 脱敏失败不影响主流程，继续返回数据
        }
    }


    /**
     * 对单条记录的指定字段进行脱敏
     *
     * @param row     记录 Map
     * @param key     字段名
     * @param masker  脱敏函数
     */
    private void maskField(Map<String, Object> row, String key, java.util.function.Function<String, String> masker) {
        Object value = row.get(key);
        if (value != null) {
            row.put(key, masker.apply(value.toString()));
        }
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
