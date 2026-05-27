//package com.crm.adapter.admin.system.zqzt;
//
//import java.util.List;
//import java.util.Arrays;
//
//import lombok.RequiredArgsConstructor;
//import javax.servlet.http.HttpServletResponse;
//import javax.validation.constraints.*;
//import cn.dev33.satoken.annotation.SaCheckPermission;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.validation.annotation.Validated;
//import com.crm.common.annotation.RepeatSubmit;
//import com.crm.common.annotation.Log;
//import com.crm.common.core.controller.BaseController;
//import com.crm.common.core.domain.PageQuery;
//import com.crm.common.core.domain.R;
//import com.crm.common.core.validate.AddGroup;
//import com.crm.common.core.validate.EditGroup;
//import com.crm.common.enums.BusinessType;
//import com.crm.common.utils.poi.ExcelUtil;
//import com.crm.system.domain.vo.ZqztGrpCustInuseUserlistVo;
//import com.crm.system.domain.bo.ZqztGrpCustInuseUserlistBo;
//import com.crm.system.service.IZqztGrpCustInuseUserlistService;
//import com.crm.common.core.page.TableDataInfo;
//
///**
// * 政企实际使用客户类型用户清单
// *
// * @author crm
// * @date 2026-01-20
// */
//@Validated
//@RequiredArgsConstructor
//@RestController
//@RequestMapping("/system/grpCustInuseUserlist")
//public class ZqztGrpCustInuseUserlistController extends BaseController {
//
//    private final IZqztGrpCustInuseUserlistService iZqztGrpCustInuseUserlistService;
//
//    /**
//     * 查询政企实际使用客户类型用户清单列表
//     */
//    @SaCheckPermission("system:grpCustInuseUserlist:list")
//    @GetMapping("/list")
//    public TableDataInfo<ZqztGrpCustInuseUserlistVo> list(ZqztGrpCustInuseUserlistBo bo, PageQuery pageQuery) {
//        return iZqztGrpCustInuseUserlistService.queryPageList(bo, pageQuery);
//    }
//
//    /**
//     * 导出政企实际使用客户类型用户清单列表
//     */
//    @SaCheckPermission("system:grpCustInuseUserlist:export")
//    @Log(title = "政企实际使用客户类型用户清单", businessType = BusinessType.EXPORT)
//    @PostMapping("/export")
//    public void export(ZqztGrpCustInuseUserlistBo bo, HttpServletResponse response) {
//        List<ZqztGrpCustInuseUserlistVo> list = iZqztGrpCustInuseUserlistService.queryList(bo);
//        ExcelUtil.exportExcel(list, "政企实际使用客户类型用户清单", ZqztGrpCustInuseUserlistVo.class, response);
//    }
//
//    /**
//     * 获取政企实际使用客户类型用户清单详细信息
//     *
//     * @param inuseId 主键
//     */
//    @SaCheckPermission("system:grpCustInuseUserlist:query")
//    @GetMapping("/{inuseId}")
//    public R<ZqztGrpCustInuseUserlistVo> getInfo(@NotNull(message = "主键不能为空")
//                                     @PathVariable String inuseId) {
//        return R.ok(iZqztGrpCustInuseUserlistService.queryById(inuseId));
//    }
//
//    /**
//     * 新增政企实际使用客户类型用户清单
//     */
//    @SaCheckPermission("system:grpCustInuseUserlist:add")
//    @Log(title = "政企实际使用客户类型用户清单", businessType = BusinessType.INSERT)
//    @RepeatSubmit()
//    @PostMapping()
//    public R<Void> add(@Validated(AddGroup.class) @RequestBody ZqztGrpCustInuseUserlistBo bo) {
//        return toAjax(iZqztGrpCustInuseUserlistService.insertByBo(bo));
//    }
//
//    /**
//     * 修改政企实际使用客户类型用户清单
//     */
//    @SaCheckPermission("system:grpCustInuseUserlist:edit")
//    @Log(title = "政企实际使用客户类型用户清单", businessType = BusinessType.UPDATE)
//    @RepeatSubmit()
//    @PutMapping()
//    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ZqztGrpCustInuseUserlistBo bo) {
//        return toAjax(iZqztGrpCustInuseUserlistService.updateByBo(bo));
//    }
//
//    /**
//     * 删除政企实际使用客户类型用户清单
//     *
//     * @param inuseIds 主键串
//     */
//    @SaCheckPermission("system:grpCustInuseUserlist:remove")
//    @Log(title = "政企实际使用客户类型用户清单", businessType = BusinessType.DELETE)
//    @DeleteMapping("/{inuseIds}")
//    public R<Void> remove(@NotEmpty(message = "主键不能为空")
//                          @PathVariable String[] inuseIds) {
//        return toAjax(iZqztGrpCustInuseUserlistService.deleteWithValidByIds(Arrays.asList(inuseIds), true));
//    }
//}
