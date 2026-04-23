package com.crm.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.crm.common.annotation.Log;
import com.crm.common.annotation.RepeatSubmit;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.R;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import com.crm.common.enums.BusinessType;
import com.crm.common.helper.LoginHelper;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.bo.DwdRepSysPersonDBo;
import com.crm.system.domain.vo.DwdRepSysPersonDVo;
import com.crm.system.service.IDwdRepSysPersonDService;
import com.crm.system.service.ISysAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 客户经理信息控制器
 *
 * @author crm
 * @date 2025-11-24
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/repSysPersonD")
public class DwdRepSysPersonDController extends BaseController {

    private final IDwdRepSysPersonDService iDwdRepSysPersonDService;
    private final ISysAreaService iSysAreaService;

    /**
     * 查询【请填写功能名称】列表
     */
    @SaCheckPermission("system:repSysPersonD:list")
    @GetMapping("/list")
    public TableDataInfo<DwdRepSysPersonDVo> list(DwdRepSysPersonDBo bo, PageQuery pageQuery) {
        return iDwdRepSysPersonDService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出【请填写功能名称】列表
     */
    @SaCheckPermission("system:repSysPersonD:export")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(DwdRepSysPersonDBo bo, HttpServletResponse response) {
        List<DwdRepSysPersonDVo> list = iDwdRepSysPersonDService.queryList(bo);
        ExcelUtil.exportExcel(list, "【请填写功能名称】", DwdRepSysPersonDVo.class, response);
    }

    /**
     * 获取【请填写功能名称】详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:repSysPersonD:query")
    @GetMapping("/{id}")
    public R<DwdRepSysPersonDVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(iDwdRepSysPersonDService.queryById(id));
    }

    /**
     * 新增【请填写功能名称】
     */
    @SaCheckPermission("system:repSysPersonD:add")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody DwdRepSysPersonDBo bo) {
        return toAjax(iDwdRepSysPersonDService.insertByBo(bo));
    }

    /**
     * 修改【请填写功能名称】
     */
    @SaCheckPermission("system:repSysPersonD:edit")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody DwdRepSysPersonDBo bo) {
        return toAjax(iDwdRepSysPersonDService.updateByBo(bo));
    }

    /**
     * 删除客户经理信息
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:repSysPersonD:remove")
    @Log(title = "客户经理信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(iDwdRepSysPersonDService.deleteWithValidByIds(Arrays.asList(ids), true));
    }



    /**
     * 根据县市编码和拓展客户类型查询客户经理信息
     * @param cityCode 县市编码
     * @param expandCustType 拓展客户类型(01:省级,02:市级,03:县级)
     * @return 客户经理信息列表
     */
    @GetMapping("/queryManagersByCityCodeAndType")
    public R<List<Map<String, Object>>> queryManagersByCityCodeAndType(
            @RequestParam String cityCode,
            @RequestParam(name = "expandType", required = false) String expandCustType,
            @RequestParam(name = "grpCustType", required = false) String grpCustType) {
        try {
            List<Map<String, Object>> result = iDwdRepSysPersonDService.queryManagersByCityCodeAndType(cityCode, expandCustType,grpCustType);
            return R.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("查询客户经理信息失败：" + e.getMessage());
        }
    }

    /**
     * 根据当前登录用户来查询自己部门下的客户经理信息
     */
    @GetMapping("/getManagersByLoginUser")
    public R<List<Object>> getManagersByLoginUser(){
        return R.ok(iDwdRepSysPersonDService.getManagersByLoginUser());
    }
}
