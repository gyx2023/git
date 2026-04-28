package com.crm.web.controller.system;

import java.util.List;
import java.util.Arrays;

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
import com.crm.common.enums.BusinessType;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.vo.ZqztOperRecordVo;
import com.crm.system.domain.bo.ZqztOperRecordBo;
import com.crm.system.service.IZqztOperRecordService;
import com.crm.common.core.page.TableDataInfo;

/**
 * 系统操作记录
 *
 * @author crm
 * @date 2026-04-02
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/operRecord")
public class ZqztOperRecordController extends BaseController {

    private final IZqztOperRecordService iZqztOperRecordService;

    /**
     * 查询系统操作记录列表
     */
    @SaCheckPermission("system:operRecord:list")
    @GetMapping("/list")
    public TableDataInfo<ZqztOperRecordVo> list(ZqztOperRecordBo bo, PageQuery pageQuery) {
        return iZqztOperRecordService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出系统操作记录列表
     */
    @SaCheckPermission("system:operRecord:export")
    @Log(title = "系统操作记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ZqztOperRecordBo bo, HttpServletResponse response) {
        List<ZqztOperRecordVo> list = iZqztOperRecordService.queryList(bo);
        ExcelUtil.exportExcel(list, "系统操作记录", ZqztOperRecordVo.class, response);
    }

    /**
     * 获取系统操作记录详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:operRecord:query")
    @GetMapping("/{id}")
    public R<ZqztOperRecordVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(iZqztOperRecordService.queryById(id));
    }

    /**
     * 新增系统操作记录
     */
    @SaCheckPermission("system:operRecord:add")
    @Log(title = "系统操作记录", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ZqztOperRecordBo bo) {
        return toAjax(iZqztOperRecordService.insertByBo(bo));
    }

    /**
     * 修改系统操作记录
     */
    @SaCheckPermission("system:operRecord:edit")
    @Log(title = "系统操作记录", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ZqztOperRecordBo bo) {
        return toAjax(iZqztOperRecordService.updateByBo(bo));
    }

    /**
     * 删除系统操作记录
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:operRecord:remove")
    @Log(title = "系统操作记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(iZqztOperRecordService.deleteWithValidByIds(Arrays.asList(ids), true));
    }
}
