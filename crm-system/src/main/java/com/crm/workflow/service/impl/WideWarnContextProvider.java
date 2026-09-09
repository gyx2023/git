package com.crm.workflow.service.impl;

import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.sys.ISysCodeService;
import com.crm.workflow.domain.vo.WfBusinessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 宽带/双线到期预警（Wide / double_wide / double_wide_second）业务上下文提供者
 * <p>
 * Wide（C00002 首次预警）走 single_approval 通用流程，无 wide_second_new_* 流程变量，
 * 此处对其补充按 orderType 过滤的跟踪情况选项（wide_follow_up），供处理操作下拉使用；
 * 其余类型沿用 AbstractWideWarnContextProvider 的宽带升级逻辑。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
public class WideWarnContextProvider extends AbstractWideWarnContextProvider {

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
        // C00002 首次预警：补充跟踪情况选项（对齐 WotvExpireWarningBusinessContextProvider）
        // sys_code 约定：Wide 维系情况独立 type='Wide'（setting_str3='Wide'），不复用老框架
        // 共用的 type='wide_follow_up'（那 13 条中文码属老宽带系，仍在被老框架 ACTIVITI 单使用）
        if (order != null && "Wide".equals(order.getOrderType())) {
            try {
                extraData.put("followUpOptions",
                        sysCodeService.getCodeMapByTypeAndSettingStr3("Wide", "Wide"));
            } catch (Exception e) {
                log.warn("查询宽带到期预警跟踪情况选项失败", e);
                extraData.put("followUpOptions", new LinkedHashMap<>());
            }
        }
    }
}
