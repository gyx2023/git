package com.crm.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
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
import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.domain.vo.*;
import com.crm.system.service.IZqGrpGroupInfoService;
import com.crm.system.service.IZqztOperRecordService;
import com.crm.system.utils.ExcelDropdownHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private IZqztOperRecordService iZqztOperRecordService;

    /**
     * 查询政企集团客户信息列表（根据sys_param表里的配置决定省市县经理信息是否脱敏）
     */
    @GetMapping("/lists")
    public TableDataInfo<ZqGrpGroupInfoVo2> list(ZqGrpGroupInfoBo bo, PageQuery pageQuery,
                                                @RequestParam(value = "grpCreateTimeBegin", required = false) String grpCreateTimeBegin,
                                                @RequestParam(value = "grpCreateTimeEnd", required = false) String grpCreateTimeEnd) {
        return iZqGrpGroupInfoService.queryPageListWithoutSensitive(bo, pageQuery, grpCreateTimeBegin, grpCreateTimeEnd);
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
     * 获取政企集团客户信息详细信息（不脱敏）
     *
     * @param grpId 主键
     */
    @GetMapping("/detail/{grpId}")
    @SaCheckPermission(value={"system:client:viewDetail"})
    public R<ZqGrpGroupInfo> getInfoWithoutSensitive(@NotNull(message = "主键不能为空")
                                                       @PathVariable Long grpId) {
        try {
            ZqGrpGroupInfo info = iZqGrpGroupInfoService.queryByIdWithoutSensitive(grpId);
            // 正常查询则状态为 0，error_msg 为空
            iZqztOperRecordService.insertByGroupInfo(info, null, null);
            return R.ok(info);
        } catch (Exception e) {
            // 如果出现异常状态为 1，记录错误信息
            try {
                // 尝试查询集团信息用于记录操作日志
                ZqGrpGroupInfo errorInfo = new ZqGrpGroupInfo();
                errorInfo.setGrpId(grpId);
                iZqztOperRecordService.insertByGroupInfo(errorInfo, 1, e.getMessage());
            } catch (Exception ex) {
                // 如果记录日志也失败，打印错误信息
                ex.printStackTrace();
            }
            return R.fail(e.getMessage());
        }
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
    @SaCheckPermission(value={"system:client:update","system:client:department","system:client:expandType","system:client:stock","system:client:customerType","system:client:contractManagement"},mode= SaMode.OR)
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ZqGrpGroupInfoBo bo) {
        return toAjax(iZqGrpGroupInfoService.updateByBo(bo));
    }

    /**
     * 修改政企集团客户经理信息
     */
    @Log(title = "政企集团客户经理信息", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/edits")
    @SaCheckPermission("system:manager:distribute")
    public R<Void> edits(@RequestBody ZqGrpGroupInfoBo bo) {
        // 检查是否有需要更新的客户经理信息字段
        boolean hasUpdateFields = false;
        if (bo.getCustManagerEmpId() != null || bo.getCustManagerOauser() != null || bo.getCustManagerName() != null) {
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
        if (bo.getCustManagerEmpId() != null) {
            managerBo.setCustManagerEmpId(bo.getCustManagerEmpId());
        }
        if (bo.getCustManagerOauser() != null) {
            managerBo.setCustManagerOauser(bo.getCustManagerOauser());
        }
        if (bo.getCustManagerName() != null) {
            managerBo.setCustManagerName(bo.getCustManagerName());
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

    @GetMapping("/queryStt")
    public String queryStt() {
        // 从zq_grp_group_info表中获取字典数据
        String result = iZqGrpGroupInfoService.getUserByStt();
        return result;
    }

}
