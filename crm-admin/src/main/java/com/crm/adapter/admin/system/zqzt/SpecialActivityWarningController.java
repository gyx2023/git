package com.crm.adapter.admin.system.zqzt;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.vo.SpecialActivityWarningDetailExportVo;
import com.crm.system.service.ISpecialActivityWarningDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 特殊活动到期预警 Controller
 * <p>
 * 提供预警明细查询、导出等业务专属接口。
 * 流程操作（complete/reject/transfer/transferList/auditHandlers/track）已统一
 * 由 WideWarningController（/wideWarning）提供。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/specialActivityWarning")
public class SpecialActivityWarningController extends BaseController {

    private final ISpecialActivityWarningDetailService detailService;

    /**
     * 分页查询工单关联的预警明细记录列表
     */
    @GetMapping("/records")
    public TableDataInfo<Map<String, Object>> records(@RequestParam("orderId") Long orderId, PageQuery pageQuery) {
        return detailService.queryPageRecords(orderId, pageQuery);
    }

    /**
     * 导出预警明细
     */
    @PostMapping("/export")
    public void export(@RequestParam("orderId") Long orderId, HttpServletResponse response) {
        List<Map<String, Object>> records = detailService.queryRecords(orderId);
        List<SpecialActivityWarningDetailExportVo> list = new ArrayList<>();
        for (Map<String, Object> r : records) {
            SpecialActivityWarningDetailExportVo vo = new SpecialActivityWarningDetailExportVo();
            vo.setAreaName(str(r, "areaName"));
            vo.setCountyName(str(r, "countyName"));
            vo.setSvcNum(str(r, "svcNum"));
            vo.setCustName(str(r, "custName"));
            vo.setDeveloperName(str(r, "developerName"));
            vo.setSchemeId(str(r, "schemeId"));
            vo.setSchemeName(str(r, "schemeName"));
            vo.setServiceName(str(r, "serviceName"));
            vo.setProdType(str(r, "prodType"));
            vo.setEffTime(str(r, "effTime"));
            vo.setExpTime(str(r, "expTime"));
            vo.setIsXy(str(r, "isXy"));
            vo.setNewSchemeName(str(r, "newSchemeName"));
            vo.setRenewalTime(str(r, "renewalTime"));
            vo.setNewEffTime(str(r, "newEffTime"));
            vo.setNewExpTime(str(r, "newExpTime"));
            list.add(vo);
        }
        ExcelUtil.exportExcel(list, "特殊活动到期预警明细", SpecialActivityWarningDetailExportVo.class, response);
    }

    private String str(Map<String, Object> m, String k) {
        Object v = m != null ? m.get(k) : null;
        return v != null ? v.toString() : null;
    }
}
