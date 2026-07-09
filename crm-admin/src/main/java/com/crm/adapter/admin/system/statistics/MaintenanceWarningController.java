package com.crm.adapter.admin.system.statistics;

import com.crm.common.annotation.InterfaceMonitor;
import com.crm.common.annotation.Log;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.enums.BusinessType;
import com.crm.system.domain.dto.MaintenanceWarningReqDTO;
import com.crm.system.service.statistics.MaintenanceWarningService;
import com.crm.system.service.statistics.impl.MaintenanceWarningExportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 维系预警报表 Controller
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/statistics/maintenanceWarning")
public class MaintenanceWarningController {

    @Resource
    private MaintenanceWarningService maintenanceWarningService;

    @Resource
    private MaintenanceWarningExportService exportService;

    /**
     * 查询维系预警报表列表（Tab2 带分页，其他 Tab 不分页）
     */
    @InterfaceMonitor
    @Log(title = "维系预警报表", businessType = BusinessType.EXPORT)
    @PostMapping("/getMaintenanceWarningList")
    public Object getMaintenanceWarningList(@RequestBody MaintenanceWarningReqDTO reqDTO) {
        if (reqDTO.getTabType() != null && reqDTO.getTabType() == 2) {
            return maintenanceWarningService.selectTab2Paged(reqDTO);
        }
        if (reqDTO.getTabType() != null && reqDTO.getTabType() == 6) {
            return maintenanceWarningService.selectTab6Paged(reqDTO);
        }
        return maintenanceWarningService.selectTabList(reqDTO);
    }

    /**
     * 导出报表数据（复杂表头、中文列名、分组颜色）
     */
    @Log(title = "维系预警报表导出", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(@RequestBody MaintenanceWarningReqDTO reqDTO, HttpServletResponse response) throws Exception {
        int tabType = reqDTO.getTabType() != null ? reqDTO.getTabType() : 5;
        List<Map<String, Object>> list = maintenanceWarningService.selectTabExportList(reqDTO);

        String fileName = URLEncoder.encode("维系预警报表.xlsx", StandardCharsets.UTF_8.toString());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        try (Workbook workbook = new XSSFWorkbook()) {
            exportService.export(workbook, tabType, list, reqDTO.getOrderType());
            try (OutputStream os = response.getOutputStream()) {
                workbook.write(os);
            }
        }
    }

    /**
     * 获取预警类型列表
     */
    @PostMapping("/orderTypeList")
    public List<Map<String, Object>> getOrderTypeList() {
        return maintenanceWarningService.getOrderTypeList();
    }

    /**
     * 查询未派发工单清单（对应老项目 maintenanceUnallocatedStat）
     */
    @PostMapping("/getMaintenanceUnallocatedList")
    public TableDataInfo<Map<String, Object>> getMaintenanceUnallocatedList(@RequestBody MaintenanceWarningReqDTO reqDTO) {
        return maintenanceWarningService.selectUnallocatedList(reqDTO);
    }

    /**
     * 导出未派发工单清单（不分页，不脱敏）
     */
    @Log(title = "未派发工单清单导出", businessType = BusinessType.EXPORT)
    @PostMapping("/exportUnallocatedList")
    public void exportUnallocatedList(@RequestBody MaintenanceWarningReqDTO reqDTO, HttpServletResponse response) throws Exception {
        List<Map<String, Object>> list = maintenanceWarningService.exportUnallocatedList(reqDTO);

        String fileName = URLEncoder.encode("维系预警未派发工单清单.xlsx", StandardCharsets.UTF_8.toString());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        try (Workbook workbook = new XSSFWorkbook()) {
            exportService.exportUnallocatedList(workbook, list);
            try (OutputStream os = response.getOutputStream()) {
                workbook.write(os);
            }
        }
    }

    /**
     * 获取客户归属部门列表
     */
    @PostMapping("/getDeptList")
    public List<Map<String, Object>> getDeptList() {
        return maintenanceWarningService.getDeptList();
    }

    /**
     * 获取是否重保客户列表
     */
    @PostMapping("/getIsImpPrtList")
    public List<Map<String, Object>> getIsImpPrtList() {
        return maintenanceWarningService.getIsImpPrtList();
    }

    /**
     * 获取集团类别列表
     */
    @PostMapping("/getGrpTypeList")
    public List<Map<String, Object>> getGrpTypeList() {
        return maintenanceWarningService.getGrpTypeList();
    }
}
