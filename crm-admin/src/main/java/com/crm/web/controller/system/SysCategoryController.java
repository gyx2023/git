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
import com.crm.system.domain.vo.SysCategoryVo;
import com.crm.system.domain.bo.SysCategoryBo;
import com.crm.system.service.ISysCategoryService;
import com.crm.common.core.page.TableDataInfo;

/**
 * 行业大类-亚类-细类分类
 *
 * @author crm
 * @date 2025-11-24
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/category")
public class SysCategoryController extends BaseController {

    private final ISysCategoryService iSysCategoryService;

    /**
     * 查询行业大类-亚类-细类分类列表
     */
    @SaCheckPermission("system:category:list")
    @GetMapping("/list")
    public TableDataInfo<SysCategoryVo> list(SysCategoryBo bo, PageQuery pageQuery) {
        return iSysCategoryService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询所有行业大类列表
     */
    @GetMapping("/majorCategories")
    public R<List<SysCategoryVo>> listMajorCategories() {
        List<SysCategoryVo> list = iSysCategoryService.selectMajorCategories();
        return R.ok(list);
    }

    /**
     * 根据父级ID查询行业亚类列表
     */
    @GetMapping("/subCategories")
    public R<List<SysCategoryVo>> listSubCategoriesByParentId(@RequestParam("parentId") String parentId) {
        System.out.println("Received request for subCategories with parentId: " + parentId);
        List<SysCategoryVo> list = iSysCategoryService.selectSubCategoriesByParentId(parentId);
        System.out.println("Returning " + (list != null ? list.size() : 0) + " sub categories");
        return R.ok(list);
    }

    /**
     * 导出行业大类-亚类-细类分类列表
     */
    @SaCheckPermission("system:category:export")
    @Log(title = "行业大类-亚类-细类分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SysCategoryBo bo, HttpServletResponse response) {
        List<SysCategoryVo> list = iSysCategoryService.queryList(bo);
        ExcelUtil.exportExcel(list, "行业大类-亚类-细类分类", SysCategoryVo.class, response);
    }

    /**
     * 获取行业大类-亚类-细类分类详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:category:query")
    @GetMapping("/{id}")
    public R<SysCategoryVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable String id) {
        return R.ok(iSysCategoryService.queryById(id));
    }

    /**
     * 新增行业大类-亚类-细类分类
     */
    @SaCheckPermission("system:category:add")
    @Log(title = "行业大类-亚类-细类分类", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SysCategoryBo bo) {
        return toAjax(iSysCategoryService.insertByBo(bo));
    }

    /**
     * 修改行业大类-亚类-细类分类
     */
    @SaCheckPermission("system:category:edit")
    @Log(title = "行业大类-亚类-细类分类", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SysCategoryBo bo) {
        return toAjax(iSysCategoryService.updateByBo(bo));
    }

    /**
     * 删除行业大类-亚类-细类分类
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:category:remove")
    @Log(title = "行业大类-亚类-细类分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable String[] ids) {
        return toAjax(iSysCategoryService.deleteWithValidByIds(Arrays.asList(ids), true));
    }
}
