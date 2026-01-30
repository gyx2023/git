package com.crm.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.crm.common.annotation.Log;
import com.crm.common.annotation.RepeatSubmit;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.R;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import com.crm.common.enums.BusinessType;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.bo.ZqztGrpContractOtherInfoLogBo;
import com.crm.system.domain.vo.ZqztGrpContractOtherInfoLogVo;
import com.crm.system.service.IZqztGrpContractOtherInfoLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

/**
 * 集团合同扩展信息日志
 *
 * @author 万超凡
 * @date 2026-01-15
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/grpContractOtherInfoLog")
public class ZqztGrpContractOtherInfoLogController extends BaseController {

    private final IZqztGrpContractOtherInfoLogService iZqztGrpContractOtherInfoLogService;

    /**
     * 查询集团合同扩展信息日志列表
     */
    @SaCheckPermission("system:grpContractOtherInfoLog:list")
    @GetMapping("/list")
    public TableDataInfo<ZqztGrpContractOtherInfoLogVo> list(ZqztGrpContractOtherInfoLogBo bo, PageQuery pageQuery) {
        return iZqztGrpContractOtherInfoLogService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出集团合同扩展信息日志列表
     */
    @SaCheckPermission("system:grpContractOtherInfoLog:export")
    @Log(title = "集团合同扩展信息日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ZqztGrpContractOtherInfoLogBo bo, HttpServletResponse response) {
        List<ZqztGrpContractOtherInfoLogVo> list = iZqztGrpContractOtherInfoLogService.queryList(bo);
        ExcelUtil.exportExcel(list, "集团合同扩展信息日志", ZqztGrpContractOtherInfoLogVo.class, response);
    }

    /**
     * 获取集团合同扩展信息日志详细信息
     *
     * @param contractId 主键
     */
    @SaCheckPermission("system:grpContractOtherInfoLog:query")
    @GetMapping("/{contractId}")
    public R<ZqztGrpContractOtherInfoLogVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable String contractId) {
        return R.ok(iZqztGrpContractOtherInfoLogService.queryById(contractId));
    }

    /**
     * 新增集团合同扩展信息日志
     */
    @SaCheckPermission("system:grpContractOtherInfoLog:add")
    @Log(title = "集团合同扩展信息日志", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ZqztGrpContractOtherInfoLogBo bo) {
        return toAjax(iZqztGrpContractOtherInfoLogService.insertByBo(bo));
    }

    /**
     * 修改集团合同扩展信息日志
     */
    @SaCheckPermission("system:grpContractOtherInfoLog:edit")
    @Log(title = "集团合同扩展信息日志", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ZqztGrpContractOtherInfoLogBo bo) {
        return toAjax(iZqztGrpContractOtherInfoLogService.updateByBo(bo));
    }

    /**
     * 删除集团合同扩展信息日志
     *
     * @param contractIds 主键串
     */
    @SaCheckPermission("system:grpContractOtherInfoLog:remove")
    @Log(title = "集团合同扩展信息日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{contractIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable String[] contractIds) {
        return toAjax(iZqztGrpContractOtherInfoLogService.deleteWithValidByIds(Arrays.asList(contractIds), true));
    }
}
