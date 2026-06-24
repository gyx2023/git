package com.crm.adapter.admin.system.statistics;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.excel.EasyExcel;
import com.crm.common.annotation.Log;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.enums.BusinessType;
import com.crm.system.domain.dto.SupportCityStatsDTO;
import com.crm.system.domain.vo.SupportCityDetailVO;
import com.crm.system.domain.vo.SupportCityStatsVO;
import com.crm.system.service.statistics.ISupportCityStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 支撑情况地市统计Controller
 *
 * @author crm
 */
@Validated
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/statistics/supportCity")
public class SupportCityStatsController extends BaseController {

    private final ISupportCityStatsService supportCityStatsService;

    /**
     * 查询支撑情况地市统计列表
     */
    @GetMapping("/list")
    public TableDataInfo<SupportCityStatsVO> list(SupportCityStatsDTO dto) {
        List<SupportCityStatsVO> list = supportCityStatsService.selectSupportCityStatsList(dto);
        return TableDataInfo.build(list);
    }

    /**
     * 查询支撑工单明细
     */
    @GetMapping("/detail")
    public TableDataInfo<SupportCityDetailVO> detail(@RequestParam("areaId") String areaId,
                                                      @RequestParam(value = "startDate", required = false) String startDate,
                                                      @RequestParam(value = "endDate", required = false) String endDate) {
        List<SupportCityDetailVO> list = supportCityStatsService.selectSupportCityDetailList(areaId, startDate, endDate);
        return TableDataInfo.build(list);
    }

    /**
     * 导出支撑情况地市统计
     */
    @Log(title = "支撑情况地市统计", businessType = BusinessType.EXPORT)
    @SaCheckPermission("statistics:supportCity:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SupportCityStatsDTO dto) throws IOException {
        List<SupportCityStatsVO> list = supportCityStatsService.exportSupportCityStatsList(dto);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = URLEncoder.encode("支撑情况地市统计" + dateStr, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), SupportCityStatsVO.class)
                .sheet("支撑情况地市统计")
                .doWrite(list);
    }

}
