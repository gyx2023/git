package com.crm.workflow.service.impl;

import com.crm.common.utils.StringUtils;
import com.crm.system.domain.vo.SysCodeVo;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.sys.ISysCodeService;
import com.crm.system.service.topp.IToppOpportunityService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.workflow.domain.vo.WfBusinessAction;
import com.crm.workflow.domain.vo.WfBusinessContext;
import com.crm.workflow.domain.vo.WfBusinessSection;
import com.crm.workflow.service.BusinessContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import cn.hutool.core.bean.BeanUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 默认商机/工单业务上下文提供者（topp_* 流程）
 * 匹配所有 topp_ 开头的流程定义，通过 order 查询订单和商机信息
 * 自动将 code 字段转译为中文标签
 *
 * @author crm
 */
@Component
@RequiredArgsConstructor
public class DefaultToppBusinessContextProvider implements BusinessContextProvider {

    private final IToppOrderService orderService;
    private final IToppOpportunityService opportunityService;
    private final ISysCodeService sysCodeService;

    @Override
    public boolean supports(String procDefKey, String source, ToppOrderVo order) {
        return procDefKey != null && procDefKey.startsWith("topp_") && order != null;
    }

    @Override
    public WfBusinessContext provide(String procInsId, String source, String procDefKey, ToppOrderVo order) {
        WfBusinessContext ctx = new WfBusinessContext();
        List<WfBusinessSection> sections = new ArrayList<>();

        // 订单详情（code → 中文）
        sections.add(new WfBusinessSection("order", "订单处理", 1, enrichOrderMap(order)));

        // 商机详情（code → 中文）
        if (order.getOppoId() != null) {
            ToppOpportunityVo oppo = opportunityService.queryById(order.getOppoId());
            if (oppo != null) {
                sections.add(new WfBusinessSection("opportunity", "商机详情", 2, enrichOppoMap(oppo)));
            }
        }

        ctx.setSections(sections);

        // topp_close_process 流程添加商机关闭操作按钮
        if ("new".equals(source) && "topp_close_process".equals(procDefKey)) {
            List<WfBusinessAction> actions = new ArrayList<>();
            actions.add(new WfBusinessAction("close_oppo", "关闭商机", "primary", "el-icon-circle-check"));
            actions.add(new WfBusinessAction("modify_sign_time", "修改签约时间", "warning", "el-icon-edit"));
            actions.add(new WfBusinessAction("conversion", "转化", "success", "el-icon-refresh"));
            ctx.setActions(actions);
        }

        return ctx;
    }

    // ========== 字段转译辅助方法 ==========

    /**
     * 查 sys_code 表，将 code 转译为中文名，查不到时返回原值。
     * 支持逗号分隔的多值 code（如 ",opp_type_security,opp_type_cloud," → "网络安全,云计算"）
     */
    private Object codeToName(String type, String code) {
        if (StringUtils.isBlank(code)) {
            return code;
        }
        // 多值 code：按逗号拆分，逐项转译再拼接
        if (code.indexOf(',') >= 0) {
            String[] parts = code.split(",");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                if (part.isEmpty()) {
                    continue;
                }
                SysCodeVo vo = sysCodeService.findCode(type, part);
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(vo != null && StringUtils.isNotBlank(vo.getName()) ? vo.getName() : part);
            }
            return sb.length() > 0 ? sb.toString() : code;
        }
        // 单值 code
        SysCodeVo vo = sysCodeService.findCode(type, code);
        if (vo != null && StringUtils.isNotBlank(vo.getName())) {
            return vo.getName();
        }
        return code;
    }

    /**
     * 将字段名→sys_code type 的映射定义，批量转译
     */
    private Object enrich(Map<String, Object> map, String fieldKey, String codeType) {
        Object raw = map.get(fieldKey);
        if (raw instanceof String && StringUtils.isNotBlank((String) raw)) {
            return codeToName(codeType, (String) raw);
        }
        // Integer 类型的 code（如 fiveMergeState=1）也转
        if (raw instanceof Integer) {
            return codeToName(codeType, String.valueOf(raw));
        }
        return raw;
    }

    /**
     * 将 ToppOrderVo → LinkedHashMap，code 字段转中文
     */
    private Map<String, Object> enrichOrderMap(ToppOrderVo vo) {
        Map<String, Object> m = toMap(vo);
        m.put("orderType", enrich(m, "orderType", "process_key"));
        m.put("status", enrich(m, "status", "order_status"));
        m.put("endReason", enrich(m, "endReason", "end_reason"));
        m.put("businessType", enrich(m, "businessType", "business_type"));
        m.put("businessTwoType", enrich(m, "businessTwoType", "business_two_type"));
        m.put("orderSource", enrich(m, "orderSource", "zqzq_order_source"));
        m.put("orderCategory", enrich(m, "orderCategory", "order_category"));
        return m;
    }

    /**
     * 将 ToppOpportunityVo → LinkedHashMap，code 字段转中文
     */
    private Map<String, Object> enrichOppoMap(ToppOpportunityVo vo) {
        Map<String, Object> m = toMap(vo);
        m.put("oppSource", enrich(m, "oppSource", "clue_source"));
        m.put("oppType", enrich(m, "oppType", "opp_type"));
        m.put("oppIncludeType", enrich(m, "oppIncludeType", "opp_type"));
        m.put("oppPhase", enrich(m, "oppPhase", "oppo_state"));
        m.put("unicomType", enrich(m, "unicomType", "unicom_type"));
        m.put("unicomType2", enrich(m, "unicomType2", "unicom_type_2"));
        m.put("bgType", enrich(m, "bgType", "bg_type_new"));
        m.put("isValid", enrich(m, "isValid", "yes_or_no"));
        m.put("oppDataReform", enrich(m, "oppDataReform", "data_reform"));
        m.put("fiveMergeState", enrich(m, "fiveMergeState", "five_merge_state"));
        m.put("fiveMergeCode", enrich(m, "fiveMergeCode", "merge_code"));
        m.put("referTerminal", enrich(m, "referTerminal", "refer_terminal"));
        m.put("preDecision", enrich(m, "preDecision", "pre_decision"));
        m.put("endOppType", enrich(m, "endOppType", "stop_opp_type"));
        m.put("auditStatus", enrich(m, "auditStatus", "opp_audit_status"));
        m.put("designType", enrich(m, "designType", "design_type"));
        m.put("sfoppoType", enrich(m, "sfoppoType", "opp_sf_type"));
        m.put("newOppPhase", enrich(m, "newOppPhase", "new_opp_phase"));
        return m;
    }

    /**
     * 利用 Hutool BeanUtil 将 Java Bean 转为有序 Map
     */
    private static <T> Map<String, Object> toMap(T bean) {
        return BeanUtil.beanToMap(bean, false, true);
    }
}
