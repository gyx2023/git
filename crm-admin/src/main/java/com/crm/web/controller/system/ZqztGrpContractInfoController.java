package com.crm.web.controller.system;

import java.util.List;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import com.crm.common.annotation.RepeatSubmit;
import com.crm.common.annotation.Log;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.R;
import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import com.crm.common.core.validate.QueryGroup;
import com.crm.common.enums.BusinessType;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.vo.ZqztGrpContractInfoVo;
import com.crm.system.domain.bo.ZqztGrpContractInfoBo;
import com.crm.system.service.IZqztGrpContractInfoService;
import com.crm.common.core.page.TableDataInfo;

/**
 * 集团合同信息
 *
 * @author 万超凡
 * @date 2026-01-15
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/grpContractInfo")
public class ZqztGrpContractInfoController extends BaseController {

    private final IZqztGrpContractInfoService iZqztGrpContractInfoService;

    /**
     * 查询集团合同信息列表
     */
    @SaCheckPermission("system:grpContractInfo:list")
    @GetMapping("/list")
    public TableDataInfo<ZqztGrpContractInfoVo> list(ZqztGrpContractInfoBo bo, PageQuery pageQuery) {
        return iZqztGrpContractInfoService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出集团合同信息列表
     */
    @SaCheckPermission("system:grpContractInfo:export")
    @Log(title = "集团合同信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ZqztGrpContractInfoBo bo, HttpServletResponse response) {
        List<ZqztGrpContractInfoVo> list = iZqztGrpContractInfoService.queryList(bo);
        ExcelUtil.exportExcel(list, "集团合同信息", ZqztGrpContractInfoVo.class, response);
    }

    /**
     * 获取集团合同信息详细信息
     *
     * @param contractId 主键
     */
    @SaCheckPermission("system:grpContractInfo:query")
    @GetMapping("/{contractId}")
    public R<ZqztGrpContractInfoVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable String contractId) {
        return R.ok(iZqztGrpContractInfoService.queryById(contractId));
    }

    /**
     * 新增集团合同信息
     */
    @SaCheckPermission("system:grpContractInfo:add")
    @Log(title = "集团合同信息", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ZqztGrpContractInfoBo bo) {
        return toAjax(iZqztGrpContractInfoService.insertByBo(bo));
    }

    /**
     * 修改集团合同信息
     */
    @SaCheckPermission("system:grpContractInfo:edit")
    @Log(title = "集团合同信息", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ZqztGrpContractInfoBo bo) {
        return toAjax(iZqztGrpContractInfoService.updateByBo(bo));
    }

    /**
     * 删除集团合同信息
     *
     * @param contractIds 主键串
     */
    @SaCheckPermission("system:grpContractInfo:remove")
    @Log(title = "集团合同信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{contractIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable String[] contractIds) {
        return toAjax(iZqztGrpContractInfoService.deleteWithValidByIds(Arrays.asList(contractIds), true));
    }
}
