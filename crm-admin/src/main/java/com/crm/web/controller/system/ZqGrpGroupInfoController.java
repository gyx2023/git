package com.crm.web.controller.system;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.crm.common.annotation.Log;
import com.crm.common.annotation.RepeatSubmit;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.R;
import com.crm.common.core.domain.entity.SysDictData;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import com.crm.common.enums.BusinessType;
import com.crm.common.utils.file.FileUtils;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.domain.vo.*;
import com.crm.system.service.IZqGrpGroupInfoService;
import com.crm.system.utils.ExcelDropdownHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 政企集团客户信息
 *
 * @author crm
 * @date 2025-11-20
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/grpGroupInfo")
public class ZqGrpGroupInfoController extends BaseController {

    private final IZqGrpGroupInfoService iZqGrpGroupInfoService;

    /**
     * 查询政企集团客户信息列表
     */
    @GetMapping("/lists")
    public TableDataInfo<ZqGrpGroupInfoVo> list(ZqGrpGroupInfoBo bo, PageQuery pageQuery,
                                                @RequestParam(value = "grpCreateTimeBegin", required = false) String grpCreateTimeBegin,
                                                @RequestParam(value = "grpCreateTimeEnd", required = false) String grpCreateTimeEnd) {
        return iZqGrpGroupInfoService.queryPageList(bo, pageQuery, grpCreateTimeBegin, grpCreateTimeEnd);
    }


    /**
     * 查询政企集团客户信息列表 - 处理前端传来的分配时间和创建时间参数
     */
    @GetMapping("/list")
    public TableDataInfo<ZqGrpGroupInfoVo> listWithTimes(ZqGrpGroupInfoBo bo, PageQuery pageQuery,
                                                         @RequestParam(value = "assignBeginTime", required = false) String assignBeginTime,
                                                         @RequestParam(value = "assignEndTime", required = false) String assignEndTime,
                                                         @RequestParam(value = "createBeginTime", required = false) String createBeginTime,
                                                         @RequestParam(value = "createEndTime", required = false) String createEndTime) {

        // 处理分配时间范围参数
        if (assignBeginTime != null || assignEndTime != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (assignBeginTime != null) {
                    Date beginDate = sdf.parse(assignBeginTime);
                    bo.setAssignTimeStart(beginDate);
                }
                if (assignEndTime != null) {
                    Date endDate = sdf.parse(assignEndTime);
                    bo.setAssignTimeEnd(endDate);
                }
                System.out.println("分配时间参数处理成功: assignBeginTime=" + assignBeginTime + ", assignEndTime=" + assignEndTime);
            } catch (Exception e) {
                System.out.println("分配时间参数解析失败: " + e.getMessage());
            }
        }

        // 处理创建时间范围参数
        if (createBeginTime != null || createEndTime != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (createBeginTime != null) {
                    Date beginDate = sdf.parse(createBeginTime);
                    bo.setCreateTimeStart(beginDate);
                }
                if (createEndTime != null) {
                    Date endDate = sdf.parse(createEndTime);
                    bo.setCreateTimeEnd(endDate);
                }
                System.out.println("创建时间参数处理成功: createBeginTime=" + createBeginTime + ", createEndTime=" + createEndTime);
            } catch (Exception e) {
                System.out.println("创建时间参数解析失败: " + e.getMessage());
            }
        }

        // 查询数据并返回
        return iZqGrpGroupInfoService.queryPageList(bo, pageQuery);
    }


    /**
     * 导出政企集团客户信息列表
     */
    @Log(title = "政企集团客户信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ZqGrpGroupInfoBo bo, HttpServletResponse response,
                       @RequestParam(value = "grpCreateTimeBegin", required = false) String grpCreateTimeBegin,
                       @RequestParam(value = "grpCreateTimeEnd", required = false) String grpCreateTimeEnd) {
        // 处理时间范围参数
        if (grpCreateTimeBegin != null && !grpCreateTimeBegin.isEmpty() && grpCreateTimeEnd != null && !grpCreateTimeEnd.isEmpty()) {
            bo.setGrpCreateTimeBegin(grpCreateTimeBegin);
            bo.setGrpCreateTimeEnd(grpCreateTimeEnd);
        }

        List<ZqGrpGroupInfoVo> list = iZqGrpGroupInfoService.queryList(bo);
        ExcelUtil.exportExcel(list, "政企集团客户信息", ZqGrpGroupInfoVo.class, response);
    }

    /**
     * 获取政企集团客户信息详细信息
     *
     * @param grpId 主键
     */
    @GetMapping("/{grpId}")
    public R<ZqGrpGroupInfoVo> getInfo(@NotNull(message = "主键不能为空")
                                       @PathVariable Long grpId) {
        ZqGrpGroupInfoVo vo = iZqGrpGroupInfoService.queryById(grpId);
        return R.ok(vo);
    }

    /**
     * 新增政企集团客户信息
     */
    @Log(title = "政企集团客户信息", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ZqGrpGroupInfoBo bo) {
        return toAjax(iZqGrpGroupInfoService.insertByBo(bo));
    }

    /**
     * 修改政企集团客户信息
     */
    @Log(title = "政企集团客户信息", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/edit")
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ZqGrpGroupInfoBo bo) {
        return toAjax(iZqGrpGroupInfoService.updateByBo(bo));
    }

    /**
     * 修改政企集团客户经理信息
     */
    @Log(title = "政企集团客户经理信息", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/edits")
    public R<Void> edits(@RequestBody ZqGrpGroupInfoBo bo) {
        // 检查是否有需要更新的客户经理信息字段
        boolean hasUpdateFields = false;
        if (bo.getProvManagerOauser() != null || bo.getProvManagerName() != null ||
            bo.getCityManagerOauser() != null || bo.getCityManagerName() != null ||
            bo.getCountyManagerOauser() != null || bo.getCountyManagerName() != null) {
            hasUpdateFields = true;
        }

        // 如果没有任何需要更新的字段，直接返回成功
        if (!hasUpdateFields) {
            return R.ok("没有需要更新的字段");
        }

        // 创建一个新的bo对象，只保留需要的字段
        ZqGrpGroupInfoBo managerBo = new ZqGrpGroupInfoBo();
        managerBo.setGrpId(bo.getGrpId());

        // 只设置非空的客户经理信息
        if (bo.getProvManagerOauser() != null) {
            managerBo.setProvManagerOauser(bo.getProvManagerOauser());
        }
        if (bo.getProvManagerName() != null) {
            managerBo.setProvManagerName(bo.getProvManagerName());
        }
        if (bo.getCityManagerOauser() != null) {
            managerBo.setCityManagerOauser(bo.getCityManagerOauser());
        }
        if (bo.getCityManagerName() != null) {
            managerBo.setCityManagerName(bo.getCityManagerName());
        }
        if (bo.getCountyManagerOauser() != null) {
            managerBo.setCountyManagerOauser(bo.getCountyManagerOauser());
        }
        if (bo.getCountyManagerName() != null) {
            managerBo.setCountyManagerName(bo.getCountyManagerName());
        }

        try {
            boolean result = iZqGrpGroupInfoService.updateByBo1(managerBo);
            if (result) {
                return R.ok("更新成功");
            } else {
                return R.fail("更新失败，未找到对应的集团信息");
            }
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 删除政企集团客户信息
     *
     * @param grpIds 主键串
     */
    @Log(title = "政企集团客户信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{grpIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] grpIds) {
        return toAjax(iZqGrpGroupInfoService.deleteWithValidByIds(Arrays.asList(grpIds), true));
    }

    /**
     * 获取政企集团客户信息相关字典数据
     */
    @GetMapping("/dict")
    public R<Map<String, List<SysDictData>>> getDictData() {
        // 从zq_grp_group_info表中获取字典数据
        Map<String, List<SysDictData>> result = iZqGrpGroupInfoService.getDictData();
        return R.ok(result);
    }

    /**
     * 下载批量分配客户经理模板
     */
    @GetMapping("/downloadManagerTemplate")
    public void downloadManagerTemplate(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "批量分配客户经理模板", CustomerManagerTemplateVo.class, response);
    }

    /**
     * 下载客户归属部门维护模板
     */
    @GetMapping("/downloadBusiDeptTemplate")
    public void downloadBusiDeptTemplate(HttpServletResponse response) {
        try {
            // 设置响应头
            FileUtils.setAttachmentResponseHeader(response, "客户归属部门维护模板.xlsx");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");

            // 创建Excel写入器并注册下拉框处理器
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), GrpBusiDeptTemplateVo.class)
                .registerWriteHandler(new ExcelDropdownHandler(1, "sys_busi_dept_type")) // 第二列(索引1)是客户归属部门
                .build();

            WriteSheet writeSheet = EasyExcel.writerSheet("客户归属部门维护模板").build();
            excelWriter.write(new ArrayList<>(), writeSheet);
            excelWriter.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载集团客户拓展类型维护模板
     */
    @GetMapping("/downloadExpandTypeTemplate")
    public void downloadExpandTypeTemplate(HttpServletResponse response) {
        try {
            // 设置响应头
            FileUtils.setAttachmentResponseHeader(response, "集团客户拓展类型维护模板.xlsx");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");

            // 创建Excel写入器并注册下拉框处理器
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), GrpExpandTypeTemplateVo.class)
                .registerWriteHandler(new ExcelDropdownHandler(1, "sys_expand_type")) // 第二列(索引1)是集团客户拓展类型
                .build();

            WriteSheet writeSheet = EasyExcel.writerSheet("集团客户拓展类型维护模板").build();
            excelWriter.write(new ArrayList<>(), writeSheet);
            excelWriter.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载客户存增量维护模板
     */
    @GetMapping("/downloadStockTemplate")
    public void downloadStockTemplate(HttpServletResponse response) {
        try {
            // 设置响应头
            FileUtils.setAttachmentResponseHeader(response, "客户存增量维护模板.xlsx");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");

            // 创建Excel写入器并注册下拉框处理器
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), CustomerExistingStockTemplateVo.class)
                .registerWriteHandler(new ExcelDropdownHandler(1, "sys_province_newold_type"))
                .build();

            WriteSheet writeSheet = EasyExcel.writerSheet("客户存增量维护模板").build();
            excelWriter.write(new ArrayList<>(), writeSheet);
            excelWriter.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载客户类别维护模板
     */
    @GetMapping("/downloadCustomerTypeTemplate")
    public void downloadCustomerTypeTemplate(HttpServletResponse response) {
        try {
            // 设置响应头
            FileUtils.setAttachmentResponseHeader(response, "集团客户类别维护模板.xlsx");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");

            // 创建Excel写入器并注册下拉框处理器
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), CustomerTypeTemplateVo.class)
                .registerWriteHandler(new ExcelDropdownHandler(1, "sys_cust_type"))
                .build();

            WriteSheet writeSheet = EasyExcel.writerSheet("集团客户类别维护模板").build();
            excelWriter.write(new ArrayList<>(), writeSheet);
            excelWriter.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
