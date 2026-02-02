package com.crm.web.controller.identify;

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
    @SaCheckPermission("system:score:list")
    @GetMapping("/list")
    public TableDataInfo<IdentifyScoreVo> list(IdentifyScoreBo bo, PageQuery pageQuery) {
        return iIdentifyScoreService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出【请填写功能名称】列表
     */
    @SaCheckPermission("system:score:export")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(IdentifyScoreBo bo, HttpServletResponse response) {
        List<IdentifyScoreVo> list = iIdentifyScoreService.queryList(bo);
        ExcelUtil.exportExcel(list, "【请填写功能名称】", IdentifyScoreVo.class, response);
    }

    /**
     * 获取【请填写功能名称】详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:score:query")
    @GetMapping("/{id}")
    public R<IdentifyScoreVo> getInfo(@NotNull(message = "主键不能为空")
                                      @PathVariable Long id) {
        return R.ok(iIdentifyScoreService.queryById(id));
    }

    /**
     * 新增【请填写功能名称】
     */
    @SaCheckPermission("system:score:add")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody IdentifyScoreBo bo) {
        return toAjax(iIdentifyScoreService.insertByBo(bo));
    }

    /**
     * 修改【请填写功能名称】
     */
    @SaCheckPermission("system:score:edit")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.UPDATE)
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
    @Log(title = "【请填写功能名称】", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(iIdentifyScoreService.deleteWithValidByIds(Arrays.asList(ids), true));
    }
}
