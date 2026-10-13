package com.crm.adapter.admin.system.zqzt;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.vo.TerminalContractWarningDetailExportVo;
import com.crm.system.service.ITerminalContractWarningDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 终端合约到期预警 Controller
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
@RequestMapping("/terminalContractWarning")
public class TerminalContractWarningController extends BaseController {

    private final ITerminalContractWarningDetailService detailService;

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
        List<TerminalContractWarningDetailExportVo> list = new ArrayList<>();
        for (Map<String, Object> r : records) {
            TerminalContractWarningDetailExportVo vo = new TerminalContractWarningDetailExportVo();
            vo.setSvcNum(str(r, "svcNum"));
            vo.setAreaName(str(r, "areaName"));
            vo.setCountyId(str(r, "countyId"));
            vo.setCustName(str(r, "custName"));
            vo.setDeveloperName(str(r, "developerName"));
            vo.setSchemeId(str(r, "schemeId"));
            vo.setSchemeName(str(r, "schemeName"));
            vo.setEffTime(str(r, "effTime"));
            vo.setExpTime(str(r, "expTime"));
            vo.setNewSchemeName(str(r, "newSchemeName"));
            vo.setRenewalTime(str(r, "renewalTime"));
            vo.setNewExpTime(str(r, "newExpTime"));
            vo.setCycleFlagText(str(r, "cycleFlagText"));
            vo.setWarningLevelText(str(r, "warningLevelText"));
            list.add(vo);
        }
        ExcelUtil.exportExcel(list, "终端合约到期预警明细", TerminalContractWarningDetailExportVo.class, response);
    }

    private String str(Map<String, Object> m, String k) {
        Object v = m != null ? m.get(k) : null;
        return v != null ? v.toString() : null;
    }
}
