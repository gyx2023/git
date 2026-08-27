package com.crm.workflow.service.impl;

import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.stats.IStatsService;
import com.crm.workflow.domain.vo.WfBusinessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * 宽带/红名单升级预警公共基类
 * <p>
 * 共享 wide_second_new 模块的流程变量和客户列表/话术统计查询逻辑。
 * 子类只需声明订单类型集合和区块信息。
 * </p>
 *
 * @author crm
 */
@Slf4j
public abstract class AbstractWideWarnContextProvider extends AbstractWarningBusinessContextProvider {

    /** 预警客户列表默认页大小（与前端分页 /wideWarning/custList 默认值一致） */
    protected static final int CUST_LIST_DEFAULT_PAGE_SIZE = 20;

    @Autowired
    protected IStatsService statsService;

    @Override
    protected void enrichExtraData(WfBusinessContext ctx, Map<String, Object> extraData,
                                   Map<String, Object> procVars, ToppOrderVo order) {
        // 预警信息
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
            String sql = statsService.buildWideCustInfoStatsSql(codeInfo, period, orderFlag);
            // 详情首屏只返回第一页（默认页大小，与 /wideWarning/custList 分页接口一致），翻页走分页接口
            com.baomidou.mybatisplus.core.metadata.IPage<Map<String, Object>> custPage =
                    statsService.queryWideCustInfoStats(sql, 1, CUST_LIST_DEFAULT_PAGE_SIZE);
            extraData.put("custList", custPage.getRecords());
            extraData.put("custTotal", custPage.getTotal());
        } catch (Exception e) { log.warn("查询客户列表失败", e); }
        try {
            Map<String, Object> speechcraftMap = statsService.getWideUpSpeechcraftStats(codeInfo, period, orderFlag);
            extraData.put("speechcraftMap", speechcraftMap);
            extraData.put("codeName", speechcraftMap.getOrDefault("codeName",
                    statsService.getCodeName(codeInfo, orderFlag)));
        } catch (Exception e) { log.warn("查询话术统计失败", e); }
    }
}
