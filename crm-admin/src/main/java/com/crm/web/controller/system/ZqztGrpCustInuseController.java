package com.crm.web.controller.system;

import java.util.List;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.crm.common.core.domain.dto.ZqztGrpCustAddUserListReqDTO;
import com.crm.common.core.domain.dto.ZqztGrpCustInuseRespDTO;
import com.crm.common.core.domain.dto.ZqztGrpCustInuseUserListRespDTO;
import com.crm.common.core.domain.dto.ZqztGrpCustReqDTO;
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
import com.crm.system.domain.vo.ZqztGrpCustInuseVo;
import com.crm.system.domain.bo.ZqztGrpCustInuseBo;
import com.crm.system.service.IZqztGrpCustInuseService;
import com.crm.common.core.page.TableDataInfo;

/**
 * 政企主体-实际使用客户（zqzt_grp_cust_inuse）
 *
 * @author crm
 * @date 2026-01-20
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/grpCustInuse")
public class ZqztGrpCustInuseController extends BaseController {

    private final IZqztGrpCustInuseService iZqztGrpCustInuseService;

    /**
     * 查询政企主体-实际使用客户（zqzt_grp_cust_inuse）列表
     */
    @SaCheckPermission("system:grpCustInuse:list")
    @GetMapping("/list")
    public TableDataInfo<ZqztGrpCustInuseVo> list(ZqztGrpCustInuseBo bo, PageQuery pageQuery) {
        return iZqztGrpCustInuseService.queryPageList(bo, pageQuery);
    }
    /**
     * 查询关联客户列表
     */
    @SaCheckPermission("system:grpCustInuse:list")
    @GetMapping("/grpList")
    public TableDataInfo<ZqztGrpCustInuseRespDTO> grpList(ZqztGrpCustReqDTO reqDTO, PageQuery pageQuery) {
        return iZqztGrpCustInuseService.queryPageGrpList(reqDTO, pageQuery);
    }
    /**
     * 导出政企主体-实际使用客户（zqzt_grp_cust_inuse）列表
     */
    @SaCheckPermission("system:grpCustInuse:export")
    @Log(title = "政企主体-实际使用客户（zqzt_grp_cust_inuse）", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ZqztGrpCustInuseBo bo, HttpServletResponse response) {
        List<ZqztGrpCustInuseVo> list = iZqztGrpCustInuseService.queryList(bo);
        ExcelUtil.exportExcel(list, "政企主体-实际使用客户（zqzt_grp_cust_inuse）", ZqztGrpCustInuseVo.class, response);
    }

    /**
     * 获取政企主体-实际使用客户（zqzt_grp_cust_inuse）详细信息
     *
     * @param inuseId 主键
     */
    @SaCheckPermission("system:grpCustInuse:query")
    @GetMapping("/{inuseId}")
    public R<ZqztGrpCustInuseVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable String inuseId) {
        return R.ok(iZqztGrpCustInuseService.queryById(inuseId));
    }

    /**
     * 新增政企主体-实际使用客户（zqzt_grp_cust_inuse）
     */
    @SaCheckPermission("system:grpCustInuse:add")
    @Log(title = "政企主体-实际使用客户（zqzt_grp_cust_inuse）", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ZqztGrpCustInuseBo bo) {
        return toAjax(iZqztGrpCustInuseService.insertByBo(bo));
    }

    /**
     * 修改政企主体-实际使用客户（zqzt_grp_cust_inuse）
     */
    @SaCheckPermission("system:grpCustInuse:edit")
    @Log(title = "政企主体-实际使用客户（zqzt_grp_cust_inuse）", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ZqztGrpCustInuseBo bo) {
        return toAjax(iZqztGrpCustInuseService.updateByBo(bo));
    }

    /**
     * 删除政企主体-实际使用客户（zqzt_grp_cust_inuse）
     *
     * @param inuseIds 主键串
     */
    @SaCheckPermission("system:grpCustInuse:remove")
    @Log(title = "政企主体-实际使用客户（zqzt_grp_cust_inuse）", businessType = BusinessType.DELETE)
    @DeleteMapping("/{inuseIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable String[] inuseIds) {
        return toAjax(iZqztGrpCustInuseService.deleteWithValidByIds(Arrays.asList(inuseIds), true));
    }
    /**
     * 查询待添加关联客户用户列表
     */
    @SaCheckPermission("system:grpCustInuse:userList")
    @GetMapping("/getAddUserList")
    public TableDataInfo<ZqztGrpCustInuseUserListRespDTO> getAddUserList(ZqztGrpCustReqDTO reqDTO, PageQuery pageQuery) {
        return iZqztGrpCustInuseService.getAddUserList(reqDTO, pageQuery);
    }
    /**
     * 添加关联客户用户
     */
    @SaCheckPermission("system:grpCustInuse:addUserList")
//    @Log(title = "集团用户关联客户管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addUserList")
    public R<Void> addUserList(@Validated(EditGroup.class) @RequestBody ZqztGrpCustAddUserListReqDTO reqDTO) {
        return toAjax(iZqztGrpCustInuseService.addUserList(reqDTO));
    }
    /**
     * 查询关联客户用户列表
     */
    @SaCheckPermission("system:grpCustInuse:userList")
    @GetMapping("/userList")
    public TableDataInfo<ZqztGrpCustInuseUserListRespDTO> userList(ZqztGrpCustReqDTO reqDTO, PageQuery pageQuery) {
        return iZqztGrpCustInuseService.queryPageUserList(reqDTO, pageQuery);
    }
    /**
     * 更新关联客户
     */
    @SaCheckPermission("system:grpCustInuse:add")
//    @Log(title = "集团用户关联客户管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/addInuse")
    public R<Void> addInuse(@Validated(EditGroup.class) @RequestBody ZqztGrpCustReqDTO reqDTO) {
        return toAjax(iZqztGrpCustInuseService.addInuse(reqDTO));
    }

    /**
     * 删除关联客户用户
     *
     * @param subscrbids 主键串
     */
    @SaCheckPermission("system:grpCustInuse:removeUserList")
//    @Log(title = "集团用户关联客户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/userList/{subscrbids}")
    public R<Void> removeUserList(@NotEmpty(message = "主键不能为空") @PathVariable String[] subscrbids) {
        return toAjax(iZqztGrpCustInuseService.deleteUserListWithValidByIds(subscrbids, true));
    }
}
