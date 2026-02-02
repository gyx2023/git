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
import com.crm.system.domain.vo.GrpCustInuseUserlistVo;
import com.crm.system.domain.bo.GrpCustInuseUserlistBo;
import com.crm.system.service.IGrpCustInuseUserlistService;
import com.crm.common.core.page.TableDataInfo;

/**
 * 政企实际使用客户类型用户清单
 *
 * @author crm
 * @date 2026-01-20
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/custInuseUserlist")
public class GrpCustInuseUserlistController extends BaseController {

    private final IGrpCustInuseUserlistService iGrpCustInuseUserlistService;

    /**
     * 查询政企实际使用客户类型用户清单列表
     */
    @SaCheckPermission("system:custInuseUserlist:list")
    @GetMapping("/list")
    public TableDataInfo<GrpCustInuseUserlistVo> list(GrpCustInuseUserlistBo bo, PageQuery pageQuery) {
        return iGrpCustInuseUserlistService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出政企实际使用客户类型用户清单列表
     */
    @SaCheckPermission("system:custInuseUserlist:export")
    @Log(title = "政企实际使用客户类型用户清单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(GrpCustInuseUserlistBo bo, HttpServletResponse response) {
        List<GrpCustInuseUserlistVo> list = iGrpCustInuseUserlistService.queryList(bo);
        ExcelUtil.exportExcel(list, "政企实际使用客户类型用户清单", GrpCustInuseUserlistVo.class, response);
    }

    /**
     * 获取政企实际使用客户类型用户清单详细信息
     *
     * @param inuseId 主键
     */
    @SaCheckPermission("system:custInuseUserlist:query")
    @GetMapping("/{inuseId}")
    public R<GrpCustInuseUserlistVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable String inuseId) {
        return R.ok(iGrpCustInuseUserlistService.queryById(inuseId));
    }

    /**
     * 新增政企实际使用客户类型用户清单
     */
    @SaCheckPermission("system:custInuseUserlist:add")
    @Log(title = "政企实际使用客户类型用户清单", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody GrpCustInuseUserlistBo bo) {
        return toAjax(iGrpCustInuseUserlistService.insertByBo(bo));
    }

    /**
     * 修改政企实际使用客户类型用户清单
     */
    @SaCheckPermission("system:custInuseUserlist:edit")
    @Log(title = "政企实际使用客户类型用户清单", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody GrpCustInuseUserlistBo bo) {
        return toAjax(iGrpCustInuseUserlistService.updateByBo(bo));
    }

    /**
     * 删除政企实际使用客户类型用户清单
     *
     * @param inuseIds 主键串
     */
    @SaCheckPermission("system:custInuseUserlist:remove")
    @Log(title = "政企实际使用客户类型用户清单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{inuseIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable String[] inuseIds) {
        return toAjax(iGrpCustInuseUserlistService.deleteWithValidByIds(Arrays.asList(inuseIds), true));
    }
}
