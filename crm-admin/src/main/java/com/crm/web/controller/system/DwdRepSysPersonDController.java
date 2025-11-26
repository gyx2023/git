package com.crm.web.controller.system;

import java.util.*;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import liquibase.pro.packaged.I;

import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import com.crm.system.domain.vo.SysAreaVo;
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
import com.crm.system.domain.SysArea;
import com.crm.system.domain.vo.DwdRepSysPersonDVo;
import com.crm.system.domain.bo.DwdRepSysPersonDBo;
import com.crm.system.service.IDwdRepSysPersonDService;
import com.crm.system.service.ISysAreaService;
import com.crm.common.core.page.TableDataInfo;

/**
 * 客户经理信息控制器
 *
 * @author crm
 * @date 2025-11-24
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/repSysPersonD")
public class DwdRepSysPersonDController extends BaseController {

    private final IDwdRepSysPersonDService iDwdRepSysPersonDService;
    private final ISysAreaService iSysAreaService;

    /**
     * 查询【请填写功能名称】列表
     */
    @SaCheckPermission("system:repSysPersonD:list")
    @GetMapping("/list")
    public TableDataInfo<DwdRepSysPersonDVo> list(DwdRepSysPersonDBo bo, PageQuery pageQuery) {
        return iDwdRepSysPersonDService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出【请填写功能名称】列表
     */
    @SaCheckPermission("system:repSysPersonD:export")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(DwdRepSysPersonDBo bo, HttpServletResponse response) {
        List<DwdRepSysPersonDVo> list = iDwdRepSysPersonDService.queryList(bo);
        ExcelUtil.exportExcel(list, "【请填写功能名称】", DwdRepSysPersonDVo.class, response);
    }

    /**
     * 获取【请填写功能名称】详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:repSysPersonD:query")
    @GetMapping("/{id}")
    public R<DwdRepSysPersonDVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(iDwdRepSysPersonDService.queryById(id));
    }

    /**
     * 新增【请填写功能名称】
     */
    @SaCheckPermission("system:repSysPersonD:add")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody DwdRepSysPersonDBo bo) {
        return toAjax(iDwdRepSysPersonDService.insertByBo(bo));
    }

    /**
     * 修改【请填写功能名称】
     */
    @SaCheckPermission("system:repSysPersonD:edit")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody DwdRepSysPersonDBo bo) {
        return toAjax(iDwdRepSysPersonDService.updateByBo(bo));
    }

    /**
     * 删除客户经理信息
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:repSysPersonD:remove")
    @Log(title = "客户经理信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(iDwdRepSysPersonDService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

    /**
     * 根据县市编码查询客户经理信息
     * @param cityCode 县市编码（如330108）
     * @return 按级别分类的客户经理信息
     */
    @GetMapping("/queryByCityCode")
    public R<Map<String, List<Map<String, String>>>> queryByCityCode(@RequestParam String cityCode, @RequestParam(required = false) String cust) {
        try {
            // 查询SysArea表，根据cityCode匹配数据
            SysAreaVo areaVo = queryAreaByCityCode(cityCode);
            if (areaVo == null) {
                return R.fail("未找到对应的地区信息");
            }

            String type = areaVo.getType();
            int empAreaId = areaVo.getEmpAreaId();
            int empCountyId = areaVo.getEmpCountyId();

            // 准备返回结果
            Map<String, List<Map<String, String>>> result = new HashMap<>();
            result.put("provincialManagers", new ArrayList<>());
            result.put("cityManagers", new ArrayList<>());
            result.put("countyManagers", new ArrayList<>());

            // 根据type级别查询客户经理
            if ("一级".equals(type)) {
                // 省级：查询所有客户经理
                result.put("provincialManagers", queryManagersByLevel("PROVINCE"));
                result.put("cityManagers", queryManagersByLevel("CITY"));
                result.put("countyManagers", queryManagersByLevel("COUNTY"));
            } else if ("二级".equals(type)) {
                // 市级：查询市级和县级客户经理
                result.put("cityManagers", queryManagersByArea(empAreaId, "CITY"));
                result.put("countyManagers", queryManagersByArea(empAreaId, "COUNTY"));
            } else if ("三级".equals(type)) {
                // 县级：只查询县级客户经理
                result.put("countyManagers", queryManagersByCounty(empCountyId));
            }

            return R.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("查询客户经理信息失败：" + e.getMessage());
        }
    }

    /**
     * 根据cityCode查询区域信息
     * @param cityCode 县市编码，将直接作为id查询
     * @return 区域信息对象，包含emp_area_id和emp_county_id，用于关联查询客户经理表
     */
    private SysAreaVo queryAreaByCityCode(String cityCode) {
        try {
            // 直接使用id查询单条记录，获取区域信息
            // 后续会使用查询结果中的emp_area_id和emp_county_id关联查询客户经理表
            Integer id = Integer.valueOf(cityCode);
            return iSysAreaService.queryById(Long.valueOf(id));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new RuntimeException("cityCode格式错误，无法转换为数字类型");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询区域信息失败：" + e.getMessage());
        }
    }

    /**
     * 根据客户经理级别查询
     */
    private List<Map<String, String>> queryManagersByLevel(String level) {
        DwdRepSysPersonDBo bo = new DwdRepSysPersonDBo();
        bo.setPersonBigType(level);
        List<Map<String, String>> managers = new ArrayList<>();

        List<DwdRepSysPersonDVo> list = iDwdRepSysPersonDService.queryList(bo);

        for (DwdRepSysPersonDVo person : list) {
            Map<String, String> manager = new HashMap<>();
            manager.put("name", person.getFullName());
            manager.put("oaAccount", person.getOaUserName());
            managers.add(manager);
        }

        return managers;
    }

    /**
     * 根据地区ID查询特定级别客户经理
     */
    private List<Map<String, String>> queryManagersByArea(int areaId, String level) {
        DwdRepSysPersonDBo bo = new DwdRepSysPersonDBo();
        bo.setAreaId(areaId);
        bo.setPersonBigType(level);
        List<Map<String, String>> managers = new ArrayList<>();

        List<DwdRepSysPersonDVo> list = iDwdRepSysPersonDService.queryList(bo);

        for (DwdRepSysPersonDVo person : list) {
            Map<String, String> manager = new HashMap<>();
            manager.put("name", person.getFullName());
            manager.put("oaAccount", person.getOaUserName());
            managers.add(manager);
        }

        return managers;
    }

    /**
     * 根据区县ID查询县级客户经理
     */
    private List<Map<String, String>> queryManagersByCounty(int countyId) {

        DwdRepSysPersonDBo bo = new DwdRepSysPersonDBo();
        bo.setCountyId(countyId);

        System.out.println("构建查询条件：countyId=" + countyId);

        List<Map<String, String>> managers = new ArrayList<>();

        // 执行查询并打印查询结果数量
        List<DwdRepSysPersonDVo> list = iDwdRepSysPersonDService.queryList(bo);
        System.out.println("县级客户经理查询结果数量: " + (list != null ? list.size() : 0));

        // 如果有结果，转换为所需格式
        if (list != null && !list.isEmpty()) {
            for (DwdRepSysPersonDVo person : list) {
                Map<String, String> manager = new HashMap<>();
                manager.put("name", person.getFullName());
                manager.put("oaAccount", person.getOaUserName());
                managers.add(manager);
            }
        }

        return managers;
    }
}
