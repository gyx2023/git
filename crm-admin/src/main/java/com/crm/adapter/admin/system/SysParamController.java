package com.crm.adapter.admin.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.crm.common.annotation.Log;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.R;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.enums.BusinessType;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.dao.SysParamDo;
import com.crm.system.service.sys.ISysParamService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 配置管理 信息操作处理
 *
 * @author ian
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/param")
public class SysParamController extends BaseController {

    private final ISysParamService paramService;

    /**
     * 获取配置列表
     */
    @Log(title = "配置管理", businessType = BusinessType.QUERY)
    @SaCheckPermission("system:param:list")
    @GetMapping("/list")
    public TableDataInfo<SysParamDo> list(SysParamDo param, PageQuery pageQuery) {
        return paramService.selectPageParamList(param, pageQuery);
    }

    /**
     * 导出配置列表
     */
    @Log(title = "配置管理", businessType = BusinessType.EXPORT)
    @SaCheckPermission("system:param:export")
    @PostMapping("/export")
    public void export(SysParamDo param, HttpServletResponse response) {
        List<SysParamDo> list = paramService.selectParamList(param);
        ExcelUtil.exportExcel(list, "配置数据", SysParamDo.class, response);
    }

    /**
     * 根据配置编号获取详细信息
     *
     * @param id 配置ID
     */
    @SaCheckPermission("system:param:query")
    @GetMapping(value = "/{id}")
    public R<SysParamDo> getInfo(@PathVariable("id") Long id) {
        return R.ok(paramService.selectParamById(id));
    }

    /**
     * 新增配置
     */
    @SaCheckPermission("system:param:add")
    @Log(title = "配置管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysParamDo param) {
        paramService.insertParam(param);
        return R.ok();
    }

    /**
     * 修改配置
     */
    @SaCheckPermission("system:param:edit")
    @Log(title = "配置管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysParamDo param) {
        paramService.updateParam(param);
        return R.ok();
    }

    /**
     * 删除配置
     *
     * @param ids 配置ID串
     */
    @SaCheckPermission("system:param:remove")
    @Log(title = "配置管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable("ids") Long[] ids) {
        paramService.deleteParamByIds(ids);
        return R.ok();
    }

    /**
     * 刷新配置缓存
     */
    @SaCheckPermission("system:param:remove")
    @Log(title = "配置管理", businessType = BusinessType.CLEAN)
    @DeleteMapping("/refreshCache")
    public R<Void> refreshCache() {
        paramService.resetParamCache();
        return R.ok();
    }
}
