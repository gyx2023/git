package com.crm.adapter.admin.system.specialList;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.crm.common.annotation.Log;
import com.crm.common.annotation.RepeatSubmit;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.R;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import com.crm.common.enums.BusinessType;
import com.crm.common.utils.file.FileUtils;
import com.crm.system.domain.bo.SpecialListBo;
import com.crm.system.domain.vo.SpecialListVo;
import com.crm.system.service.ISpecialListService;
import com.crm.system.service.file.IFileAsyncTaskService;
import com.crm.system.utils.ExcelDropdownHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 特殊名单
 *
 * @author crm
 */
@Validated
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/system/specialList")
public class SpecialListController extends BaseController {

    @Resource
    private ISpecialListService specialListService;

    @Resource
    private IFileAsyncTaskService iFileAsyncTaskService;

    /**
     * 查询特殊名单列表
     */
    @GetMapping("/list")
    public TableDataInfo<SpecialListVo> list(SpecialListBo bo, PageQuery pageQuery) {
        return specialListService.queryPageList(bo, pageQuery);
    }

    /**
     * 新增特殊名单
     */
    @SaCheckPermission("system:specialList:add")
    @Log(title = "特殊名单", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SpecialListBo bo) {
        try {
            return toAjax(specialListService.insertByBo(bo));
        } catch (DuplicateKeyException e) {
            log.error("特殊名单新增失败，唯一键冲突", e);
            return R.fail("新增失败，该记录已存在");
        }
    }

    /**
     * 修改特殊名单
     */
    @SaCheckPermission("system:specialList:edit")
    @Log(title = "特殊名单", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SpecialListBo bo) {
        return toAjax(specialListService.updateByBo(bo));
    }

    /**
     * 删除特殊名单
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:specialList:remove")
    @Log(title = "特殊名单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Integer[] ids) {
        return toAjax(specialListService.deleteWithValidByIds(Arrays.asList(ids)));
    }


    @GetMapping("/downloadImportTemplate")
    public void downloadImportTemplate(HttpServletResponse response) {
        try {
            FileUtils.setAttachmentResponseHeader(response, "special_list_import_template.xlsx");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");

            List<List<String>> head = new ArrayList<>();
            head.add(Arrays.asList("*场景名称"));
            head.add(Arrays.asList("*特殊名单值"));
            head.add(Arrays.asList("生效时间"));
            head.add(Arrays.asList("失效时间"));
            head.add(Arrays.asList("备注"));

            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream())
                    .head(head)
                    .registerWriteHandler(new ExcelDropdownHandler(0, "yunxi_type"))
                    .build();

            WriteSheet writeSheet = EasyExcel.writerSheet("导入模板").build();
            excelWriter.write(new ArrayList<>(), writeSheet);
            excelWriter.finish();
        } catch (Exception e) {
            log.error("下载导入模板异常", e);
            throw new RuntimeException("下载导入模板失败: " + e.getMessage());
        }
    }

    /**
     * 下载批量编辑模板
     */
    @GetMapping("/downloadBatchEditTemplate")
    public void downloadBatchEditTemplate(HttpServletResponse response) {
        try {
            FileUtils.setAttachmentResponseHeader(response, "special_list_batch_edit_template.xlsx");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");

            List<List<String>> head = new ArrayList<>();
            head.add(Arrays.asList("*场景名称"));
            head.add(Arrays.asList("*特殊名单值"));
            head.add(Arrays.asList("*状态"));
            head.add(Arrays.asList("生效时间"));
            head.add(Arrays.asList("失效时间"));
            head.add(Arrays.asList("备注"));

            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream())
                    .head(head)
                    .registerWriteHandler(new ExcelDropdownHandler(0, "yunxi_type"))
                    .registerWriteHandler(new ExcelDropdownHandler(2, "special_list_status"))
                    .build();

            WriteSheet writeSheet = EasyExcel.writerSheet("批量编辑模板").build();
            excelWriter.write(new ArrayList<>(), writeSheet);
            excelWriter.finish();
        } catch (Exception e) {
            log.error("下载批量编辑模板异常", e);
            throw new RuntimeException("下载批量编辑模板失败: " + e.getMessage());
        }
    }

    /**
     * 申请导入特殊名单
     *
     * @param file         文件
     * @param businessType 业务类型
     * @param taskName     任务名称
     * @return 任务ID
     */
    @SaCheckPermission("system:specialList:import")
    @PostMapping(value = "/applyImport", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Long> applyImport(@RequestPart("file") MultipartFile file,
                               @RequestParam(defaultValue = "special_list_import") String businessType,
                               @RequestParam("taskName") String taskName) {
        try {
            Long taskId = iFileAsyncTaskService.applyImport(file, businessType, taskName, null);
            return R.ok(taskId);
        } catch (Exception e) {
            log.error("导入任务申请异常", e);
            return R.fail("导入任务申请异常: " + e.getMessage());
        }
    }

    /**
     * 申请批量编辑特殊名单
     *
     * @param file         文件
     * @param businessType 业务类型
     * @param taskName     任务名称
     * @return 任务ID
     */
    @SaCheckPermission("system:specialList:edit")
    @PostMapping(value = "/applyBatchEdit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Long> applyBatchEdit(@RequestPart("file") MultipartFile file,
                                  @RequestParam(defaultValue = "special_list_batch_edit") String businessType,
                                  @RequestParam("taskName") String taskName) {
        try {
            Long taskId = iFileAsyncTaskService.applyImport(file, businessType, taskName, null);
            return R.ok(taskId);
        } catch (Exception e) {
            log.error("批量编辑任务申请异常", e);
            return R.fail("批量编辑任务申请异常: " + e.getMessage());
        }
    }

    /**
     * 申请导出特殊名单
     *
     * @param requestParams 请求参数（JSON格式）
     * @param businessType  业务类型
     * @param taskName      任务名称
     * @return 任务ID
     */
    @SaCheckPermission("system:specialList:export")
    @PostMapping("/applyExport")
    public R<Long> applyExport(@RequestParam("requestParams") String requestParams,
                               @RequestParam(defaultValue = "special_list_export") String businessType,
                               @RequestParam("taskName") String taskName) {
        try {
            Long taskId = iFileAsyncTaskService.applyExport(businessType, requestParams, taskName);
            return R.ok(taskId);
        } catch (Exception e) {
            log.error("导出任务申请异常", e);
            return R.fail("导出任务申请异常: " + e.getMessage());
        }
    }
}
