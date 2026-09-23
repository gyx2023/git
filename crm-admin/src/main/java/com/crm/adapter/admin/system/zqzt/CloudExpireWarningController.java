package com.crm.adapter.admin.system.zqzt;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.vo.CloudExpireWarningDetailExportVo;
import com.crm.system.service.ICloudExpireWarningDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 云业务到期预警 Controller
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
@RequestMapping("/cloudExpireWarning")
public class CloudExpireWarningController extends BaseController {

    private final ICloudExpireWarningDetailService detailService;

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
        List<CloudExpireWarningDetailExportVo> list = new ArrayList<>();
        for (Map<String, Object> r : records) {
            CloudExpireWarningDetailExportVo vo = new CloudExpireWarningDetailExportVo();
            vo.setAreaName(str(r, "areaName"));
            vo.setCountyName(str(r, "countyName"));
            vo.setSerialNumber(str(r, "serialNumber"));
            vo.setGrpName(str(r, "grpName"));
            vo.setOpenDate(str(r, "openDate"));
            vo.setDeveloperName(str(r, "developerName"));
            vo.setCloudTag(str(r, "cloudTag"));
            vo.setConsumeType(str(r, "consumeType"));
            vo.setConsumeTime(str(r, "consumeTime"));
            vo.setEndTime(str(r, "endTime"));
            vo.setServerDuration(str(r, "serverDuration"));
            vo.setAmountPerYear(str(r, "amountPerYear"));
            vo.setIsRenew(str(r, "isRenew"));
            vo.setNewCloudTag(str(r, "newCloudTag"));
            vo.setNewConsumeType(str(r, "newConsumeType"));
            vo.setRenewalTime(str(r, "renewalTime"));
            vo.setNewEndTime(str(r, "newEndTime"));
            vo.setNewServerDuration(str(r, "newServerDuration"));
            vo.setNewAmountPerYear(str(r, "newAmountPerYear"));
            list.add(vo);
        }
        ExcelUtil.exportExcel(list, "云业务到期预警明细", CloudExpireWarningDetailExportVo.class, response);
    }

    private String str(Map<String, Object> m, String k) {
        Object v = m != null ? m.get(k) : null;
        return v != null ? v.toString() : null;
    }
}
