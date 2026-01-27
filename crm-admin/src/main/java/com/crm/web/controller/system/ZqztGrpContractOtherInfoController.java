package com.crm.web.controller.system;

import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
import com.crm.system.domain.vo.ZqztGrpContractOtherInfoVo;
import com.crm.system.domain.bo.ZqztGrpContractOtherInfoBo;
import com.crm.system.service.IZqztGrpContractOtherInfoService;
import com.crm.common.core.page.TableDataInfo;

/**
 * 集团合同扩展信息
 *
 * @author 万超凡
 * @date 2026-01-13
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/grpContractOtherInfo")
public class ZqztGrpContractOtherInfoController extends BaseController {

    private final IZqztGrpContractOtherInfoService iZqztGrpContractOtherInfoService;

    /**
     * 查询集团合同扩展信息列表
     */
    @SaCheckPermission("system:grpContractOtherInfo:list")
    @GetMapping("/list")
    public TableDataInfo<ZqztGrpContractOtherInfoVo> list(ZqztGrpContractOtherInfoBo bo, PageQuery pageQuery) {
        return iZqztGrpContractOtherInfoService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出集团合同扩展信息列表
     */
    @SaCheckPermission("system:grpContractOtherInfo:export")
    @Log(title = "集团合同扩展信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ZqztGrpContractOtherInfoBo bo, HttpServletResponse response) {
        List<ZqztGrpContractOtherInfoVo> list = iZqztGrpContractOtherInfoService.queryList(bo);
        ExcelUtil.exportExcel(list, "集团合同扩展信息", ZqztGrpContractOtherInfoVo.class, response);
    }

    /**
     * 获取集团合同扩展信息详细信息
     *
     * @param contractId 主键
     */
    @SaCheckPermission("system:grpContractOtherInfo:query")
    @GetMapping("/{contractId}")
    public R<ZqztGrpContractOtherInfoVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable String contractId) {
        return R.ok(iZqztGrpContractOtherInfoService.queryById(contractId));
    }

    /**
     * 新增集团合同扩展信息
     */
    @SaCheckPermission("system:grpContractOtherInfo:add")
    @Log(title = "集团合同扩展信息", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ZqztGrpContractOtherInfoBo bo) {
        return toAjax(iZqztGrpContractOtherInfoService.insertByBo(bo));
    }

    /**
     * 修改集团合同扩展信息
     */
    @SaCheckPermission("system:grpContractOtherInfo:edit")
    @Log(title = "集团合同扩展信息", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ZqztGrpContractOtherInfoBo bo) {
        return toAjax(iZqztGrpContractOtherInfoService.updateByBo(bo));
    }

    /**
     * 删除集团合同扩展信息
     *
     * @param contractIds 主键串
     */
    @SaCheckPermission("system:grpContractOtherInfo:remove")
    @Log(title = "集团合同扩展信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{contractIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable String[] contractIds) {
        return toAjax(iZqztGrpContractOtherInfoService.deleteWithValidByIds(Arrays.asList(contractIds), true));
    }

    /**
     * 根据合同ID查询集团合同扩展信息列表
     *
     * @param contractId 合同ID
     * @return 集团合同扩展信息列表
     */
    @SaCheckPermission("system:grpContractOtherInfo:list")
    @GetMapping("/contract/{contractId}")
    public R<List<ZqztGrpContractOtherInfoVo>> listByContractId(@PathVariable String contractId) {
        List<ZqztGrpContractOtherInfoVo> list = iZqztGrpContractOtherInfoService.selectByContractId(contractId);
        return R.ok(list);
    }

    /**
     * 根据合同ID和服务号码查询集团合同扩展信息列表
     *
     * @param contractId 合同ID
     * @param infoVal2 服务号码
     * @return 集团合同扩展信息列表
     */
    @SaCheckPermission("system:grpContractOtherInfo:list")
    @GetMapping("/contract/{contractId}/infoVal2/{infoVal2}")
    public R<List<ZqztGrpContractOtherInfoVo>> listByContractIdAndInfoVal2(@PathVariable String contractId, @PathVariable String infoVal2) {
        List<ZqztGrpContractOtherInfoVo> list = iZqztGrpContractOtherInfoService.selectByContractIdAndInfoVal2(contractId, infoVal2);
        return R.ok(list);
    }

    /**
     * 根据合同ID查询合同关联的客户信息，将合同扩展信息与客户维护信息关联
     * 支持按服务号码(infoVal2)进行可选筛选
     *
     * @param contractId 合同ID
     * @param infoVal2 服务号码（可选）
     * @return 关联的客户信息列表
     */
    @SaCheckPermission("system:grpContractOtherInfo:list")
    @GetMapping({"/contract/{contractId}/userInfo", "/contract/{contractId}/infoVal2/{infoVal2}/userInfo"})
    public R<List<Map<String, Object>>> listContractRelatedUserInfo(@PathVariable String contractId, @PathVariable(required = false) String infoVal2) {
        List<Map<String, Object>> list = iZqztGrpContractOtherInfoService.selectContractRelatedUserInfo(contractId, infoVal2);
        return R.ok(list);
    }

    /**
     * 批量使合同关联用户信息失效
     *
     * @param ids 合同关联信息ID列表
     * @return 操作结果
     */
    @SaCheckPermission("system:grpContractOtherInfo:edit")
    @Log(title = "合同关联用户", businessType = BusinessType.UPDATE)
    @PutMapping("/batchInvalidate")
    public R<Void> batchInvalidateContractUserRelation(@RequestBody List<Long> ids) {
        return toAjax(iZqztGrpContractOtherInfoService.batchInvalidateContractUserRelation(ids));
    }
}