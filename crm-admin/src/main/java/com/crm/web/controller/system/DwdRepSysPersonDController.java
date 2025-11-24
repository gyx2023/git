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
import com.crm.system.domain.vo.DwdRepSysPersonDVo;
import com.crm.system.domain.bo.DwdRepSysPersonDBo;
import com.crm.system.service.IDwdRepSysPersonDService;
import com.crm.common.core.page.TableDataInfo;

/**
 * 【请填写功能名称】
 *
 * @author crm
 * @date 2025-11-24
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/repSysPersonD")
public class DwdRepSysPersonDController extends BaseController {

    private final IDwdRepSysPersonDService iDwdRepSysPersonDService;

    /**
     * 查询【请填写功能名称】列表
     */
    @SaCheckPermission("system:repSysPersonD:list")
    @GetMapping("/list")
    public TableDataInfo<DwdRepSysPersonDVo> list(DwdRepSysPersonDBo bo, PageQuery pageQuery) {
        return iDwdRepSysPersonDService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出【请填写功能名称】列表
     */
    @SaCheckPermission("system:repSysPersonD:export")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(DwdRepSysPersonDBo bo, HttpServletResponse response) {
        List<DwdRepSysPersonDVo> list = iDwdRepSysPersonDService.queryList(bo);
        ExcelUtil.exportExcel(list, "【请填写功能名称】", DwdRepSysPersonDVo.class, response);
    }

    /**
     * 获取【请填写功能名称】详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:repSysPersonD:query")
    @GetMapping("/{id}")
    public R<DwdRepSysPersonDVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(iDwdRepSysPersonDService.queryById(id));
    }

    /**
     * 新增【请填写功能名称】
     */
    @SaCheckPermission("system:repSysPersonD:add")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody DwdRepSysPersonDBo bo) {
        return toAjax(iDwdRepSysPersonDService.insertByBo(bo));
    }

    /**
     * 修改【请填写功能名称】
     */
    @SaCheckPermission("system:repSysPersonD:edit")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody DwdRepSysPersonDBo bo) {
        return toAjax(iDwdRepSysPersonDService.updateByBo(bo));
    }

    /**
     * 删除【请填写功能名称】
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:repSysPersonD:remove")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(iDwdRepSysPersonDService.deleteWithValidByIds(Arrays.asList(ids), true));
    }
}
