package com.crm.adapter.admin.system.zqzt;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.vo.WideSecondWarningDetailExportVo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.IWideSecondWarningService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.workflow.service.IWfProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 双线二次预警 Controller
 * <p>
 * 用户明细（wide_sys_auto_warning）与宽带二次预警共用
 * {@link com.crm.system.mapper.WideSecondWarningDetailMapper} / {@link IWideSecondWarningService}，
 * 仅到期日期流程变量名不同（double_wide_second-date）。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/doubleWideSecondWarning")
public class DoubleWideSecondWarningController extends BaseController {

    private final IWideSecondWarningService wideSecondService;
    private final IWfProcessService wfProcessService;
    private final IToppOrderService orderService;


    @GetMapping("/records")
    public R<Map<String, Object>> records(@RequestParam("orderId") Long orderId,
                                          @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                          @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        String targetDate = resolveTargetDate(orderId);
        Map<String, Object> result = wideSecondService.pageRecords(orderId, pageNum, pageSize, targetDate);
        return R.ok(result);
    }

    @PostMapping("/export")
    public void export(@RequestParam("orderId") Long orderId, HttpServletResponse response) {
        String targetDate = resolveTargetDate(orderId);
        List<Map<String, Object>> records = wideSecondService.queryExportRecords(orderId, targetDate);
        List<WideSecondWarningDetailExportVo> list = new ArrayList<>();
        for (Map<String, Object> r : records) {
            WideSecondWarningDetailExportVo vo = new WideSecondWarningDetailExportVo();
            vo.setSvcNum(str(r, "svc_num"));
            vo.setCustName(str(r, "cust_name"));
            vo.setContactMan(str(r, "contact_man"));
            vo.setContactPhone(str(r, "contact_phone"));
            vo.setDeveloperName(str(r, "developer_name"));
            vo.setSchemeName(str(r, "scheme_name"));
            vo.setLvlName(str(r, "lvl_name"));
            vo.setServiceName(str(r, "service_name"));
            vo.setConnType(str(r, "conn_type"));
            vo.setAmountPerYear(str(r, "amount_per_year"));
            vo.setEffTime(str(r, "eff_time"));
            vo.setExpTime(str(r, "exp_time"));
            vo.setRate(str(r, "rate_"));
            vo.setUserAddr(str(r, "user_addr"));
            Object exteFlag = r.get("exte_flag");
            vo.setExteFlagStr("1".equals(String.valueOf(exteFlag)) ? "是" : "否");
            vo.setNewSchemeName(str(r, "new_scheme_name"));
            vo.setNewCreateTime(str(r, "new_create_time"));
            vo.setNewEffTime(str(r, "new_eff_time"));
            vo.setNewExpTime(str(r, "new_exp_time"));
            vo.setNewSchemePeriod(str(r, "new_scheme_period"));
            vo.setNewSchemeFee(str(r, "new_scheme_fee"));
            Object relationType = r.get("relation_type_name");
            vo.setRelationTypeStr(relationType != null && !"".equals(relationType) ? "是" : "否");
            vo.setNewSchemeId(str(r, "new_scheme_id"));
            vo.setNewSchemeNameFusion(str(r, "new_scheme_name"));
            vo.setNewCreateTimeFusion(str(r, "new_create_time"));
            vo.setNewEffTimeFusion(str(r, "new_eff_time"));
            vo.setNewExpTimeFusion(str(r, "new_exp_time"));
            vo.setUserStatus(str(r, "user_status"));
            list.add(vo);
        }
        ExcelUtil.exportExcel(list, "双线到期详情", WideSecondWarningDetailExportVo.class, response);
    }

    @GetMapping("/followUpOptions")
    public R<Map<String, String>> followUpOptions() {
        return R.ok(wideSecondService.getFollowUpOptions());
    }

    /** 从流程变量解析 targetDate（double_wide_second-date） */
    private String resolveTargetDate(Long orderId) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null || order.getPiId() == null) {
            return null;
        }
        Map<String, Object> procVars = wfProcessService.loadProcVars(order.getPiId());
        Object dateObj = procVars.get("double_wide_second-date");
        return dateObj != null ? dateObj.toString() : null;
    }

    private String str(Map<String, Object> m, String k) {
        Object v = m != null ? m.get(k) : null;
        return v != null ? v.toString() : null;
    }
}
