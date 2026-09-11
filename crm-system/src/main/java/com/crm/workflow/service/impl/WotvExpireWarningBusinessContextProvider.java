package com.crm.workflow.service.impl;

import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.IWotvExpireWarningDetailService;
import com.crm.system.service.sys.ISysCodeService;
import com.crm.workflow.domain.vo.WfBusinessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 沃TV到期预警（wotv_expire_warning）业务上下文提供者
 * <p>
 * 匹配 single_approval 通用流程，提供工单信息、预警明细记录、流程变量、当前任务数据、
 * 以及按 orderType 过滤的跟踪情况选项（wide_follow_up）。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WotvExpireWarningBusinessContextProvider extends AbstractWarningBusinessContextProvider {

    @Autowired
    private IWotvExpireWarningDetailService detailService;

    @Autowired
    private ISysCodeService sysCodeService;

    @Override
    protected Set<String> getOrderTypes() {
        return Collections.singleton("wotv_expire_warning");
    }

    @Override
    protected String getSectionKey() {
        return "records";
    }

    @Override
    protected String getSectionTitle() {
        return "沃TV到期预警明细";
    }

    @Override
    protected String getReminderText() {
        return "宽带业务即将到期";
    }

    @Override
    protected void enrichExtraData(WfBusinessContext ctx, Map<String, Object> extraData,
                                   Map<String, Object> procVars, ToppOrderVo order) {
        // 查询预警明细记录列表
        try {
            List<Map<String, Object>> records = detailService.queryRecords(order.getId());
            extraData.put("records", records);
        } catch (Exception e) {
            log.warn("查询wotv_expire_warning预警明细失败: orderId={}", order.getId(), e);
            extraData.put("records", new ArrayList<>());
        }

        // 跟踪情况选项：沃TV独立下拉（type=wotv_expire_warning），setting_str3 区分 1=完成 2=跟踪中 3=转派
        try {
            extraData.put("followUpOptions", sysCodeService.getCodeMapGroupBySettingStr3(
                    "wotv_expire_warning"));
        } catch (Exception e) {
            log.warn("查询wotv_expire_warning跟踪情况选项失败", e);
            extraData.put("followUpOptions", new LinkedHashMap<>());
        }
    }
}
