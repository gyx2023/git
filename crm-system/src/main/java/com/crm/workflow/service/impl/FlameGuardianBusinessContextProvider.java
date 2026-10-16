package com.crm.workflow.service.impl;

import cn.hutool.json.JSONUtil;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.vo.ZqztLedgerAreaVo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.IZqztLedgerAreaService;
import com.crm.workflow.domain.vo.WfBusinessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 火焰卫士一体化交付 待办业务上下文
 * <p>
 * 待办表单初值取建单时写入的流程变量快照（fg_*），不回查 OB。
 * </p>
 */
@Slf4j
@Component
public class FlameGuardianBusinessContextProvider extends AbstractWarningBusinessContextProvider {

    private static final String ORDER_TYPE = "flame_guardian";

    /** 流程变量前缀 */
    private static final String VAR_PREFIX = "fg_";

    /** 快照字段：流程变量后缀 → formInitial 的键（前端表单字段名） */
    private static final String[][] FORM_KEYS = {
            {"orderNo", "fg_orderNo"},
            {"signDate", "fg_signDate"},
            {"orderCreateTime", "fg_orderCreateTime"},
            {"cityId", "fg_cityId"},
            {"cityName", "fg_cityName"},
            {"companyName", "fg_companyName"},
            {"creditCode", "fg_creditCode"},
            {"regAddress", "fg_regAddress"},
            {"actualAddress", "fg_actualAddress"},
            {"signRoutes", "fg_signRoutes"},
            {"companyPrincipal", "fg_companyPrincipal"},
            {"principalPhone", "fg_principalPhone"},
            {"safetyOfficer1", "fg_safetyOfficer1"},
            {"safetyOfficer1Phone", "fg_safetyOfficer1Phone"},
            {"unicomManager", "fg_unicomManager"},
            {"unicomManagerPhone", "fg_unicomManagerPhone"},
            {"unicomManagerCounty", "fg_unicomManagerCounty"},
            {"broadbandIsReuse", "fg_broadbandIsReuse"},
            {"broadbandAccount", "fg_broadbandAccount"},
            {"broadbandIsSporadic", "fg_broadbandIsSporadic"},
            {"broadbandSporadicOrderNo", "fg_broadbandSporadicOrderNo"},
            {"broadbandAddNum", "fg_broadbandAddNum"},
            {"cloudAccount", "fg_cloudAccount"},
            {"cloudVisionPackageId", "fg_cloudVisionPackageId"},
            {"networkAccountList", "fg_networkAccountList"}
    };

    /** 日期型快照字段的输出格式：formInitial 键 → 格式（未列出的字段原样透传） */
    private static final Map<String, String> DATE_FORMATS;

    /**
     * 客户经理「通过」时写回的表单快照流程变量名（前端页面形态 JSON，
     * 见 {@code FlameGuardianDealController#complete}）。存在时只读页优先用它渲染。
     */
    private static final String VAR_SUBMITTED_FORM = "fg_submittedForm";

    static {
        Map<String, String> dateFormats = new HashMap<>(2);
        dateFormats.put("signDate", "yyyy-MM-dd HH:mm:ss");
        dateFormats.put("orderCreateTime", "yyyy-MM-dd HH:mm:ss");
        DATE_FORMATS = Collections.unmodifiableMap(dateFormats);
    }

    @Autowired
    private IZqztLedgerAreaService ledgerAreaService;

    @Override
    protected Set<String> getOrderTypes() {
        return Collections.singleton(ORDER_TYPE);
    }

    @Override
    protected String getSectionKey() {
        return "flameGuardian";
    }

    @Override
    protected String getSectionTitle() {
        return "火焰卫士一体化交付";
    }

    @Override
    protected String getReminderText() {
        return "请核对并补全企业/设备信息后提交，通过后台账将同步火焰卫士平台";
    }

    @Override
    protected void enrichExtraData(WfBusinessContext ctx, Map<String, Object> extraData,
                                   Map<String, Object> procVars, ToppOrderVo order) {
        if (extraData == null) {
            return;
        }
        Map<String, Object> formInitial = new HashMap<>();
        for (String[] pair : FORM_KEYS) {
            Object value = procVars == null ? null : procVars.get(pair[1]);
            if (value != null) {
                formInitial.put(pair[0], toFormValue(pair[0], value));
            }
        }
        extraData.put("formInitial", formInitial);

        Object submitted = procVars == null ? null : procVars.get(VAR_SUBMITTED_FORM);
        if (submitted instanceof String && StringUtils.isNotBlank((String) submitted)) {
            try {
                extraData.put("submittedForm", JSONUtil.parseObj((String) submitted));
            } catch (Exception e) {
                log.warn("【火焰卫士待办】提交快照解析失败，前端回退到建单快照", e);
            }
        }

        // 三级联动下拉：地市 → 区县 → 街道
        extraData.put("cityOptions", queryChildren(null));
        Object cityId = formInitial.get("cityId");
        if (cityId != null) {
            extraData.put("districtOptions", queryChildren(String.valueOf(cityId)));
        }
    }

    /**
     * 快照值转表单初值：日期型流程变量格式化为字符串，其余类型原样返回。
     * <p>
     * fg_signDate / fg_orderCreateTime 由建单写入 java.util.Date，直接下发会被序列化成
     * 时间戳或 ISO-8601 字符串，前端 el-date-picker 的 value-format 需要精确匹配的字符串。
     * </p>
     */
    private Object toFormValue(String key, Object value) {
        if (value instanceof Date) {
            String pattern = DATE_FORMATS.get(key);
            if (pattern != null) {
                return DateUtils.parseDateToStr(pattern, (Date) value);
            }
        }
        return value;
    }

    private List<ZqztLedgerAreaVo> queryChildren(String parentCode) {
        try {
            return ledgerAreaService.queryChildren(parentCode);
        } catch (Exception e) {
            log.warn("【火焰卫士待办】取区域下拉失败: parentCode={}", parentCode, e);
            return Collections.emptyList();
        }
    }
}
