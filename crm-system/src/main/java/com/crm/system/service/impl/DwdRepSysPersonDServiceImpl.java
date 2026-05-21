package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.helper.LoginHelper;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.DwdRepSysPersonD;
import com.crm.system.domain.bo.DwdRepSysPersonDBo;
import com.crm.system.domain.vo.DwdRepSysPersonDVo;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.domain.zbzq.RepSysPersonNewDDo;
import com.crm.system.mapper.DwdRepSysPersonDMapper;
import com.crm.system.mapper.zbzq.RepSysPersonNewDMapper;
import com.crm.system.service.IDwdRepSysPersonDService;
import com.crm.system.service.ISysAreaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author crm
 * @date 2025-11-24
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DwdRepSysPersonDServiceImpl implements IDwdRepSysPersonDService {

    private final DwdRepSysPersonDMapper baseMapper;

    @Autowired
    private RepSysPersonNewDMapper repSysPersonNewDMapper;
    @Autowired
    private SysUserServiceImpl sysUserService;
    @Autowired
    private SysDeptServiceImpl sysDeptService;
    @Autowired
    private ISysAreaService sysAreaService;

    /**
     * 查询【请填写功能名称】
     */
    @Override
    public DwdRepSysPersonDVo queryById(Long id) {
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
    private void validEntityBeforeSave(DwdRepSysPersonD entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除【请填写功能名称】
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 根据城市编码和客户类型查询管理人员信息
     *
     * @param cityCode       城市编码
     * @param expandCustType 拓展客户类型，01-省级，02-市级，03-县级
     * @return 返回管理人员列表，按省级、市级、县级分类
     */
    @Override
    public List<Map<String, Object>> queryManagersByCityCodeAndType(String cityCode, String expandCustType, String grpCustType) {
        // 初始化结果集
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // 1. 通过县市编码查询AREA表对应的数据
            SysAreaVo sysAreaVo = sysAreaService.queryById(Long.valueOf(cityCode));

            if (sysAreaVo == null) {
                return result;
            }
            // 2. 获取AREA表的empCountyId和empAreaId
            int empCountyId = sysAreaVo.getEmpCountyId();
            int empAreaId = sysAreaVo.getEmpAreaId();

            if(ObjectUtil.equals("400", grpCustType)){

                    // 3. 根据拓展客户类型执行不同的查询
                    if ("01".equals(expandCustType)) {
                        // 省级: 查询三次
                        Map<String, Object> level1 = new HashMap<>();
                        level1.put("category", "省级");
                        List<Map<String, Object>> list1 = new ArrayList<>();
                        // 第一次: 查询AREA_ID=3300的记录
                        LambdaQueryWrapper<DwdRepSysPersonD> lqw1 = Wrappers.lambdaQuery();
                        lqw1.eq(DwdRepSysPersonD::getAreaId, 3300L)
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
                        // 第二次: 查询AREA_ID=empAreaId的记录
                        LambdaQueryWrapper<DwdRepSysPersonD> lqw2 = Wrappers.lambdaQuery();
                        lqw2.eq(DwdRepSysPersonD::getAreaId, (long) empAreaId)
                            .eq(DwdRepSysPersonD::getOnjobFlag, 0); // 只查询在职的客户经理
                        List<DwdRepSysPersonDVo> dataList2 = baseMapper.selectVoList(lqw2);

                        // 过滤掉县名匹配的数据，只保留纯市级数据
                        List<DwdRepSysPersonDVo> cityManagers = filterCityManagers(dataList2, String.valueOf(empAreaId));

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
                        lqw1.eq(DwdRepSysPersonD::getAreaId, (long) empAreaId)
                            .eq(DwdRepSysPersonD::getOnjobFlag, 0); // 只查询在职的客户经理
                        List<DwdRepSysPersonDVo> dataList1 = baseMapper.selectVoList(lqw1);

                        // 过滤掉县名匹配的数据，只保留纯市级数据
                        List<DwdRepSysPersonDVo> cityManagers = filterCityManagers(dataList1, String.valueOf(empAreaId));

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

                    } else if("03".equals(expandCustType))  {
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
            }else{
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
     * 过滤市级客户经理，排除县级人员
     * @param dataList 原始数据列表
     * @param countyPrefix 县级前缀
     * @return 过滤后的市级客户经理列表
     */
    private List<DwdRepSysPersonDVo> filterCityManagers(List<DwdRepSysPersonDVo> dataList, String countyPrefix) {
        // 获取该市下所有的县ID列表，用于后续匹配判断
        List<SysAreaVo> countyAreas = sysAreaService.getCountiesByCityCodePrefix(countyPrefix);
        Set<Long> countyIds = new HashSet<>();

        // 收集所有县的ID
        for (SysAreaVo county : countyAreas) {
            countyIds.add((long) county.getEmpCountyId());
        }

        // 直接通过countyId进行过滤，避免字符串匹配
        return dataList.stream()
            .filter(person -> {
                if (person.getCountyId() == null) return true;
                // 如果客户的countyId在县列表中，则为县级数据，应排除
                return !countyIds.contains(person.getCountyId());
            })
            .collect(Collectors.toList());
    }

    /**
     * 将客户经理列表添加到结果集中
     *
     * @param result 结果集
     * @param list   客户经理列表
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

    /**
     * 批量根据姓名和电话查询人员地区信息
     * @param personInfos 人员信息列表 [{fullName: "张三", mobile: "13800138000"}, ...]
     * @return 人员地区信息列表
     */
    @Override
    public List<DwdRepSysPersonDVo> selectAreaAndCounty(List<Map<String, String>> personInfos) {
        return baseMapper.selectAreaAndCounty(personInfos);
    }

    @Override
    public TableDataInfo<DwdRepSysPersonDVo> getManagerPageListByLoginUser(PageQuery pageQuery, String keyword) {
        LoginUser loginUser = LoginHelper.getLoginUser();

        LambdaQueryWrapper<DwdRepSysPersonD> lqw = Wrappers.lambdaQuery();

        Integer deptLevel = repSysPersonNewDMapper.getDeptLevel(loginUser.getUsername());
        if (deptLevel >= 1) {
            return getProvincialManagersPage(pageQuery, keyword);
        } else {
            SysUser user = sysUserService.selectUserById(loginUser.getUserId());
            SysDept dept = sysDeptService.selectDeptById(user.getDeptId());
            SysAreaVo area = sysAreaService.queryById(Long.valueOf(dept.getAreaCode()));

            if ("一级".equals(area.getType())) {
                lqw.eq(DwdRepSysPersonD::getOnjobFlag, 0);
            } else if ("二级".equals(area.getType())) {
                lqw.eq(DwdRepSysPersonD::getAreaId, area.getEmpAreaId())
                    .eq(DwdRepSysPersonD::getOnjobFlag, 0);
            } else if ("三级".equals(area.getType())) {
                lqw.eq(DwdRepSysPersonD::getCountyId, area.getEmpCountyId())
                    .eq(DwdRepSysPersonD::getOnjobFlag, 0);
            } else {
                return TableDataInfo.build();
            }
        }
        if (StringUtils.isNotBlank(keyword)) {
            lqw.and(wrapper -> wrapper
                .like(DwdRepSysPersonD::getOaUserName, keyword)
                .or()
                .like(DwdRepSysPersonD::getFullName, keyword)
            );
        }
        lqw.orderByAsc(DwdRepSysPersonD::getFullName);

        Page<DwdRepSysPersonDVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);

        return TableDataInfo.build(page);
    }
    private TableDataInfo<DwdRepSysPersonDVo> getProvincialManagersPage(PageQuery pageQuery, String keyword) {
        Page<RepSysPersonNewDDo> page = pageQuery.build();
        Page<RepSysPersonNewDDo> resultPage = repSysPersonNewDMapper.selectProvincialManagers(page, keyword);

        List<DwdRepSysPersonDVo> voList = new ArrayList<>();
        for (RepSysPersonNewDDo record : resultPage.getRecords()) {
            DwdRepSysPersonDVo vo = new DwdRepSysPersonDVo();
            vo.setEmpId(record.getEmpId());
            vo.setOaUserName(record.getOaUserName());
            vo.setFullName(record.getFullName());
            voList.add(vo);
        }

        Page<DwdRepSysPersonDVo> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(voList);

        return TableDataInfo.build(voPage);
    }

    @Override
    public List<Map<String, Object>> getDwdRepSysPersonListFromIob(Integer currentPage, Integer pageSize) {
        Integer startRow = (currentPage - 1) * pageSize + 1;
        Integer endRow = currentPage * pageSize;
        return baseMapper.selectDwdRepSysPersonListFromIob(startRow, endRow);
    }

    @Override
    public int getPersonCountFromIob() {
        return baseMapper.selectPersonCountFromIob();
    }

    @Override
    public void truncate() {
        baseMapper.delete(null);
        log.info("清空dwd_rep_sys_person_d表数据完成");
    }

    @Override
    public void insertBatch(List<DwdRepSysPersonD> list) {
        if (list != null && !list.isEmpty()) {
            log.info("开始插入{}条dwd_rep_sys_person_d数据", list.size());
            long startTime = System.currentTimeMillis();
            baseMapper.insertBatch(list);
            long endTime = System.currentTimeMillis();
            log.info("插入{}条dwd_rep_sys_person_d数据成功,耗时:{}ms", list.size(), (endTime - startTime));
        }
    }
}
