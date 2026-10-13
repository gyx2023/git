package com.crm.workflow.service.impl;

import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.IGroupBuyWarningDetailService;
import com.crm.system.service.sys.ISysCodeService;
import com.crm.workflow.domain.vo.WfBusinessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 团购折扣到期预警（group_buy_warn）业务上下文提供者
 * <p>
 * 匹配 single_approval 通用流程，提供工单信息、预警明细记录、流程变量、当前任务数据，
 * 以及按 orderType 过滤的维系情况选项（type_=group_buy_warn，setting_str3 区分 1=完成 2=跟踪中 3=转派）。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GroupBuyWarningBusinessContextProvider extends AbstractWarningBusinessContextProvider {

    @Autowired
    private IGroupBuyWarningDetailService detailService;

    @Autowired
    private ISysCodeService sysCodeService;

    @Override
    protected Set<String> getOrderTypes() {
        return Collections.singleton("group_buy_warn");
    }

    @Override
    protected String getSectionKey() {
        return "records";
    }

    @Override
    protected String getSectionTitle() {
        return "团购折扣到期预警明细";
    }

    @Override
    protected String getReminderText() {
        return "专属活动即将到期，请关注并做好提前维系";
    }

    @Override
    protected void enrichExtraData(WfBusinessContext ctx, Map<String, Object> extraData,
                                   Map<String, Object> procVars, ToppOrderVo order) {
        // 查询预警明细记录
        try {
            List<Map<String, Object>> records = detailService.queryRecords(order.getId());
            extraData.put("records", records);
        } catch (Exception e) {
            log.warn("查询group_buy_warn预警明细失败: orderId={}", order.getId(), e);
            extraData.put("records", new ArrayList<>());
        }

        // 维系情况选项：团购折扣独立下拉，按 setting_str3(1=完成 2=跟踪中 3=转派) 分组返回
        try {
            extraData.put("followUpOptions", sysCodeService.getCodeMapGroupBySettingStr3(
                    "group_buy_warn"));
        } catch (Exception e) {
            log.warn("查询团购折扣到期预警维系情况选项失败", e);
            extraData.put("followUpOptions", new LinkedHashMap<>());
        }
    }
}
