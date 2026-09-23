package com.crm.workflow.service.impl;

import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.ICloudExpireWarningDetailService;
import com.crm.system.service.sys.ISysCodeService;
import com.crm.workflow.domain.vo.WfBusinessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 云业务到期预警（cloud_expire_alarm）业务上下文提供者
 * <p>
 * 匹配 single_approval 通用流程，提供工单信息、预警明细记录（ftp_tc_grp_cloud_expire_alarm）、
 * 流程变量、当前任务数据，以及按处理结果分组的维系情况选项。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CloudExpireWarningBusinessContextProvider extends AbstractWarningBusinessContextProvider {

    @Autowired
    private ICloudExpireWarningDetailService detailService;

    @Autowired
    private ISysCodeService sysCodeService;

    @Override
    protected Set<String> getOrderTypes() {
        return Collections.singleton("cloud_expire_alarm");
    }

    @Override
    protected String getSectionKey() {
        return "records";
    }

    @Override
    protected String getSectionTitle() {
        return "云业务到期预警明细";
    }

    @Override
    protected String getReminderText() {
        return "云业务即将到期";
    }

    @Override
    protected void enrichExtraData(WfBusinessContext ctx, Map<String, Object> extraData,
                                   Map<String, Object> procVars, ToppOrderVo order) {
        // 查询预警明细记录列表
        try {
            List<Map<String, Object>> records = detailService.queryRecords(order.getId());
            extraData.put("records", records);
        } catch (Exception e) {
            log.warn("查询cloud_expire_alarm预警明细失败: orderId={}", order.getId(), e);
            extraData.put("records", new ArrayList<>());
        }

        // 维系情况选项：云业务到期预警独立下拉（type=cloud_expire_alarm），setting_str3 区分 1=完成 2=跟踪中 3=转派
        try {
            extraData.put("followUpOptions", sysCodeService.getCodeMapGroupBySettingStr3(
                    "cloud_expire_alarm"));
        } catch (Exception e) {
            log.warn("查询cloud_expire_alarm维系情况选项失败", e);
            extraData.put("followUpOptions", new LinkedHashMap<>());
        }
    }
}
