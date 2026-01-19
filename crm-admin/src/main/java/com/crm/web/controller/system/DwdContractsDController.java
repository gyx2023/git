package com.crm.web.controller.system;

import com.crm.common.annotation.Log;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.R;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.enums.BusinessType;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.bo.DwdContractsDBo;
import com.crm.system.domain.vo.DwdContractsDDoVo;
import com.crm.system.service.IDwdContractsDService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * DWD_CONTRACTS_D Controller
 *
 * @author 万超凡
 * @date 2026-01-19
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/dwdContractsD")
public class DwdContractsDController extends BaseController {

    private final IDwdContractsDService dwdContractsDService;

    /**
     * 查询DWD_CONTRACTS_D列表
     */
    @GetMapping("/list")
    public TableDataInfo<DwdContractsDDoVo> list(DwdContractsDBo bo, PageQuery pageQuery) {
        return dwdContractsDService.selectDwdContractsDPageList(bo, pageQuery);
    }

    /**
     * 获取DWD_CONTRACTS_D详细信息
     */
    @GetMapping(value = "/{id}")
    public R<DwdContractsDDoVo> getInfo(@PathVariable String id) {
        // 注意：由于DWD_CONTRACTS_D表没有主键，这里使用业务流水号作为唯一标识
        DwdContractsDDoVo dwdContractsDDoVo = dwdContractsDService.selectDwdContractsDById(id);
        return R.ok(dwdContractsDDoVo);
    }

    /**
     * 导出DWD_CONTRACTS_D列表
     */
    @Log(title = "DWD_CONTRACTS_D", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(DwdContractsDBo bo, HttpServletResponse response) {
        List<DwdContractsDDoVo> list = dwdContractsDService.selectDwdContractsDList(bo);
        ExcelUtil.exportExcel(list, "DWD_CONTRACTS_D数据", DwdContractsDDoVo.class, response);
    }
}
