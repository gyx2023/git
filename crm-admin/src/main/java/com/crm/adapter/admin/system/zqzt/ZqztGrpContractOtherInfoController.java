package com.crm.adapter.admin.system.zqzt;

import cn.hutool.json.JSONUtil;
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
import com.crm.system.domain.bo.ZqztGrpContractOtherInfoBo;
import com.crm.system.domain.vo.ContractUserTemplateVo;
import com.crm.system.domain.vo.ZqztGrpContractOtherInfoVo;
import com.crm.system.service.ISysOssService;
import com.crm.system.service.IZqztGrpContractOtherInfoService;
import com.crm.system.service.file.IFileAsyncTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 集团合同扩展信息
 *
 * @author 万超凡
 * @date 2026-01-13
 */
@Validated
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/grpContractOtherInfo")
public class ZqztGrpContractOtherInfoController extends BaseController {

    private final IZqztGrpContractOtherInfoService iZqztGrpContractOtherInfoService;
    private final ISysOssService sysOssService;
    private final IFileAsyncTaskService fileAsyncTaskService;

    /**
     * 查询集团合同扩展信息列表
     */
    @GetMapping("/list")
    public TableDataInfo<ZqztGrpContractOtherInfoVo> list(ZqztGrpContractOtherInfoBo bo, PageQuery pageQuery) {
        return iZqztGrpContractOtherInfoService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出集团合同扩展信息列表
     */
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
    @GetMapping("/{contractId}")
    public R<ZqztGrpContractOtherInfoVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable String contractId) {
        return R.ok(iZqztGrpContractOtherInfoService.queryById(contractId));
    }

    /**
     * 新增集团合同扩展信息
     */
    @Log(title = "集团合同扩展信息", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ZqztGrpContractOtherInfoBo bo) {
        return toAjax(iZqztGrpContractOtherInfoService.insertByBo(bo));
    }

    /**
     * 修改集团合同扩展信息
     */
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
    @GetMapping("/contract/{contractId}")
    public R<List<ZqztGrpContractOtherInfoVo>> listByContractId(@PathVariable String contractId) {
        List<ZqztGrpContractOtherInfoVo> list = iZqztGrpContractOtherInfoService.selectByContractId(contractId);
        return R.ok(list);
    }

    /**
     * 根据合同ID查询合同关联的客户信息，将合同扩展信息与客户维护信息关联
     * 支持按服务号码(infoVal2)进行可选筛选
     *
     * @param contractId 合同ID
     * @param infoVal2 服务号码（可选，可能为加密格式）
     * @return 关联的客户信息分页列表
     */
    @GetMapping({"/contract/userInfo"})
    public TableDataInfo<ZqztGrpContractOtherInfoVo> listContractRelatedUserInfo(
        String contractId,
        String infoVal2,
        PageQuery pageQuery) {

        return iZqztGrpContractOtherInfoService.selectContractRelatedUserInfoPage(contractId, infoVal2, pageQuery);
    }

    /**
     * 批量使合同关联用户信息失效
     *
     * @param ids 合同关联信息ID列表
     * @return 操作结果
     */
    @Log(title = "合同关联用户", businessType = BusinessType.UPDATE)
    @PutMapping("/batchInvalidate")
    public R<Void> batchInvalidateContractUserRelation(@RequestBody List<Long> ids) {
        return toAjax(iZqztGrpContractOtherInfoService.batchInvalidateContractUserRelation(ids));
    }

    /**
     * 批量新增集团合同扩展信息
     */
    @Log(title = "集团合同扩展信息", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/batch")
    public R<Void> batchAdd(@Validated(AddGroup.class) @RequestBody List<ZqztGrpContractOtherInfoBo> boList) {
        return toAjax(iZqztGrpContractOtherInfoService.insertBatchByBo(boList));
    }

    /**
     * 下载合同关联用户批量导入模板
     */
    @GetMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) {
        ExcelUtil.exportExcel(null, "合同关联用户批量导入模板", ContractUserTemplateVo.class, response);
    }

    /**
     * 批量导入合同关联用户
     */
    @Log(title = "合同关联用户", businessType = BusinessType.IMPORT)
    @PostMapping("/batchImport")
    public R<Long> batchImport(@RequestParam("file") MultipartFile file, @RequestParam("contractId") String contractId, @RequestParam("hqGrpCode") String hqGrpCode) {
        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("contractId", contractId);
            params.put("hqGrpCode", hqGrpCode);
            String requestParams = JSONUtil.toJsonStr(params);

            // 申请导入任务
            String businessType = "contract_user_import";
            String taskName = "合同关联用户导入任务";
            Long taskId = fileAsyncTaskService.applyImport(file, businessType, taskName, requestParams);

            return R.ok("批量导入任务已提交，请等待处理完成",taskId );
        } catch (Exception e) {
            log.error("批量导入失败", e);
            return R.fail("批量导入失败：" + e.getMessage());
        }
    }
}
