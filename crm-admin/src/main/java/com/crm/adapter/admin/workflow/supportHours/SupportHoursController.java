package com.crm.adapter.admin.workflow.supportHours;

import com.alibaba.excel.EasyExcel;
import com.crm.common.annotation.Log;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.enums.BusinessType;
import com.crm.system.domain.vo.SysCodeVo;
import com.crm.system.service.sys.ISysCodeService;
import com.crm.system.domain.dto.SupportHoursQueryDTO;
import com.crm.system.domain.vo.SupportHoursVO;
import com.crm.system.service.ISupportHoursService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 支撑工时统计Controller
 *
 * @author crm
 */
@Validated
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/workflow/supportHours")
public class SupportHoursController extends BaseController {

    private final ISupportHoursService supportHoursService;
    private final ISysCodeService sysCodeService;

    /**
     * 查询支撑工时统计数据
     */

    @GetMapping("/list")
    @Log(title = "支撑工时统计", businessType = BusinessType.QUERY)
    public TableDataInfo<SupportHoursVO> list(SupportHoursQueryDTO queryDTO) {
        return supportHoursService.querySupportHoursList(queryDTO);
    }

    /**
     * 导出支撑工时统计数据
     */
    @Log(title = "支撑工时统计", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SupportHoursQueryDTO queryDTO) throws IOException {
        List<SupportHoursVO> list = supportHoursService.exportSupportHoursList(queryDTO);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        String fileName = URLEncoder.encode("商机支撑人日统计", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");

        // 生成动态表头
        List<List<String>> headList = supportHoursService.generateExportHeaders(queryDTO);

        // 转换数据为二维数组，与表头对应
        List<List<Object>> dataList = convertToExportData(list, queryDTO);

        EasyExcel.write(response.getOutputStream())
                .head(headList)
                .sheet("商机支撑人日统计")
                .doWrite(dataList);
    }

    /**
     * 转换数据为导出格式
     */
    private List<List<Object>> convertToExportData(List<SupportHoursVO> list, SupportHoursQueryDTO queryDTO) {
        List<List<Object>> dataList = new ArrayList<>();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date leftDate = sdf.parse(queryDTO.getLeftTime());
            Date rightDate = sdf.parse(queryDTO.getRightTime());

            // 获取年月列表和周周期列表
            List<String> yearMonths = supportHoursService.getYearMonthsBetween(leftDate, rightDate);
            List<String> weekCycles = queryDTO.getOptions() != null && queryDTO.getOptions().contains("week")
                    ? supportHoursService.getWeeklyCycles(leftDate, rightDate)
                    : new ArrayList<>();

            for (SupportHoursVO vo : list) {
                List<Object> row = new ArrayList<>();

                // 基础字段
                row.add(vo.getOppCode());                    // 商机编号
                row.add(vo.getOppName());                    // 商机名称
                row.add(vo.getCustName());                   // 客户名称
                row.add(vo.getXpectContractMoney());         // 预期合同(万元)

                // 客户行业：编码转名称
                String unicomTypeName = "";
                if (vo.getUnicomType() != null && !vo.getUnicomType().isEmpty()) {
                    SysCodeVo sysCodeVo = sysCodeService.findCode("unicom_type", vo.getUnicomType());
                    if (sysCodeVo != null && sysCodeVo.getName() != null) {
                        unicomTypeName = sysCodeVo.getName();
                    } else {
                        unicomTypeName = vo.getUnicomType(); // 如果找不到，使用原编码
                    }
                }
                row.add(unicomTypeName);                     // 客户行业

                row.add(vo.getCustManagerName());            // 客户经理
                row.add(vo.getFirstManagetName());           // 主责支撑经理
                row.add(vo.getSecondManagetName());          // 非主责支撑经理
                row.add(vo.getTaskName());                   // 支撑阶段
                row.add(vo.getTotalHours() != null ? vo.getTotalHours() : 0.0); // 合计

                // 月份数据
                for (String month : yearMonths) {
                    Double hours = vo.getMonthHour() != null ? vo.getMonthHour().get(month) : null;
                    row.add(hours != null ? hours : 0.0);
                }

                // 周数据
                if (queryDTO.getOptions() != null && queryDTO.getOptions().contains("week")) {
                    for (String cycle : weekCycles) {
                        Double hours = vo.getWeeklyHours() != null ? vo.getWeeklyHours().get(cycle) : null;
                        row.add(hours != null ? hours : 0.0);
                    }
                }

                // 商机到达时间
                row.add(vo.getOppTime());

                // 支撑情况（暂时为空）
                row.add("");

                dataList.add(row);
            }
        } catch (Exception e) {
            log.error("转换导出数据失败", e);
        }

        return dataList;
    }

    /**
     * 获取年月列表
     */
    @GetMapping("/getYearMonths")
    public R<List<String>> getYearMonths(@RequestParam String leftTime, @RequestParam String rightTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date leftDate = sdf.parse(leftTime);
            Date rightDate = sdf.parse(rightTime);
            List<String> yearMonths = supportHoursService.getYearMonthsBetween(leftDate, rightDate);
            return R.ok(yearMonths);
        } catch (Exception e) {
            log.error("获取年月列表失败", e);
            return R.fail("获取年月列表失败");
        }
    }

    /**
     * 获取周周期列表
     */
    @GetMapping("/getWeeklyCycles")
    public R<List<String>> getWeeklyCycles(@RequestParam String leftTime, @RequestParam String rightTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date leftDate = sdf.parse(leftTime);
            Date rightDate = sdf.parse(rightTime);
            List<String> cycles = supportHoursService.getWeeklyCycles(leftDate, rightDate);
            return R.ok(cycles);
        } catch (Exception e) {
            log.error("获取周周期列表失败", e);
            return R.fail("获取周周期列表失败");
        }
    }
}
