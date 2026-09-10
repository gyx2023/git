package com.crm.workflow.service.impl;

import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.stats.IStatsService;
import com.crm.workflow.domain.vo.WfBusinessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 红名单升级预警（new_hmd_up）业务上下文提供者
 * <p>
 * 升级预警（new_wide_up / new_hmd_up）共用订单变量前缀 wide_second_new_*（历史命名，
 * 对齐老框架 WarningUpJob.saveOrderVariables），但客户列表/话术统计按预警各自独立查询：
 * 红名单走 buildHmdWarnCustSql / getHmdUpSpeechcraftStats（order_type='hmd_warn'），
 * 不复用宽带升级（new_wide_up）的宽带 SQL（order_type='Wide'），故仅继承中性基类
 * {@link AbstractWarningBusinessContextProvider}（与 Wotv / KdAddr provider 同款），
 * 不继承宽带专属的 {@link AbstractWideWarnContextProvider}。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
public class NewHmdUpWarnContextProvider extends AbstractWarningBusinessContextProvider {

    /** 预警客户列表默认页大小（与分页接口 /wideWarning/custList 默认值一致） */
    private static final int CUST_LIST_DEFAULT_PAGE_SIZE = 20;

    @Autowired
    private IStatsService statsService;

    @Override
    protected Set<String> getOrderTypes() { return Collections.singleton("new_hmd_up"); }
    @Override
    protected String getSectionKey() { return "hmdUp"; }
    @Override
    protected String getSectionTitle() { return "红名单升级信息"; }
    @Override
    protected String getReminderText() { return "红名单到期升级预警"; }

    @Override
    protected void enrichExtraData(WfBusinessContext ctx, Map<String, Object> extraData,
                                   Map<String, Object> procVars, ToppOrderVo order) {
        // 预警信息（升级预警统一以 wide_second_new_ 前缀存储流程变量）
        Map<String, Object> warnInfo = new HashMap<>();
        warnInfo.put("period", str(procVars, "wide_second_new_period"));
        warnInfo.put("orderFlag", str(procVars, "wide_second_new_orderFlag"));
        warnInfo.put("codeInfo", str(procVars, "wide_second_new_codeInfo"));
        extraData.put("warnInfo", warnInfo);

        String codeInfo = str(procVars, "wide_second_new_codeInfo");
        String period = str(procVars, "wide_second_new_period");
        String orderFlag = str(procVars, "wide_second_new_orderFlag");
        if (codeInfo == null || period == null || orderFlag == null) {
            return;
        }
        try {
            // 红名单客户明细（order_type='hmd_warn'），首屏返回第一页，翻页走 /wideWarning/custList 分页接口
            String sql = statsService.buildHmdWarnCustSql(codeInfo, period, orderFlag);
            com.baomidou.mybatisplus.core.metadata.IPage<Map<String, Object>> custPage =
                    statsService.queryHmdWarnCust(sql, 1, CUST_LIST_DEFAULT_PAGE_SIZE);
            extraData.put("custList", custPage.getRecords());
            extraData.put("custTotal", custPage.getTotal());
        } catch (Exception e) { log.warn("查询红名单升级客户列表失败", e); }
        try {
            // 红名单话术统计（hmdWarnTotal / overTimeTotal）
            Map<String, Object> speechcraftMap = statsService.getHmdUpSpeechcraftStats(codeInfo, period, orderFlag);
            extraData.put("speechcraftMap", speechcraftMap);
            extraData.put("codeName", speechcraftMap.getOrDefault("codeName",
                    statsService.getCodeName(codeInfo, orderFlag)));
        } catch (Exception e) { log.warn("查询红名单升级话术统计失败", e); }
    }
}
