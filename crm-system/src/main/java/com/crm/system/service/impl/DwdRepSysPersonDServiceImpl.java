package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.crm.system.domain.bo.DwdRepSysPersonDBo;
import com.crm.system.domain.vo.DwdRepSysPersonDVo;
import com.crm.system.domain.DwdRepSysPersonD;
import com.crm.system.mapper.DwdRepSysPersonDMapper;
import com.crm.system.service.IDwdRepSysPersonDService;
import com.crm.system.service.ISysAreaService;
import com.crm.system.domain.SysArea;
import com.crm.system.domain.vo.SysAreaVo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author crm
 * @date 2025-11-24
 */
@RequiredArgsConstructor
@Service
public class DwdRepSysPersonDServiceImpl implements IDwdRepSysPersonDService {

    private final DwdRepSysPersonDMapper baseMapper;
    private final ISysAreaService sysAreaService;

    /**
     * 查询【请填写功能名称】
     */
    @Override
    public DwdRepSysPersonDVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public TableDataInfo<DwdRepSysPersonDVo> queryPageList(DwdRepSysPersonDBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<DwdRepSysPersonD> lqw = buildQueryWrapper(bo);
        Page<DwdRepSysPersonDVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<DwdRepSysPersonDVo> queryList(DwdRepSysPersonDBo bo) {
        LambdaQueryWrapper<DwdRepSysPersonD> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<DwdRepSysPersonD> buildQueryWrapper(DwdRepSysPersonDBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DwdRepSysPersonD> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getEmpId()), DwdRepSysPersonD::getEmpId, bo.getEmpId());
        lqw.eq(StringUtils.isNotBlank(bo.getHrCode()), DwdRepSysPersonD::getHrCode, bo.getHrCode());
        lqw.like(StringUtils.isNotBlank(bo.getFullName()), DwdRepSysPersonD::getFullName, bo.getFullName());
        lqw.like(StringUtils.isNotBlank(bo.getOaUserName()), DwdRepSysPersonD::getOaUserName, bo.getOaUserName());
        lqw.eq(bo.getOnjobFlag() != null, DwdRepSysPersonD::getOnjobFlag, bo.getOnjobFlag());
        lqw.eq(StringUtils.isNotBlank(bo.getUserIdentityCode()), DwdRepSysPersonD::getUserIdentityCode, bo.getUserIdentityCode());
        lqw.eq(StringUtils.isNotBlank(bo.getMobile()), DwdRepSysPersonD::getMobile, bo.getMobile());
        lqw.eq(StringUtils.isNotBlank(bo.getUserPersonTypeCode()), DwdRepSysPersonD::getUserPersonTypeCode, bo.getUserPersonTypeCode());
        lqw.eq(StringUtils.isNotBlank(bo.getLabelCategoryCode()), DwdRepSysPersonD::getLabelCategoryCode, bo.getLabelCategoryCode());
        lqw.eq(StringUtils.isNotBlank(bo.getElementName3MapCode()), DwdRepSysPersonD::getElementName3MapCode, bo.getElementName3MapCode());
        lqw.eq(StringUtils.isNotBlank(bo.getPersonBigType()), DwdRepSysPersonD::getPersonBigType, bo.getPersonBigType());
        lqw.eq(bo.getAreaId() != null, DwdRepSysPersonD::getAreaId, bo.getAreaId());
        lqw.like(StringUtils.isNotBlank(bo.getAreaName()), DwdRepSysPersonD::getAreaName, bo.getAreaName());
        lqw.eq(bo.getCountyId() != null, DwdRepSysPersonD::getCountyId, bo.getCountyId());
        lqw.like(StringUtils.isNotBlank(bo.getCountyName()), DwdRepSysPersonD::getCountyName, bo.getCountyName());
        lqw.eq(StringUtils.isNotBlank(bo.getHxZoneId()), DwdRepSysPersonD::getHxZoneId, bo.getHxZoneId());
        lqw.like(StringUtils.isNotBlank(bo.getHxZoneName()), DwdRepSysPersonD::getHxZoneName, bo.getHxZoneName());
        lqw.eq(StringUtils.isNotBlank(bo.getDeptTypeCode()), DwdRepSysPersonD::getDeptTypeCode, bo.getDeptTypeCode());
        lqw.eq(StringUtils.isNotBlank(bo.getBakColumn1()), DwdRepSysPersonD::getBakColumn1, bo.getBakColumn1());
        lqw.eq(StringUtils.isNotBlank(bo.getBakColumn2()), DwdRepSysPersonD::getBakColumn2, bo.getBakColumn2());
        lqw.eq(StringUtils.isNotBlank(bo.getBakColumn3()), DwdRepSysPersonD::getBakColumn3, bo.getBakColumn3());
        lqw.eq(StringUtils.isNotBlank(bo.getBakColumn4()), DwdRepSysPersonD::getBakColumn4, bo.getBakColumn4());
        lqw.eq(StringUtils.isNotBlank(bo.getBakColumn5()), DwdRepSysPersonD::getBakColumn5, bo.getBakColumn5());
        lqw.eq(StringUtils.isNotBlank(bo.getBakColumn6()), DwdRepSysPersonD::getBakColumn6, bo.getBakColumn6());
        lqw.eq(bo.getQuitDate() != null, DwdRepSysPersonD::getQuitDate, bo.getQuitDate());
        lqw.eq(StringUtils.isNotBlank(bo.getCreatedBy()), DwdRepSysPersonD::getCreatedBy, bo.getCreatedBy());
        lqw.eq(bo.getCreationDate() != null, DwdRepSysPersonD::getCreationDate, bo.getCreationDate());
        lqw.eq(StringUtils.isNotBlank(bo.getLastUpdatedBy()), DwdRepSysPersonD::getLastUpdatedBy, bo.getLastUpdatedBy());
        lqw.eq(bo.getLastUpdateDate() != null, DwdRepSysPersonD::getLastUpdateDate, bo.getLastUpdateDate());
        lqw.eq(StringUtils.isNotBlank(bo.getBakColumn7()), DwdRepSysPersonD::getBakColumn7, bo.getBakColumn7());
        lqw.eq(StringUtils.isNotBlank(bo.getBakColumn8()), DwdRepSysPersonD::getBakColumn8, bo.getBakColumn8());
        lqw.eq(StringUtils.isNotBlank(bo.getBakColumn9()), DwdRepSysPersonD::getBakColumn9, bo.getBakColumn9());
        lqw.eq(StringUtils.isNotBlank(bo.getSrZoneId()), DwdRepSysPersonD::getSrZoneId, bo.getSrZoneId());
        lqw.eq(bo.getEtlDataCycle() != null, DwdRepSysPersonD::getEtlDataCycle, bo.getEtlDataCycle());
        lqw.eq(bo.getEtlTime() != null, DwdRepSysPersonD::getEtlTime, bo.getEtlTime());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(DwdRepSysPersonDBo bo) {
        DwdRepSysPersonD add = BeanUtil.toBean(bo, DwdRepSysPersonD.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改【请填写功能名称】
     */
    @Override
    public Boolean updateByBo(DwdRepSysPersonDBo bo) {
        DwdRepSysPersonD update = BeanUtil.toBean(bo, DwdRepSysPersonD.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(DwdRepSysPersonD entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除【请填写功能名称】
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 根据县市编码和拓展客户类型查询客户经理信息
     * @param cityCode 县市编码
     * @param expandCustType 拓展客户类型(01:省级,02:市级,03:县级)
     * @return 客户经理信息列表
     */
    @Override
    public List<Map<String, Object>> queryManagersByCityCodeAndType(String cityCode, String expandCustType) {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // 1. 通过县市编码查询AREA表的empCountyId
            SysAreaVo areaVo = sysAreaService.queryById(Long.valueOf(cityCode));
            if (areaVo == null) {
                return result;
            }

            int empCountyId = areaVo.getEmpCountyId();
            String countyIdStr = String.valueOf(empCountyId);
            String countyPrefix = countyIdStr.substring(0, 4); // 获取前四位

            // 2. 获取该市下所有的县名列表，用于后续匹配判断
            List<SysAreaVo> countyAreas = sysAreaService.getCountiesByCityCodePrefix(countyPrefix);
            Set<String> countyFullNames = new HashSet<>(); // 完整县名
            Set<String> countyShortNames = new HashSet<>(); // 县名的前两个字和前三个字
            
            for (SysAreaVo county : countyAreas) {
                String name = county.getName();
                if (name != null) {
                    countyFullNames.add(name);
                    // 添加县名的前两个字和前三个字用于匹配
                    if (name.length() >= 2) {
                        countyShortNames.add(name.substring(0, 2));
                    }
                    if (name.length() >= 3) {
                        countyShortNames.add(name.substring(0, 3));
                    }
                }
            }

            // 3. 根据拓展客户类型执行不同的查询
            if ("01".equals(expandCustType)) {
                // 省级: 查询三次
                Map<String, Object> level1 = new HashMap<>();
                level1.put("category", "省级");
                List<Map<String, Object>> list1 = new ArrayList<>();
                // 第一次: 只查询COUNTY_ID的前四位=3300的记录
                LambdaQueryWrapper<DwdRepSysPersonD> lqw1 = Wrappers.lambdaQuery();
                lqw1.apply("SUBSTR(COUNTY_ID, 1, 4) = '3300'")
                    .eq(DwdRepSysPersonD::getOnjobFlag, 0); // 只查询在职的客户经理
                List<DwdRepSysPersonDVo> dataList1 = baseMapper.selectVoList(lqw1);
                addManagersToList(list1, dataList1);
                level1.put("managers", list1);
                if (!list1.isEmpty()) {
                    result.add(level1);
                }

                Map<String, Object> level2 = new HashMap<>();
                level2.put("category", "市级");
                List<Map<String, Object>> list2 = new ArrayList<>();
                // 第二次: 查询当前COUNTY_ID前四位=数据库里该字段的前四位
                LambdaQueryWrapper<DwdRepSysPersonD> lqw2 = Wrappers.lambdaQuery();
                lqw2.apply("SUBSTR(COUNTY_ID, 1, 4) = {0}", countyPrefix)
                    .eq(DwdRepSysPersonD::getOnjobFlag, 0); // 只查询在职的客户经理
                List<DwdRepSysPersonDVo> dataList2 = baseMapper.selectVoList(lqw2);
                
                // 过滤掉县名匹配的数据，只保留纯市级数据
                List<DwdRepSysPersonDVo> cityManagers = dataList2.stream()
                    .filter(person -> {
                        if (person.getCountyName() == null) return true;
                        // 检查是否包含完整县名或者县名的前缀
                        boolean containsFullCounty = countyFullNames.stream()
                            .anyMatch(cn -> person.getCountyName().contains(cn));
                        boolean containsShortCounty = countyShortNames.stream()
                            .anyMatch(sn -> person.getCountyName().contains(sn));
                        // 如果包含任何县名标识，则为县级数据，应排除
                        return !(containsFullCounty || containsShortCounty);
                    })
                    .collect(Collectors.toList());
                    
                addManagersToList(list2, cityManagers);
                level2.put("managers", list2);
                if (!list2.isEmpty()) {
                    result.add(level2);
                }

                Map<String, Object> level3 = new HashMap<>();
                level3.put("category", "县级");
                List<Map<String, Object>> list3 = new ArrayList<>();
                // 第三次: 查询COUNTY_ID=数据库的该id
                LambdaQueryWrapper<DwdRepSysPersonD> lqw3 = Wrappers.lambdaQuery();
                lqw3.eq(DwdRepSysPersonD::getCountyId, empCountyId)
                    .eq(DwdRepSysPersonD::getOnjobFlag, 0); // 只查询在职的客户经理
                List<DwdRepSysPersonDVo> dataList3 = baseMapper.selectVoList(lqw3);
                addManagersToList(list3, dataList3);
                level3.put("managers", list3);
                if (!list3.isEmpty()) {
                    result.add(level3);
                }

            } else if ("02".equals(expandCustType)) {
                // 市级: 返回两个结果
                // 先查询市级所有结果
                LambdaQueryWrapper<DwdRepSysPersonD> lqw1 = Wrappers.lambdaQuery();
                lqw1.apply("SUBSTR(COUNTY_ID, 1, 4) = {0}", countyPrefix)
                    .eq(DwdRepSysPersonD::getOnjobFlag, 0); // 只查询在职的客户经理
                List<DwdRepSysPersonDVo> dataList1 = baseMapper.selectVoList(lqw1);

                // 过滤掉县名匹配的数据，只保留纯市级数据
                List<DwdRepSysPersonDVo> cityManagers = dataList1.stream()
                    .filter(person -> {
                        if (person.getCountyName() == null) return true;
                        // 检查是否包含完整县名或者县名的前缀
                        boolean containsFullCounty = countyFullNames.stream()
                            .anyMatch(cn -> person.getCountyName().contains(cn));
                        boolean containsShortCounty = countyShortNames.stream()
                            .anyMatch(sn -> person.getCountyName().contains(sn));
                        // 如果包含任何县名标识，则为县级数据，应排除
                        return !(containsFullCounty || containsShortCounty);
                    })
                    .collect(Collectors.toList());

                // 再查询县级结果
                LambdaQueryWrapper<DwdRepSysPersonD> lqw2 = Wrappers.lambdaQuery();
                lqw2.eq(DwdRepSysPersonD::getCountyId, empCountyId)
                    .eq(DwdRepSysPersonD::getOnjobFlag, 0); // 只查询在职的客户经理
                List<DwdRepSysPersonDVo> dataList2 = baseMapper.selectVoList(lqw2);

                Map<String, Object> level1 = new HashMap<>();
                level1.put("category", "市级");
                List<Map<String, Object>> list1 = new ArrayList<>();
                addManagersToList(list1, cityManagers);
                level1.put("managers", list1);
                if (!list1.isEmpty()) {
                    result.add(level1);
                }

                Map<String, Object> level2 = new HashMap<>();
                level2.put("category", "县级");
                List<Map<String, Object>> list2 = new ArrayList<>();
                addManagersToList(list2, dataList2);
                level2.put("managers", list2);
                if (!list2.isEmpty()) {
                    result.add(level2);
                }

            } else if ("03".equals(expandCustType)) {
                // 县级: 返回COUNTY_ID=数据库的该id的结果
                Map<String, Object> level = new HashMap<>();
                level.put("category", "县级");
                List<Map<String, Object>> list = new ArrayList<>();
                LambdaQueryWrapper<DwdRepSysPersonD> lqw = Wrappers.lambdaQuery();
                lqw.eq(DwdRepSysPersonD::getCountyId, empCountyId)
                   .eq(DwdRepSysPersonD::getOnjobFlag, 0); // 只查询在职的客户经理
                List<DwdRepSysPersonDVo> dataList = baseMapper.selectVoList(lqw);
                addManagersToList(list, dataList);
                level.put("managers", list);
                if (!list.isEmpty()) {
                    result.add(level);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 将客户经理列表添加到结果集中
     * @param result 结果集
     * @param list 客户经理列表
     */
    private void addManagersToList(List<Map<String, Object>> result, List<DwdRepSysPersonDVo> list) {
        if (list != null && !list.isEmpty()) {
            for (DwdRepSysPersonDVo person : list) {
                Map<String, Object> manager = new HashMap<>();
                manager.put("name", person.getFullName());
                manager.put("oaAccount", person.getOaUserName());
                result.add(manager);
            }
        }
    }
}