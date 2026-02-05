package com.crm.web.controller.identify;

import java.util.List;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.crm.system.domain.bo.identify.IdentifyScoreCustomerBo;
import com.crm.system.domain.vo.identify.IdentifyScoreCustomerVo;
import com.crm.system.service.identify.IIdentifyScoreCustomerService;
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
import com.crm.common.core.page.TableDataInfo;

/**
 * 三清单智能评分记录
 *
 * @author crm
 * @date 2026-02-02
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/scoreCustomer")
public class IdentifyScoreCustomerController extends BaseController {

    private final IIdentifyScoreCustomerService iIdentifyScoreCustomerService;

    /**
     * 查询三清单智能评分记录列表
     */
    @SaCheckPermission("system:scoreCustomer:list")
    @GetMapping("/list")
    public TableDataInfo<IdentifyScoreCustomerVo> list(IdentifyScoreCustomerBo bo, PageQuery pageQuery) {
        return iIdentifyScoreCustomerService.queryPageList(bo, pageQuery);
    }



    /**
     * 导出三清单智能评分记录列表
     */
    @SaCheckPermission("system:scoreCustomer:export")
    @Log(title = "三清单智能评分记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(IdentifyScoreCustomerBo bo, HttpServletResponse response) {
        List<IdentifyScoreCustomerVo> list = iIdentifyScoreCustomerService.queryList(bo);
        ExcelUtil.exportExcel(list, "三清单智能评分记录", IdentifyScoreCustomerVo.class, response);
    }

    /**
     * 获取三清单智能评分记录详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:scoreCustomer:query")
    @GetMapping("/{id}")
    public R<IdentifyScoreCustomerVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(iIdentifyScoreCustomerService.queryById(id));
    }

    /**
     * 新增三清单智能评分记录
     */
    @SaCheckPermission("system:scoreCustomer:add")
    @Log(title = "三清单智能评分记录", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody IdentifyScoreCustomerBo bo) {
        return toAjax(iIdentifyScoreCustomerService.insertByBo(bo));
    }

    /**
     * 修改三清单智能评分记录
     */
    @SaCheckPermission("system:scoreCustomer:edit")
    @Log(title = "三清单智能评分记录", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody IdentifyScoreCustomerBo bo) {
        return toAjax(iIdentifyScoreCustomerService.updateByBo(bo));
    }

    /**
     * 删除三清单智能评分记录
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:scoreCustomer:remove")
    @Log(title = "三清单智能评分记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(iIdentifyScoreCustomerService.deleteWithValidByIds(Arrays.asList(ids), true));
    }
}
