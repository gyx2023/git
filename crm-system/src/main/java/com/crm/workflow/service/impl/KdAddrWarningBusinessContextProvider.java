package com.crm.workflow.service.impl;

import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.IKdAddrWarningDetailService;
import com.crm.workflow.domain.vo.WfBusinessContext;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 宽带装机地址疑似聚合/非真实预警（kd_addr_warn）业务上下文提供者
 * <p>
 * 匹配 single_approval/double_approval 通用流程，提供工单信息、预警明细记录、流程变量和当前任务数据。
 * 需注入 IKdAddrWarningDetailService 查询明细，因此独立于普通的 simple warning provider。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class KdAddrWarningBusinessContextProvider extends AbstractWarningBusinessContextProvider {

    @Autowired
    private IKdAddrWarningDetailService detailService;

    @Override
    protected Set<String> getOrderTypes() {
        return Collections.singleton("kd_addr_warn");
    }

    @Override
    protected String getSectionKey() {
        return "records";
    }

    @Override
    protected String getSectionTitle() {
        return "装机地址预警明细";
    }

    @Override
    protected String getReminderText() {
        return "装机地址疑似聚合或非真实";
    }

    @Override
    protected Map<String, Object> buildCurrentTaskInfo(Task task) {
        Map<String, Object> ct = super.buildCurrentTaskInfo(task);
        // 覆盖 canAct 计算：支持多候选人逗号分隔的 assignee
        try {
            LoginUser loginUser = LoginHelper.getLoginUser();
            String assignee = task.getAssignee();
            if (loginUser != null && assignee != null && !assignee.isEmpty()) {
                ct.put("canAct", Arrays.asList(assignee.split(","))
                        .contains(String.valueOf(loginUser.getUserId())));
            }
        } catch (Exception ignored) {
            // 后台任务等无登录态场景
        }
        return ct;
    }

    @Override
    protected void enrichExtraData(WfBusinessContext ctx, Map<String, Object> extraData,
                                   Map<String, Object> procVars, ToppOrderVo order) {
        // 查询预警明细记录列表
        try {
            List<Map<String, Object>> records = detailService.queryRecords(order.getId());
            extraData.put("records", records);
        } catch (Exception e) {
            log.warn("查询kd_addr_warn预警明细失败: orderId={}", order.getId(), e);
            extraData.put("records", new ArrayList<>());
        }
    }
}
