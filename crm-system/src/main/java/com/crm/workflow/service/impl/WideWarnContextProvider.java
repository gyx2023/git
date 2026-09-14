package com.crm.workflow.service.impl;

import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.IWideExpireWarningDetailService;
import com.crm.system.service.sys.ISysCodeService;
import com.crm.workflow.domain.vo.WfBusinessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 宽带/双线到期预警（Wide / double_wide / double_wide_second）业务上下文提供者
 * <p>
 * Wide（C00002 首次预警）走 single_approval 通用流程，无 wide_second_new_* 流程变量，
 * 此处为其补充用户清单（records，取自 wide_sys_auto_warning，与老框架 Wide 处理页同源）
 * 与按 setting_str3 分组的跟踪情况选项（对齐 WotvExpireWarningBusinessContextProvider）；
 * 其余类型沿用 AbstractWideWarnContextProvider 的宽带升级逻辑。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
public class WideWarnContextProvider extends AbstractWideWarnContextProvider {

    @Autowired
    private IWideExpireWarningDetailService wideDetailService;

    @Autowired
    private ISysCodeService sysCodeService;

    @Override
    protected Set<String> getOrderTypes() {
        return new HashSet<>(Arrays.asList("Wide", "double_wide", "double_wide_second"));
    }
    @Override
    protected String getSectionKey() { return "broadband"; }
    @Override
    protected String getSectionTitle() { return "宽带信息"; }
    @Override
    protected String getReminderText() { return "宽带/双线服务即将到期"; }

    @Override
    protected void enrichExtraData(WfBusinessContext ctx, Map<String, Object> extraData,
                                   Map<String, Object> procVars, ToppOrderVo order) {
        super.enrichExtraData(ctx, extraData, procVars, order);
        // C00002 首次预警专属补充（对齐 WotvExpireWarningBusinessContextProvider）
        if (order != null && "Wide".equals(order.getOrderType())) {
            // 用户清单：与老框架 Wide 处理页同源（wide_sys_auto_warning 按 main_order_flow_no），
            // 供 H5/PC 明细展示；缺失会让前端明细区与计数为空
            try {
                List<Map<String, Object>> records = wideDetailService.queryRecords(order.getId());
                extraData.put("records", records);
            } catch (Exception e) {
                log.warn("查询宽带到期预警明细失败: orderId={}", order.getId(), e);
                extraData.put("records", new ArrayList<>());
            }

            // 跟踪情况选项：Wide 独立下拉（type='Wide'），setting_str3 区分 1=完成 2=跟踪中 3=转派，
            // 返回 {setting_str3: {code: name}}，供前端按 action 直接取组（不再按 code 十位分组）
            // 不复用老框架共用的 type='wide_follow_up'（那 13 条中文码属老宽带系，仍在被老框架 ACTIVITI 单使用）
            try {
                extraData.put("followUpOptions", sysCodeService.getCodeMapGroupBySettingStr3("Wide"));
            } catch (Exception e) {
                log.warn("查询宽带到期预警跟踪情况选项失败", e);
                extraData.put("followUpOptions", new LinkedHashMap<>());
            }
        }
    }
}
