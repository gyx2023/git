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
import com.crm.system.domain.vo.ZqGrpGroupLogVo;
import com.crm.system.domain.bo.ZqGrpGroupLogBo;
import com.crm.system.service.IZqGrpGroupLogService;
import com.crm.common.core.page.TableDataInfo;

/**
 * 政企集团客户信息
 *
 * @author crm
 * @date 2025-11-24
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/grpGroupLog")
public class ZqGrpGroupLogController extends BaseController {

    private final IZqGrpGroupLogService iZqGrpGroupLogService;

    /**
     * 查询政企集团客户信息列表
     */
    @SaCheckPermission("system:grpGroupLog:list")
    @GetMapping("/list")
    public TableDataInfo<ZqGrpGroupLogVo> list(ZqGrpGroupLogBo bo, PageQuery pageQuery) {
        return iZqGrpGroupLogService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出政企集团客户信息列表
     */
    @SaCheckPermission("system:grpGroupLog:export")
    @Log(title = "政企集团客户信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ZqGrpGroupLogBo bo, HttpServletResponse response) {
        List<ZqGrpGroupLogVo> list = iZqGrpGroupLogService.queryList(bo);
        ExcelUtil.exportExcel(list, "政企集团客户信息", ZqGrpGroupLogVo.class, response);
    }

    /**
     * 获取政企集团客户信息详细信息
     *
     * @param grpId 主键
     */
    @SaCheckPermission("system:grpGroupLog:query")
    @GetMapping("/{grpId}")
    public R<ZqGrpGroupLogVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long grpId) {
        return R.ok(iZqGrpGroupLogService.queryById(grpId));
    }

    /**
     * 新增政企集团客户信息
     */
    @SaCheckPermission("system:grpGroupLog:add")
    @Log(title = "政企集团客户信息", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ZqGrpGroupLogBo bo) {
        return toAjax(iZqGrpGroupLogService.insertByBo(bo));
    }

    /**
     * 修改政企集团客户信息
     */
    @SaCheckPermission("system:grpGroupLog:edit")
    @Log(title = "政企集团客户信息", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ZqGrpGroupLogBo bo) {
        return toAjax(iZqGrpGroupLogService.updateByBo(bo));
    }

    /**
     * 删除政企集团客户信息
     *
     * @param grpIds 主键串
     */
    @SaCheckPermission("system:grpGroupLog:remove")
    @Log(title = "政企集团客户信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{grpIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] grpIds) {
        return toAjax(iZqGrpGroupLogService.deleteWithValidByIds(Arrays.asList(grpIds), true));
    }
}
