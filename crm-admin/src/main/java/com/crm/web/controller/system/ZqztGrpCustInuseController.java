package com.crm.web.controller.system;

import com.crm.common.annotation.RepeatSubmit;
import com.crm.common.core.domain.dto.ZqztGrpCustAddUserListReqDTO;
import com.crm.common.core.domain.dto.ZqztGrpCustReqDTO;
import com.crm.common.core.domain.dto.ZqztGrpCustInuseRespDTO;
import com.crm.common.core.domain.dto.ZqztGrpCustInuseUserListRespDTO;
import com.crm.common.core.validate.EditGroup;
import lombok.RequiredArgsConstructor;
import javax.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.R;
import com.crm.system.service.IZqztGrpCustInuseService;
import com.crm.common.core.page.TableDataInfo;


/**
 * 合同和关联客户ID
 *
 * @author crm
 * @date 2026-01-19
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/grpCustInuse")
public class ZqztGrpCustInuseController extends BaseController {

    private final IZqztGrpCustInuseService iZqztGrpCustInuseService;

    /**
     * 查询合同和关联客户ID列表
     */
    @SaCheckPermission("system:grpCustInuse:list")
    @GetMapping("/list")
    public TableDataInfo<ZqztGrpCustInuseRespDTO> list(ZqztGrpCustReqDTO reqDTO, PageQuery pageQuery) {
        return iZqztGrpCustInuseService.queryPageList(reqDTO, pageQuery);
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
     * 删除合同和关联客户ID
     *
     * @param inuseIds 主键串
     */
    @SaCheckPermission("system:grpCustInuse:remove")
//    @Log(title = "集团用户关联客户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{inuseIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable String[] inuseIds) {
        return toAjax(iZqztGrpCustInuseService.deleteWithValidByIds(inuseIds, true));
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
