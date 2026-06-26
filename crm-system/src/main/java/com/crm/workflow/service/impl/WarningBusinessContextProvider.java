package com.crm.workflow.service.impl;

import com.crm.common.utils.StringUtils;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.workflow.domain.vo.WfBusinessAction;
import com.crm.workflow.domain.vo.WfBusinessContext;
import com.crm.workflow.domain.vo.WfBusinessSection;
import com.crm.workflow.service.BusinessContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 预警流程业务上下文提供者
 * 匹配 single_approval 通用流程，根据 topp_order.order_type 区分预警类型提供不同数据
 *
 * @author crm
 */
@Component
@RequiredArgsConstructor
public class WarningBusinessContextProvider implements BusinessContextProvider {

    private final IToppOrderService orderService;

    @Override
    public boolean supports(String procDefKey, String source) {
        return "single_approval".equals(procDefKey);
    }

    @Override
    public WfBusinessContext provide(String procInsId, String source, String procDefKey) {
        ToppOrderVo order = orderService.queryByPiId(procInsId);
        if (order == null) {
            return null;
        }

        WfBusinessContext ctx = new WfBusinessContext();
        List<WfBusinessSection> sections = new ArrayList<>();

        // 通用订单信息
        sections.add(new WfBusinessSection("order", "工单信息", 1, buildOrderSection(order)));

        // 按预警类型补充业务数据
        String orderType = order.getOrderType();
        sections.addAll(buildWarningSpecificSections(orderType, order));

        ctx.setSections(sections);

        // 新系统流程可添加操作按钮
        if ("new".equals(source)) {
            List<WfBusinessAction> actions = new ArrayList<>();
            actions.add(new WfBusinessAction("agree", "已处理", "primary", "el-icon-circle-check"));
            actions.add(new WfBusinessAction("reject", "退回", "danger", "el-icon-close"));
            ctx.setActions(actions);
        }

        return ctx;
    }

    /**
     * 构建通用工单信息区块
     */
    private Map<String, Object> buildOrderSection(ToppOrderVo order) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("工单编号", order.getOrderNumber());
        map.put("工单名称", order.getOrderName());
        map.put("客户名称", order.getCustName());
        map.put("预警等级", order.getWarningLevel());
        map.put("当前阶段", order.getPhaseName());
        map.put("状态", order.getStatus());
        return map;
    }

    /**
     * 按预警类型构建业务数据区块
     */
    private List<WfBusinessSection> buildWarningSpecificSections(String orderType, ToppOrderVo order) {
        List<WfBusinessSection> sections = new ArrayList<>();

        switch (orderType) {
            case "htdq_warn":
                // 合同到期预警 - 补充合同信息
                sections.add(new WfBusinessSection("contract", "合同信息", 2, Collections.singletonMap(
                    "提醒内容", "合同即将到期，请及时跟进续约"
                )));
                break;
            case "qf_warn":
            case "yebz_warn":
                // 欠费预警 / 余额不足预警
                sections.add(new WfBusinessSection("payment", "缴费信息", 2, Collections.singletonMap(
                    "提醒内容", "客户存在欠费，请及时催缴"
                )));
                break;
            case "income_wave_warn":
                // 收入波动预警
                sections.add(new WfBusinessSection("income", "收入信息", 2, Collections.singletonMap(
                    "提醒内容", "客户收入出现波动，请及时关注"
                )));
                break;
            case "hmd_warn":
                // 红名单到期预警
                sections.add(new WfBusinessSection("hmd", "红名单信息", 2, Collections.singletonMap(
                    "提醒内容", "红名单即将到期，请及时处理"
                )));
                break;
            case "group_buy_warn":
                // 团购折扣到期预警
                sections.add(new WfBusinessSection("groupBuy", "团购信息", 2, Collections.singletonMap(
                    "提醒内容", "团购折扣即将到期"
                )));
                break;
            case "special_activity_warn":
                // 特殊活动到期预警
                sections.add(new WfBusinessSection("activity", "活动信息", 2, Collections.singletonMap(
                    "提醒内容", "特殊活动即将到期"
                )));
                break;
            case "terminal_contract_warn":
                // 终端合约到期预警
                sections.add(new WfBusinessSection("terminal", "终端合约信息", 2, Collections.singletonMap(
                    "提醒内容", "终端合约即将到期"
                )));
                break;
            case "cloud_expire_alarm":
                // 云业务到期预警
                sections.add(new WfBusinessSection("cloud", "云业务信息", 2, Collections.singletonMap(
                    "提醒内容", "云业务即将到期"
                )));
                break;
            case "cust_receipt_warn":
                // 预打发票预警
                sections.add(new WfBusinessSection("receipt", "发票信息", 2, Collections.singletonMap(
                    "提醒内容", "预打发票到期预警"
                )));
                break;
            case "cut_over_warn":
                // 割接预警
                sections.add(new WfBusinessSection("cutOver", "割接信息", 2, Collections.singletonMap(
                    "提醒内容", "割接任务即将到期"
                )));
                break;
            case "user_cust_owe_warn":
                // 用户欠费预警
                sections.add(new WfBusinessSection("userOwe", "用户欠费信息", 2, Collections.singletonMap(
                    "提醒内容", "用户存在欠费，请及时催缴"
                )));
                break;
            case "Wide":
            case "double_wide":
            case "wide_second":
            case "double_wide_second":
                // 宽带/双线到期预警
                sections.add(new WfBusinessSection("broadband", "宽带信息", 2, Collections.singletonMap(
                    "提醒内容", "宽带/双线服务即将到期"
                )));
                break;
            case "third_check_warn":
                // 三方稽核预警
                sections.add(new WfBusinessSection("thirdCheck", "稽核信息", 2, Collections.singletonMap(
                    "提醒内容", "三方稽核结果异常"
                )));
                break;
            case "kd_addr_warn":
                // 宽带装机地址预警
                sections.add(new WfBusinessSection("addr", "装机地址信息", 2, Collections.singletonMap(
                    "提醒内容", "装机地址疑似聚合或非真实"
                )));
                break;
            case "pcdn_warn":
                // 违规PCDN业务稽核
                sections.add(new WfBusinessSection("pcdn", "PCDN信息", 2, Collections.singletonMap(
                    "提醒内容", "违规PCDN业务稽核预警"
                )));
                break;
            case "double_zero_warn":
                // 双线0出账预警
                sections.add(new WfBusinessSection("doubleZero", "出账信息", 2, Collections.singletonMap(
                    "提醒内容", "双线0出账预警"
                )));
                break;
            case "si_settle_warn":
                // SI结算稽核预警
                sections.add(new WfBusinessSection("siSettle", "结算信息", 2, Collections.singletonMap(
                    "提醒内容", "SI结算稽核预警"
                )));
                break;
            case "leaseline_relet":
                // 双线起租预警
                sections.add(new WfBusinessSection("lease", "起租信息", 2, Collections.singletonMap(
                    "提醒内容", "双线起租预警"
                )));
                break;
            case "ictArrearsWarn":
                // ICT欠费预警
                sections.add(new WfBusinessSection("ict", "ICT欠费信息", 2, Collections.singletonMap(
                    "提醒内容", "ICT欠费预警"
                )));
                break;
            default:
                // 未知预警类型，仅显示基础信息
                break;
        }

        return sections;
    }
}
