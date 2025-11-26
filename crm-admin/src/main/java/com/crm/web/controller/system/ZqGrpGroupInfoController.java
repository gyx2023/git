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
import com.crm.system.domain.vo.ZqGrpGroupInfoVo;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.service.IZqGrpGroupInfoService;
import com.crm.common.core.page.TableDataInfo;

/**
 * 政企集团客户信息
 *
 * @author crm
 * @date 2025-11-20
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/grpGroupInfo")
public class ZqGrpGroupInfoController extends BaseController {

    private final IZqGrpGroupInfoService iZqGrpGroupInfoService;

    /**
     * 查询政企集团客户信息列表
     */
    @SaCheckPermission("system:grpGroupInfo:list")
    @GetMapping("/list")
    public TableDataInfo<ZqGrpGroupInfoVo> list(ZqGrpGroupInfoBo bo, PageQuery pageQuery,
                                                @RequestParam(value = "grpCreateTimeBegin", required = false) String beginTime,
                                                @RequestParam(value = "grpCreateTimeEnd", required = false) String endTime) {
        return iZqGrpGroupInfoService.queryPageList(bo, pageQuery, beginTime, endTime);
    }

    /**
     * 导出政企集团客户信息列表
     */
    @SaCheckPermission("system:grpGroupInfo:export")
    @Log(title = "政企集团客户信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ZqGrpGroupInfoBo bo, HttpServletResponse response) {
        List<ZqGrpGroupInfoVo> list = iZqGrpGroupInfoService.queryList(bo);
        ExcelUtil.exportExcel(list, "政企集团客户信息", ZqGrpGroupInfoVo.class, response);
    }

    /**
     * 获取政企集团客户信息详细信息
     *
     * @param grpId 主键
     */
    @SaCheckPermission("system:grpGroupInfo:query")
    @GetMapping("/{grpId}")
    public R<ZqGrpGroupInfoVo> getInfo(@NotNull(message = "主键不能为空")
                                       @PathVariable Long grpId) {
        return R.ok(iZqGrpGroupInfoService.queryById(grpId));
    }

    /**
     * 新增政企集团客户信息
     */
    @SaCheckPermission("system:grpGroupInfo:add")
    @Log(title = "政企集团客户信息", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ZqGrpGroupInfoBo bo) {
        return toAjax(iZqGrpGroupInfoService.insertByBo(bo));
    }

    /**
     * 修改政企集团客户信息
     */
    @SaCheckPermission("system:grpGroupInfo:edit")
    @Log(title = "政企集团客户信息", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ZqGrpGroupInfoBo bo) {
        return toAjax(iZqGrpGroupInfoService.updateByBo(bo));
    }

    /**
     * 删除政企集团客户信息
     *
     * @param grpIds 主键串
     */
    @SaCheckPermission("system:grpGroupInfo:remove")
    @Log(title = "政企集团客户信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{grpIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] grpIds) {
        return toAjax(iZqGrpGroupInfoService.deleteWithValidByIds(Arrays.asList(grpIds), true));
    }
}
