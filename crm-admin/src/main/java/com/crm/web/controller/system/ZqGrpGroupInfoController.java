package com.crm.web.controller.system;

import java.util.List;
import java.util.Arrays;
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
import com.crm.common.enums.BusinessType;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.vo.ZqGrpGroupInfoVo;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.service.IZqGrpGroupInfoService;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.entity.SysDictData;
import com.crm.generator.util.DesensitizationUtils;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;

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
    @SaCheckPermission("system:grpGroupInfo:list")
    @GetMapping("/list")
    public TableDataInfo<ZqGrpGroupInfoVo> list(ZqGrpGroupInfoBo bo, PageQuery pageQuery,
            @RequestParam(value = "beginTime", required = false) String beginTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "timeType", required = false) String timeType) {

        // 处理前端传递的时间范围参数
        if (beginTime != null && endTime != null && timeType != null) {
            try {
                // 解析时间字符串为Date对象
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date beginDate = sdf.parse(beginTime);
                Date endDate = sdf.parse(endTime);

                // 根据timeType设置对应的时间范围字段
                if ("assign".equals(timeType)) {
                    bo.setAssignTimeStart(beginDate);
                    bo.setAssignTimeEnd(endDate);
                } else if ("create".equals(timeType)) {
                    bo.setCreateTimeStart(beginDate);
                    bo.setCreateTimeEnd(endDate);
                }

                System.out.println("时间参数处理成功: timeType=" + timeType + ", beginTime=" + beginTime + ", endTime=" + endTime);
            } catch (Exception e) {
                System.out.println("时间参数解析失败: " + e.getMessage());
            }
        }

        TableDataInfo<ZqGrpGroupInfoVo> result = iZqGrpGroupInfoService.queryPageList(bo, pageQuery);

        // 添加调试日志
        System.out.println("Controller返回数据 - 总数: " + result.getTotal());
        System.out.println("Controller返回数据 - 记录数: " + result.getRows().size());

        // 对返回数据进行脱敏处理
        if (result.getRows() != null && !result.getRows().isEmpty()) {
            for (ZqGrpGroupInfoVo vo : result.getRows()) {
                // 脱敏集团名称
                if (vo.getGrpName() != null) {
                    vo.setGrpName(DesensitizationUtils.desensitizeCompanyName(vo.getGrpName()));
                }

                // 脱敏三种客户经理名称
                if (vo.getProvManagerName() != null) {
                    vo.setProvManagerName(DesensitizationUtils.desensitizeName(vo.getProvManagerName()));
                }
                if (vo.getCityManagerName() != null) {
                    vo.setCityManagerName(DesensitizationUtils.desensitizeName(vo.getCityManagerName()));
                }
                if (vo.getCountyManagerName() != null) {
                    vo.setCountyManagerName(DesensitizationUtils.desensitizeName(vo.getCountyManagerName()));
                }
            }

            ZqGrpGroupInfoVo firstRecord = result.getRows().get(0);
            System.out.println("Controller返回数据 - 第一条记录的cityManagerOauser: " + firstRecord.getCityManagerOauser());
            System.out.println("Controller返回数据 - 第一条记录的cityManagerName: " + firstRecord.getCityManagerName());
        }

        return result;
    }

    /**
     * 导出政企集团客户信息列表
     */
    @SaCheckPermission("system:grpGroupInfo:export")
    @Log(title = "政企集团客户信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ZqGrpGroupInfoBo bo, HttpServletResponse response) {
        List<ZqGrpGroupInfoVo> list = iZqGrpGroupInfoService.queryList(bo);

        // 对导出数据进行脱敏处理
        if (list != null && !list.isEmpty()) {
            for (ZqGrpGroupInfoVo vo : list) {
                // 脱敏集团名称
                if (vo.getGrpName() != null) {
                    vo.setGrpName(DesensitizationUtils.desensitizeCompanyName(vo.getGrpName()));
                }

                // 脱敏三种客户经理名称
                if (vo.getProvManagerName() != null) {
                    vo.setProvManagerName(DesensitizationUtils.desensitizeName(vo.getProvManagerName()));
                }
                if (vo.getCityManagerName() != null) {
                    vo.setCityManagerName(DesensitizationUtils.desensitizeName(vo.getCityManagerName()));
                }
                if (vo.getCountyManagerName() != null) {
                    vo.setCountyManagerName(DesensitizationUtils.desensitizeName(vo.getCountyManagerName()));
                }
            }
        }

        ExcelUtil.exportExcel(list, "政企集团客户信息", ZqGrpGroupInfoVo.class, response);
    }

    /**
     * 获取政企集团客户信息详细信息
     *
     * @param grpId 主键
     */
    @SaCheckPermission("system:grpGroupInfo:query")
    @GetMapping("/{grpId}")
    public R<ZqGrpGroupInfoVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long grpId) {
        ZqGrpGroupInfoVo vo = iZqGrpGroupInfoService.queryById(grpId);

        // 对详情数据进行脱敏处理
        if (vo != null) {
            // 脱敏集团名称
            if (vo.getGrpName() != null) {
                vo.setGrpName(DesensitizationUtils.desensitizeCompanyName(vo.getGrpName()));
            }

            // 脱敏三种客户经理名称
            if (vo.getProvManagerName() != null) {
                vo.setProvManagerName(DesensitizationUtils.desensitizeName(vo.getProvManagerName()));
            }
            if (vo.getCityManagerName() != null) {
                vo.setCityManagerName(DesensitizationUtils.desensitizeName(vo.getCityManagerName()));
            }
            if (vo.getCountyManagerName() != null) {
                vo.setCountyManagerName(DesensitizationUtils.desensitizeName(vo.getCountyManagerName()));
            }
        }

        return R.ok(vo);
    }

    /**
     * 新增政企集团客户信息
     */
    @SaCheckPermission("system:grpGroupInfo:add")
    @Log(title = "政企集团客户信息", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ZqGrpGroupInfoBo bo) {
        return toAjax(iZqGrpGroupInfoService.insertByBo(bo));
    }

    /**
     * 修改政企集团客户信息
     */
    @SaCheckPermission("system:grpGroupInfo:edit")
    @Log(title = "政企集团客户信息", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/edit")
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ZqGrpGroupInfoBo bo) {
        return toAjax(iZqGrpGroupInfoService.updateByBo(bo));
    }

    /**
     * 删除政企集团客户信息
     *
     * @param grpIds 主键串
     */
    @SaCheckPermission("system:grpGroupInfo:remove")
    @Log(title = "政企集团客户信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{grpIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] grpIds) {
        return toAjax(iZqGrpGroupInfoService.deleteWithValidByIds(Arrays.asList(grpIds), true));
    }

    /**
     * 获取政企集团客户信息相关字典数据
     */
    @SaCheckPermission("system:grpGroupInfo:query")
    @GetMapping("/dict")
    public R<Map<String, List<SysDictData>>> getDictData() {
        // 从zq_grp_group_info表中获取字典数据
        Map<String, List<SysDictData>> result = iZqGrpGroupInfoService.getDictData();
        return R.ok(result);
    }
}
