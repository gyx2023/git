package com.crm.adapter.admin.system;

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
import com.crm.system.domain.bo.SysAreaBo;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.service.ISysAreaService;
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
 * @date 2025-11-24
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/area")
public class SysAreaController extends BaseController {

    private final ISysAreaService iSysAreaService;

    /**
     * 查询浙江省地级市列表
     */
    @GetMapping("/zhejiangCities")
    public R<List<SysAreaVo>> zhejiangCities() {
        List<SysAreaVo> cities = iSysAreaService.queryZhejiangCities();
        return R.ok(cities);
    }

    /**
     * 根据地级市ID查询其下辖的区县列表
     */
    @GetMapping("/county/list")
    public R<List<SysAreaVo>> countyList(@RequestParam String areaId) {
        List<SysAreaVo> counties = iSysAreaService.queryCountyListByCityId(areaId);
        return R.ok(counties);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @GetMapping("/list")
    public TableDataInfo<SysAreaVo> list(SysAreaBo bo, PageQuery pageQuery) {
        return iSysAreaService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出【请填写功能名称】列表
     */
    @Log(title = "【请填写功能名称】", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SysAreaBo bo, HttpServletResponse response) {
        List<SysAreaVo> list = iSysAreaService.queryList(bo);
        ExcelUtil.exportExcel(list, "【请填写功能名称】", SysAreaVo.class, response);
    }

    /**
     * 获取【请填写功能名称】详细信息
     *
     * @param id 主键
     */
    @GetMapping("/{id}")
    public R<SysAreaVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(iSysAreaService.queryById(id));
    }

    /**
     * 新增【请填写功能名称】
     */
    @Log(title = "【请填写功能名称】", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SysAreaBo bo) {
        return toAjax(iSysAreaService.insertByBo(bo));
    }

    /**
     * 修改【请填写功能名称】
     */
    @Log(title = "【请填写功能名称】", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SysAreaBo bo) {
        return toAjax(iSysAreaService.updateByBo(bo));
    }

    /**
     * 删除【请填写功能名称】
     *
     * @param ids 主键串
     */
    @Log(title = "【请填写功能名称】", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(iSysAreaService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

    /**
     * 获取浙江省下所有地级市
     */
    @GetMapping("/zhejiang-cities")
    public R<List<SysAreaVo>> getZhejiangCities() {
        return R.ok(iSysAreaService.getZhejiangCities());
    }

    /**
     * 根据二级地市ID获取对应的三级县市数据
     */
    @GetMapping("/counties/{cityId}")
    public R<List<SysAreaVo>> getCountiesByCityId(@PathVariable Long cityId) {
        return R.ok(iSysAreaService.getCountiesByCityId(cityId));
    }
}
