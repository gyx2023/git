package com.crm.adapter.admin.system.zqzt;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.vo.DoubleWideExpireWarningDetailExportVo;
import com.crm.system.service.IDoubleWideExpireWarningDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 双线到期预警 Controller
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
@RequestMapping("/doubleWideExpireWarning")
public class DoubleWideExpireWarningController extends BaseController {

    private final IDoubleWideExpireWarningDetailService detailService;

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
        List<DoubleWideExpireWarningDetailExportVo> list = new ArrayList<>();
        for (Map<String, Object> r : records) {
            DoubleWideExpireWarningDetailExportVo vo = new DoubleWideExpireWarningDetailExportVo();
            vo.setSvcNum(str(r, "svcNum"));
            vo.setCustName(str(r, "custName"));
            vo.setContactMan(str(r, "contactMan"));
            vo.setContactPhone(str(r, "contactPhone"));
            vo.setDeveloperName(str(r, "developerName"));
            vo.setSchemeName(str(r, "schemeName"));
            vo.setLvlName(str(r, "lvlName"));
            vo.setServiceName(str(r, "serviceName"));
            vo.setConnType(str(r, "connType"));
            vo.setEffTime(str(r, "effTime"));
            vo.setExpTime(str(r, "expTime"));
            vo.setAmountPerYear(str(r, "amountPerYear"));
            vo.setRate(str(r, "rate"));
            vo.setUserAddr(str(r, "userAddr"));
            vo.setExteFlag(str(r, "exteFlag"));
            vo.setNewSchemeName(str(r, "newSchemeName"));
            vo.setNewCreateTime(str(r, "newCreateTime"));
            vo.setNewEffTime(str(r, "newEffTime"));
            vo.setNewExpTime(str(r, "newExpTime"));
            vo.setNewSchemePeriod(str(r, "newSchemePeriod"));
            vo.setNewSchemeFee(str(r, "newSchemeFee"));
            vo.setRelationTypeName(str(r, "relationTypeName"));
            vo.setUserStatus(str(r, "userStatus"));
            list.add(vo);
        }
        ExcelUtil.exportExcel(list, "双线到期预警明细", DoubleWideExpireWarningDetailExportVo.class, response);
    }

    private String str(Map<String, Object> m, String k) {
        Object v = m != null ? m.get(k) : null;
        return v != null ? v.toString() : null;
    }
}
