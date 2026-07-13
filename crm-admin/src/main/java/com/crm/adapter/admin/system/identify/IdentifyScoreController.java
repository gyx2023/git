package com.crm.adapter.admin.system.identify;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.crm.common.annotation.Log;
import com.crm.common.annotation.RepeatSubmit;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.R;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.validate.EditGroup;
import com.crm.common.enums.BusinessType;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.bo.identify.IdentifyScoreBo;
import com.crm.system.domain.vo.identify.IdentifyScoreVo;
import com.crm.system.service.identify.IIdentifyScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

/**
 * 【请填写功能名称】
 *
 * @author crm
 * @date 2026-02-02
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/score")
public class IdentifyScoreController extends BaseController {

    private final IIdentifyScoreService iIdentifyScoreService;

    /**
     * 查询【请填写功能名称】列表
     */
    @Log(title = "智能评分", businessType = BusinessType.QUERY)
    @GetMapping("/list")
    public TableDataInfo<IdentifyScoreVo> list(IdentifyScoreBo bo, PageQuery pageQuery) {
        return iIdentifyScoreService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出【请填写功能名称】列表
     */
    @SaCheckPermission("system:score:export")
    @Log(title = "导出", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(IdentifyScoreBo bo, HttpServletResponse response) {
        List<IdentifyScoreVo> list = iIdentifyScoreService.queryList(bo);
        ExcelUtil.exportExcel(list, "导出", IdentifyScoreVo.class, response);
    }

    /**
     * 获取【请填写功能名称】详细信息
     *
     * @param id 主键
     */
    @GetMapping("/{id}")
    public R<IdentifyScoreVo> getInfo(@NotNull(message = "主键不能为空")
                                      @PathVariable Long id) {
        return R.ok(iIdentifyScoreService.queryById(id));
    }

    /**
     * 新增【请填写功能名称】
     */
    @SaCheckPermission("system:score:add")
    @Log(title = "新增评分", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping ("/add")
    public R<Void> add( @RequestParam("ossIds") Long[] ossIds ) {
        return toAjax(iIdentifyScoreService.save(ossIds));
    }

    /**
     * 修改【请填写功能名称】
     */
    @SaCheckPermission("system:score:edit")
    @Log(title = "修改", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody IdentifyScoreBo bo) {
        return toAjax(iIdentifyScoreService.updateByBo(bo));
    }

    /**
     * 删除【请填写功能名称】
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:score:remove")
    @Log(title = "删除", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(iIdentifyScoreService.deleteWithValidByIds(Arrays.asList(ids), true));
    }


    /**
     * 重新评分
     */
    @SaCheckPermission("system:score:again")
    @Log(title = "新文件重新评分", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/again")
    public R<Void> again(@RequestParam Long customerId) {
        return toAjax(iIdentifyScoreService.again(customerId));
    }
}
