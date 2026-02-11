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
import com.crm.system.domain.vo.GrpCustInuseVo;
import com.crm.system.domain.bo.GrpCustInuseBo;
import com.crm.system.service.IGrpCustInuseService;
import com.crm.common.core.page.TableDataInfo;

/**
 * 政企实际使用客户类型
 *
 * @author crm
 * @date 2026-01-20
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/custInuse")
public class GrpCustInuseController extends BaseController {

    private final IGrpCustInuseService iGrpCustInuseService;

    /**
     * 查询政企实际使用客户类型列表
     */
    @SaCheckPermission("system:custInuse:list")
    @GetMapping("/list")
    public TableDataInfo<GrpCustInuseVo> list(GrpCustInuseBo bo, PageQuery pageQuery) {
        return iGrpCustInuseService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出政企实际使用客户类型列表
     */
    @SaCheckPermission("system:custInuse:export")
    @Log(title = "政企实际使用客户类型", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(GrpCustInuseBo bo, HttpServletResponse response) {
        List<GrpCustInuseVo> list = iGrpCustInuseService.queryList(bo);
        ExcelUtil.exportExcel(list, "政企实际使用客户类型", GrpCustInuseVo.class, response);
    }

    /**
     * 获取政企实际使用客户类型详细信息
     *
     * @param inuseId 主键
     */
    @SaCheckPermission("system:custInuse:query")
    @GetMapping("/{inuseId}")
    public R<GrpCustInuseVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable String inuseId) {
        return R.ok(iGrpCustInuseService.queryById(inuseId));
    }

    /**
     * 新增政企实际使用客户类型
     */
    @SaCheckPermission("system:custInuse:add")
    @Log(title = "政企实际使用客户类型", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody GrpCustInuseBo bo) {
        return toAjax(iGrpCustInuseService.insertByBo(bo));
    }

    /**
     * 修改政企实际使用客户类型
     */
    @SaCheckPermission("system:custInuse:edit")
    @Log(title = "政企实际使用客户类型", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody GrpCustInuseBo bo) {
        return toAjax(iGrpCustInuseService.updateByBo(bo));
    }

    /**
     * 删除政企实际使用客户类型
     *
     * @param inuseIds 主键串
     */
    @SaCheckPermission("system:custInuse:remove")
    @Log(title = "政企实际使用客户类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{inuseIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable String[] inuseIds) {
        return toAjax(iGrpCustInuseService.deleteWithValidByIds(Arrays.asList(inuseIds), true));
    }
}
