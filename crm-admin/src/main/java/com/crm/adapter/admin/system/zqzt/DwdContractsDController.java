package com.crm.adapter.admin.system.zqzt;

import com.crm.common.annotation.Log;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.R;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.enums.BusinessType;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.bo.DwdContractsDBo;
import com.crm.system.domain.vo.ContractEformRespVo;
import com.crm.system.domain.vo.DwdContractsDVo;
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
    @Log(title = "DWD_CONTRACTS_D", businessType = BusinessType.OTHER)
    @GetMapping("/list")
    public TableDataInfo<DwdContractsDVo> list(DwdContractsDBo bo, PageQuery pageQuery) {
        return dwdContractsDService.selectDwdContractsDPageList(bo, pageQuery);
    }

    /**
     * 根据合同编号获取DWD_CONTRACTS_D详细信息
     */
    @GetMapping(value = "/{arrairNo}")
    public R<DwdContractsDVo> getInfo(@PathVariable String arrairNo) {
        // 通过流水号查询数据
        DwdContractsDVo dwdContractsDDoVo = dwdContractsDService.selectDwdContractsDById(arrairNo);
        return R.ok(dwdContractsDDoVo);
    }

    /**
     * 导出DWD_CONTRACTS_D列表
     */
    @Log(title = "DWD_CONTRACTS_D", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(DwdContractsDBo bo, HttpServletResponse response) {
        List<DwdContractsDVo> list = dwdContractsDService.selectDwdContractsDList(bo);
        ExcelUtil.exportExcel(list, "DWD_CONTRACTS_D数据", DwdContractsDVo.class, response);
    }

    /**
     * 查询电子表单接口信息
     */
    @GetMapping(value = "/getEformInfoByNo/{arrairNo}")
    public R<ContractEformRespVo> getEformInfoByNo(@PathVariable String arrairNo) {
        return R.ok(dwdContractsDService.getEformInfoByNo(arrairNo));
    }
}
