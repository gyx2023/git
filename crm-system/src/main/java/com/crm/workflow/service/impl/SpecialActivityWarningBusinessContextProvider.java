package com.crm.workflow.service.impl;

import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.ISpecialActivityWarningDetailService;
import com.crm.system.service.sys.ISysCodeService;
import com.crm.workflow.domain.vo.WfBusinessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 特殊活动到期预警（special_activity_warn）业务上下文提供者
 * <p>
 * 匹配 single_approval 通用流程，提供工单信息、预警明细记录、流程变量、当前任务数据，
 * 以及按 orderType 过滤的跟踪情况选项（special_activity_follow_up）。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpecialActivityWarningBusinessContextProvider extends AbstractWarningBusinessContextProvider {

    @Autowired
    private ISpecialActivityWarningDetailService detailService;

    @Autowired
    private ISysCodeService sysCodeService;

    @Override
    protected Set<String> getOrderTypes() {
        return Collections.singleton("special_activity_warn");
    }

    @Override
    protected String getSectionKey() {
        return "records";
    }

    @Override
    protected String getSectionTitle() {
        return "特殊活动到期预警明细";
    }

    @Override
    protected String getReminderText() {
        return "特殊活动即将到期，请关注并做好提前维系";
    }

    @Override
    protected void enrichExtraData(WfBusinessContext ctx, Map<String, Object> extraData,
                                   Map<String, Object> procVars, ToppOrderVo order) {
        // 查询预警明细记录
        try {
            List<Map<String, Object>> records = detailService.queryRecords(order.getId());
            extraData.put("records", records);
        } catch (Exception e) {
            log.warn("查询special_activity_warn预警明细失败: orderId={}", order.getId(), e);
            extraData.put("records", new ArrayList<>());
        }

        // 维系情况选项（按 orderType 过滤 special_activity_follow_up）
        try {
            extraData.put("followUpOptions", sysCodeService.getCodeMapByTypeAndSettingStr3(
                    "special_activity_follow_up", "special_activity_warn"));
        } catch (Exception e) {
            log.warn("查询special_activity_warn维系情况选项失败", e);
            extraData.put("followUpOptions", new LinkedHashMap<>());
        }
    }
}
