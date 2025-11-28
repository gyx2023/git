package com.crm.web.controller.file;

import java.util.List;
import java.util.Arrays;

import com.crm.system.domain.bo.file.FileAsyncTaskBo;
import com.crm.system.domain.vo.file.FileAsyncTaskVo;
import com.crm.system.service.file.IFileAsyncTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
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
 * 任务管理
 *
 * @author crm
 * @date 2025-11-24
 */
@Validated
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/file/fileAsyncTask")
public class FileAsyncTaskController extends BaseController {

    @Resource
    private IFileAsyncTaskService iFileAsyncTaskService;

    /**
     * 查询任务管理列表
     */
    @GetMapping("/list")
    public TableDataInfo<FileAsyncTaskVo> list(FileAsyncTaskBo bo, PageQuery pageQuery) {
        return iFileAsyncTaskService.queryPageList(bo, pageQuery
        );
    }

    /**
     * 导出任务管理列表
     */
    @SaCheckPermission("system:fileAsyncTask:export")
    @Log(title = "任务管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(FileAsyncTaskBo bo, HttpServletResponse response) {
        List<FileAsyncTaskVo> list = iFileAsyncTaskService.queryList(bo);
        ExcelUtil.exportExcel(list, "任务管理", FileAsyncTaskVo.class, response);
    }

    /**
     * 获取任务管理详细信息
     *
     * @param taskId 主键
     */
    @SaCheckPermission("system:fileAsyncTask:query")
    @GetMapping("/{taskId}")
    public R<FileAsyncTaskVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long taskId) {
        return R.ok(iFileAsyncTaskService.queryById(taskId));
    }

    /**
     * 新增任务管理
     */
    @SaCheckPermission("system:fileAsyncTask:add")
    @Log(title = "任务管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody FileAsyncTaskBo bo) {
        return toAjax(iFileAsyncTaskService.insertByBo(bo));
    }

    /**
     * 修改任务管理
     */
    @SaCheckPermission("system:fileAsyncTask:edit")
    @Log(title = "任务管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody FileAsyncTaskBo bo) {
        return toAjax(iFileAsyncTaskService.updateByBo(bo));
    }

    /**
     * 删除任务管理
     *
     * @param taskIds 主键串
     */
    @SaCheckPermission("system:fileAsyncTask:remove")
    @Log(title = "任务管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{taskIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] taskIds) {
        return toAjax(iFileAsyncTaskService.deleteWithValidByIds(Arrays.asList(taskIds), true));
    }

    /**
     * 申请导入任务
     *
     * @param file 文件
     * @param businessType 业务类型
     * @return 任务ID
     */
    @PostMapping(value = "/applyImport", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Log(title = "导入任务申请", businessType = BusinessType.INSERT)
    public R<Long> applyImport(@RequestPart("file") MultipartFile file,
                             @RequestParam("businessType") String businessType,@RequestParam("taskName") String taskName) {
        if (ObjectUtil.isNull(file)) {
            return R.fail("上传文件不能为空");
        }

        try {
            Long taskId = iFileAsyncTaskService.applyImport(file, businessType,taskName);
            return R.ok(taskId);
        } catch (Exception e) {
            log.error("导入任务申请异常", e);
            return R.fail("导入任务申请异常: " + e.getMessage());
        }
    }

    /**
     * 申请导出任务
     *
     * @param businessType 业务类型
     * @param requestParams 请求参数（JSON格式）
     * @return 任务ID
     */
    @PostMapping("/applyExport")
    @Log(title = "导出任务申请", businessType = BusinessType.INSERT)
    public R<Long> applyExport(@RequestParam("businessType") String businessType,
                             @RequestParam("requestParams") String requestParams,
                               @RequestParam("taskName") String taskName) {
        try {
            Long taskId = iFileAsyncTaskService.applyExport(businessType, requestParams,taskName);
            return R.ok(taskId);
        } catch (Exception e) {
            log.error("导出任务申请异常", e);
            return R.fail("导出任务申请异常: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     *
     * @param taskId 任务ID
     */
    @Log(title = "文件下载", businessType = BusinessType.OTHER)
    @GetMapping("/downloadTaskFile")
    public void download(@NotNull(message = "任务ID不能为空") @RequestParam("taskId") Long taskId,@NotNull(message = "任务ID不能为空") @RequestParam("fileType") String fileType, HttpServletResponse response) {
        try {
            iFileAsyncTaskService.downloadFile(taskId, fileType,response);
        } catch (Exception e) {
            log.error("文件下载失败，任务ID: {}", taskId, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }
}
