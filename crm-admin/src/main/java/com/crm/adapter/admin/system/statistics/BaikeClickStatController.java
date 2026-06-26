package com.crm.adapter.admin.system.statistics;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.crm.common.annotation.Log;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.enums.BusinessType;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.statistics.domain.dto.BaikeClickStatDTO;
import com.crm.statistics.domain.vo.BaikeClickStatVO;
import com.crm.statistics.service.IBaikeClickStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 百科点击量统计Controller
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/statistics/baikeClickStat")
public class BaikeClickStatController extends BaseController {

    private final IBaikeClickStatService baikeClickStatService;

    @GetMapping("/list")
    @SaCheckPermission("statistics:baikeClickStat:list")
    @Log(title = "百科点击量统计")
    public TableDataInfo<BaikeClickStatVO> list(BaikeClickStatDTO dto) {
        return baikeClickStatService.queryList(dto);
    }

    @Log(title = "百科点击量统计导出", businessType = BusinessType.EXPORT)
    @SaCheckPermission("statistics:baikeClickStat:export")
    @PostMapping("/export")
    public void export(BaikeClickStatDTO dto, HttpServletResponse response) {
        List<BaikeClickStatVO> list = baikeClickStatService.exportList(dto);
        ExcelUtil.exportExcel(list, "政企百科点击量统计" + DateUtils.dateTime(), BaikeClickStatVO.class, response);

    }
}
