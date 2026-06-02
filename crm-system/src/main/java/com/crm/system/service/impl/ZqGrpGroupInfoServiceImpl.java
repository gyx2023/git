package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.core.domain.entity.SysDictData;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.core.page.TableDataInfo;
import com.crm.system.enums.GrpExpandTypeenum;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.common.utils.StringUtils;
import com.crm.common.utils.Desensitization;
import com.crm.system.domain.dao.sys.SysArea;
import com.crm.system.domain.dao.sys.SysCategory;
import com.crm.system.domain.dao.ZqGrpGroupInfo;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.domain.dao.SysParamDo;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.domain.vo.SysCategoryVo;
import com.crm.system.domain.vo.ZqGrpGroupInfoVo;
import com.crm.system.domain.vo.ZqGrpGroupInfoVo2;
import com.crm.system.mapper.sys.SysAreaMapper;
import com.crm.system.mapper.sys.SysCategoryMapper;
import com.crm.system.mapper.ZqGrpGroupInfoMapper;
import com.crm.system.mapper.zbzq.RepSysPersonNewDMapper;
import com.crm.system.service.*;
import com.crm.system.service.sys.ISysDeptService;
import com.crm.system.service.sys.ISysDictDataService;
import com.crm.system.service.sys.ISysParamService;
import com.crm.system.service.sys.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 政企集团客户信息Service业务层处理
 *
 * @author crm
 * @date 2025-11-20
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class ZqGrpGroupInfoServiceImpl implements IZqGrpGroupInfoService {

    private final ZqGrpGroupInfoMapper baseMapper;
    private final SysAreaMapper sysAreaMapper;
    private final IZqGrpGroupLogService zqGrpGroupLogService;
    private final ISysDictDataService dictService;
    private final ISysAreaService sysAreaService;
    private final ISysCategoryService sysCategoryService; // 添加行业分类服务
    private final SysCategoryMapper sysCategoryMapper; // 添加行业分类Mapper
    @Resource
    private ISysParamService sysParamService;
    @Resource
    private RepSysPersonNewDMapper repSysPersonNewDMapper;
    @Resource
    private ISysUserService sysUserService;
    @Resource
    private ISysDeptService sysDeptService;



    /**
     * 查询政企集团客户信息
     */
    @Override
    public ZqGrpGroupInfoVo queryById(Long grpId){
        ZqGrpGroupInfoVo vo = baseMapper.selectVoById(grpId);
        return vo;
    }

    /**
     * 查询政企集团客户信息（不脱敏）
     */
    @Override
    public ZqGrpGroupInfo queryByIdWithoutSensitive(Long grpId){
        ZqGrpGroupInfo info = baseMapper.selectById(grpId);

        if (info == null) {
            return null;
        }

        try {
            // 查询脱敏开关配置
            SysParamDo sysParamDo = sysParamService.findByCode("manager_info_desensitization_switch");

            // 如果配置不存在或 value 不是 "on"，则不进行脱敏
            if (sysParamDo == null || !"on".equalsIgnoreCase(sysParamDo.getValue())) {
                log.debug("经理信息脱敏开关未开启，不进行脱敏处理");
                return info;
            }

            log.debug("经理信息脱敏开关已开启，开始脱敏处理");

            // 脱敏实体客户经理姓名
            if (StringUtils.isNotBlank(info.getCustManagerName())) {
                info.setCustManagerName(Desensitization.desensitizeName(info.getCustManagerName()));
            }
        } catch (Exception e) {
            log.error("经理信息脱敏处理失败: {}", e.getMessage(), e);
            // 脱敏失败不影响主流程，继续返回数据
        }
        return info;
    }

    /**
     * 查询政企集团客户信息列表
     */
    @Override
    public TableDataInfo<ZqGrpGroupInfoVo> queryPageList(ZqGrpGroupInfoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ZqGrpGroupInfo> lqw = buildQueryWrapper(bo);
        LoginUser loginUser = LoginHelper.getLoginUser();
        Integer deptLevel =  repSysPersonNewDMapper.getDeptLevel(loginUser.getUsername());
        if(deptLevel>=1){
            lqw.eq(ZqGrpGroupInfo::getGrpExpandType, GrpExpandTypeenum.PROVINCE_ZT.getCode());
        }
        // 先查询为 ZqGrpGroupInfoVo2
        Page<ZqGrpGroupInfoVo2> resultPage = baseMapper.selectPageList(pageQuery.build(), lqw);

        // 根据 sys_param 表的配置决定是否脱敏
        applyDesensitizationIfNeeded(resultPage.getRecords());

        // 转换为 ZqGrpGroupInfoVo 返回
        List<ZqGrpGroupInfoVo> voList = resultPage.getRecords().stream()
            .map(vo2 -> {
                ZqGrpGroupInfoVo vo = new ZqGrpGroupInfoVo();
                BeanUtil.copyProperties(vo2, vo);
                return vo;
            })
            .collect(Collectors.toList());

        Page<ZqGrpGroupInfoVo> result = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        result.setRecords(voList);

        return TableDataInfo.build(result);
    }

    /**
     * 查询政企集团客户信息列表
     */
//    @Override
//    public TableDataInfo<ZqGrpGroupInfoVo> queryPageList(ZqGrpGroupInfoBo bo, PageQuery pageQuery, String grpCreateTimeBegin, String grpCreateTimeEnd) {
//        LambdaQueryWrapper<ZqGrpGroupInfo> lqw = buildQueryWrapper(bo);
//        LoginUser loginUser = LoginHelper.getLoginUser();
//        Integer deptLevel =  repSysPersonNewDMapper.getDeptLevel(loginUser.getUsername());
//        if(deptLevel>=1){
//            lqw.eq(ZqGrpGroupInfo::getGrpExpandType, GrpExpandTypeenum.PROVINCE_ZT.getCode());
//        }
//        // 处理集团级别查询条件：如果 grplevel 为空则查询不等于 Y 的数据，如果不为空则按参数查询
//        if (StringUtils.isBlank(bo.getGrpLevel())) {
//            lqw.ne(ZqGrpGroupInfo::getGrpLevel, "Y");
//        }
//        // 处理时间范围参数
//        if (grpCreateTimeBegin != null && !grpCreateTimeBegin.isEmpty() && grpCreateTimeEnd != null && !grpCreateTimeEnd.isEmpty()) {
//            try {
//                // 前端已传入完整的时间格式(yyyy-MM-dd HH:mm:ss)，直接解析使用
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                Date beginDate = sdf.parse(grpCreateTimeBegin);
//                Date endDate = sdf.parse(grpCreateTimeEnd);
//                lqw.between(ZqGrpGroupInfo::getGrpCreateTime, beginDate, endDate);
//            } catch (Exception e) {
//                log.warn("时间参数解析失败: {}", e.getMessage());
//            }
//        }
//
//        Page<ZqGrpGroupInfoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
//
//        return TableDataInfo.build(result);
//    }

    /**
     * 查询政企集团客户信息列表（根据sys_param表里的配置决定省市县经理信息是否脱敏）
     */
    @Override
    public TableDataInfo<ZqGrpGroupInfoVo2> queryPageListWithoutSensitive(ZqGrpGroupInfoBo bo, PageQuery pageQuery, String grpCreateTimeBegin, String grpCreateTimeEnd) {
        LambdaQueryWrapper<ZqGrpGroupInfo> lqw = buildQueryWrapper(bo);
        LoginUser loginUser = LoginHelper.getLoginUser();
        Integer deptLevel =  repSysPersonNewDMapper.getDeptLevel(loginUser.getUsername());
        if(deptLevel>=1){
            lqw.eq(ZqGrpGroupInfo::getGrpExpandType, GrpExpandTypeenum.PROVINCE_ZT.getCode());
        }

        // 处理时间范围参数
        if (grpCreateTimeBegin != null && !grpCreateTimeBegin.isEmpty() && grpCreateTimeEnd != null && !grpCreateTimeEnd.isEmpty()) {
            try {
                // 前端已传入完整的时间格式(yyyy-MM-dd HH:mm:ss)，直接解析使用
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date beginDate = sdf.parse(grpCreateTimeBegin);
                Date endDate = sdf.parse(grpCreateTimeEnd);
                lqw.between(ZqGrpGroupInfo::getGrpCreateTime, beginDate, endDate);
            } catch (Exception e) {
                log.warn("时间参数解析失败: {}", e.getMessage());
            }
        }

        Page<ZqGrpGroupInfoVo2> result = baseMapper.selectPageList(pageQuery.build(), lqw);

        // 根据 sys_param 表的配置决定是否脱敏
        applyDesensitizationIfNeeded(result.getRecords());

        return TableDataInfo.build(result);
    }

     @Override
    public List<ZqGrpGroupInfoVo> exportListLevel(ZqGrpGroupInfoBo bo, String areaCode) {
        LambdaQueryWrapper<ZqGrpGroupInfo> lqw = buildExportQueryWrapper(bo, areaCode);
        lqw.ne(ZqGrpGroupInfo::getGrpLevel, "Y");
        List<ZqGrpGroupInfoVo> list = baseMapper.selectVoList(lqw);

        // 字典翻译
        translateDictLabels(list);
        return list;
    }

    /**
     * 根据areaCode查询政企集团客户信息列表（用于异步任务导出）
     *
     * @param bo 查询条件
     * @param areaCode 区域代码
     * @return 政企集团客户信息列表
     */
    @Override
    public List<ZqGrpGroupInfoVo> exportList(ZqGrpGroupInfoBo bo, String areaCode) {

        Map<String, Object> dictCache = preLoadDictCache(bo, areaCode);
        LambdaQueryWrapper<ZqGrpGroupInfo> lqw = buildExportQueryWrapper(bo, areaCode);

        // 获取总数量
        long totalCount = baseMapper.selectCount(lqw);

        if (totalCount == 0) {
            return new ArrayList<>();
        }

        // 分批处理
        int batchSize = 1000;
        List<ZqGrpGroupInfoVo> allResults = new ArrayList<>();

        for (int offset = 0; offset < totalCount; offset += batchSize) {
            // 使用分页查询获取当前批次的数据
            Page<ZqGrpGroupInfo> page = new Page<>((offset / batchSize) + 1, batchSize, false);
            Page<ZqGrpGroupInfoVo> resultPage = baseMapper.selectVoPage(page, lqw);
            List<ZqGrpGroupInfoVo> batchList = resultPage.getRecords();

            // 对当前批次数据进行字典翻译（使用预加载的缓存）
            translateDictLabelsWithCache(batchList, dictCache);

            allResults.addAll(batchList);

            // 建议：对于大量数据，考虑直接写入文件而不是累积到内存
            if (allResults.size() > 10000) { // 每1万条处理一次
                // 这里可以触发文件写入逻辑
            }
        }

        return allResults;
    }
    /**
     * 预加载字典缓存
     */
    private Map<String, Object> preLoadDictCache(ZqGrpGroupInfoBo bo, String areaCode) {
        LambdaQueryWrapper<ZqGrpGroupInfo> lqw = buildExportQueryWrapper(bo, areaCode);

        // 获取所有需要的ID集合
        List<ZqGrpGroupInfo> allData = baseMapper.selectList(lqw);

        Set<Long> areaIds = new HashSet<>();
        Set<Long> countyIds = new HashSet<>();
        Set<String> industryMajorIds = new HashSet<>();
        Set<String> industrySubIds = new HashSet<>();
        Set<String> industryDetailIds = new HashSet<>();

        for (ZqGrpGroupInfo info : allData) {
            if (StringUtils.isNotBlank(info.getAreaId())) {
                try {
                    areaIds.add(Long.valueOf(info.getAreaId()));
                } catch (NumberFormatException e) {
                    log.warn("地市ID格式错误: {}", info.getAreaId());
                }
            }

            if (StringUtils.isNotBlank(info.getCountyId())) {
                try {
                    countyIds.add(Long.valueOf(info.getCountyId()));
                } catch (NumberFormatException e) {
                    log.warn("县分ID格式错误: {}", info.getCountyId());
                }
            }

            if (StringUtils.isNotBlank(info.getIndustryMajorKind())) {
                industryMajorIds.add(info.getIndustryMajorKind());
            }

            if (StringUtils.isNotBlank(info.getIndustrySubKind())) {
                industrySubIds.add(info.getIndustrySubKind());
            }

            if (StringUtils.isNotBlank(info.getIndustryDetailKind())) {
                industryDetailIds.add(info.getIndustryDetailKind());
            }
        }

        // 批量查询字典数据
        Map<Long, String> areaMap = batchQueryAreaNames(areaIds);
        Map<Long, String> countyMap = batchQueryCountyNames(countyIds);
        Map<String, String> industryMajorMap = batchQueryIndustryNames(industryMajorIds);
        Map<String, String> industrySubMap = batchQueryIndustryNames(industrySubIds);
        Map<String, String> industryDetailMap = batchQueryIndustryNames(industryDetailIds);

        Map<String, Object> cache = new HashMap<>();
        cache.put("areaMap", areaMap);
        cache.put("countyMap", countyMap);
        cache.put("industryMajorMap", industryMajorMap);
        cache.put("industrySubMap", industrySubMap);
        cache.put("industryDetailMap", industryDetailMap);

        return cache;
    }
    /**
     * 批量查询区域名称
     */
    private Map<Long, String> batchQueryAreaNames(Set<Long> areaIds) {
        if (areaIds.isEmpty()) {
            return new HashMap<>();
        }

        // 这里应该有一个真正的批量查询方法，如果不存在则创建
        List<SysArea> areas = sysAreaMapper.selectBatchIds(areaIds);
        return areas.stream()
            .collect(Collectors.toMap(SysArea::getId, SysArea::getName));
    }

    /**
     * 批量查询县区名称
     */
    private Map<Long, String> batchQueryCountyNames(Set<Long> countyIds) {
        if (countyIds.isEmpty()) {
            return new HashMap<>();
        }

        List<SysArea> counties = sysAreaMapper.selectBatchIds(countyIds);
        return counties.stream()
            .collect(Collectors.toMap(SysArea::getId, SysArea::getName));
    }

    /**
     * 批量查询行业分类名称
     */
    private Map<String, String> batchQueryIndustryNames(Set<String> industryIds) {
        if (industryIds.isEmpty()) {
            return new HashMap<>();
        }

        List<SysCategory> categories = sysCategoryMapper.selectBatchIds(industryIds);
        return categories.stream()
            .collect(Collectors.toMap(SysCategory::getId, SysCategory::getName));
    }

    /**
     * 使用缓存进行字典翻译
     */
    private void translateDictLabelsWithCache(List<ZqGrpGroupInfoVo> list, Map<String, Object> dictCache) {
        if (list == null || list.isEmpty() || dictCache == null) {
            return;
        }

        Map<Long, String> areaMap = (Map<Long, String>) dictCache.get("areaMap");
        Map<Long, String> countyMap = (Map<Long, String>) dictCache.get("countyMap");
        Map<String, String> industryMajorMap = (Map<String, String>) dictCache.get("industryMajorMap");
        Map<String, String> industrySubMap = (Map<String, String>) dictCache.get("industrySubMap");
        Map<String, String> industryDetailMap = (Map<String, String>) dictCache.get("industryDetailMap");

        // 应用翻译结果
        for (ZqGrpGroupInfoVo vo : list) {
            if (StringUtils.isNotBlank(vo.getAreaId())) {
                try {
                    Long areaId = Long.valueOf(vo.getAreaId());
                    String areaName = areaMap.get(areaId);
                    if (areaName != null) {
                        vo.setAreaId(areaName);
                    }
                } catch (NumberFormatException e) {
                    log.warn("地市ID格式错误: {}", vo.getAreaId());
                }
            }

            if (StringUtils.isNotBlank(vo.getCountyId())) {
                try {
                    Long countyId = Long.valueOf(vo.getCountyId());
                    String countyName = countyMap.get(countyId);
                    if (countyName != null) {
                        vo.setCountyId(countyName);
                    }
                } catch (NumberFormatException e) {
                    log.warn("县分ID格式错误: {}", vo.getCountyId());
                }
            }

            if (StringUtils.isNotBlank(vo.getIndustryMajorKind())) {
                String industryName = industryMajorMap.get(vo.getIndustryMajorKind());
                if (industryName != null) {
                    vo.setIndustryMajorKind(industryName);
                }
            }

            if (StringUtils.isNotBlank(vo.getIndustrySubKind())) {
                String industryName = industrySubMap.get(vo.getIndustrySubKind());
                if (industryName != null) {
                    vo.setIndustrySubKind(industryName);
                }
            }

            if (StringUtils.isNotBlank(vo.getIndustryDetailKind())) {
                String industryName = industryDetailMap.get(vo.getIndustryDetailKind());
                if (industryName != null) {
                    vo.setIndustryDetailKind(industryName);
                }
            }
        }
    }

    /**
     * 根据 sys_param 表的配置决定是否对经理姓名进行脱敏
     *
     * @param list 需要处理的数据列表
     */
    private void applyDesensitizationIfNeeded(List<ZqGrpGroupInfoVo2> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        try {
            // 查询脱敏开关配置
            SysParamDo sysParamDo = sysParamService.findByCode("manager_info_desensitization_switch");

            // 如果配置不存在或 value 不是 "on"，则不进行脱敏
            if (sysParamDo == null || !"on".equalsIgnoreCase(sysParamDo.getValue())) {
                log.debug("经理信息脱敏开关未开启，不进行脱敏处理");
                return;
            }

            log.debug("经理信息脱敏开关已开启，开始脱敏处理");

            // 对列表中的每条数据进行脱敏处理
            for (ZqGrpGroupInfoVo2 vo : list) {
                // 脱敏实体客户经理姓名
                if (StringUtils.isNotBlank(vo.getCustManagerName())) {
                    vo.setCustManagerName(Desensitization.desensitizeName(vo.getCustManagerName()));
                }
            }
        } catch (Exception e) {
            log.error("经理信息脱敏处理失败: {}", e.getMessage(), e);
            // 脱敏失败不影响主流程，继续返回数据
        }
    }
    private LambdaQueryWrapper<ZqGrpGroupInfo> buildQueryWrapper(ZqGrpGroupInfoBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ZqGrpGroupInfo> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getGrpId() != null, ZqGrpGroupInfo::getGrpId, bo.getGrpId());
        lqw.like(StringUtils.isNotBlank(bo.getGrpName()), ZqGrpGroupInfo::getGrpName, bo.getGrpName());
        lqw.like(StringUtils.isNotBlank(bo.getGrpCode()), ZqGrpGroupInfo::getGrpCode, bo.getGrpCode());
        lqw.like(StringUtils.isNotBlank(bo.getHqGrpCode()), ZqGrpGroupInfo::getHqGrpCode, bo.getHqGrpCode());
        lqw.eq(StringUtils.isNotBlank(bo.getCustId()), ZqGrpGroupInfo::getCustId, bo.getCustId());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpLevel()), ZqGrpGroupInfo::getGrpLevel, bo.getGrpLevel());
        // 处理集团级别查询条件：如果 grplevel 为空则查询不等于 Y 的数据（包括 null 和空字符串），如果不为空则按参数查询
        if (StringUtils.isBlank(bo.getGrpLevel())) {
            lqw.and(wrapper -> wrapper
                .ne(ZqGrpGroupInfo::getGrpLevel, "Y")
                .or()
                .isNull(ZqGrpGroupInfo::getGrpLevel)
                .or()
                .eq(ZqGrpGroupInfo::getGrpLevel, "")
            );
        }
        lqw.eq(StringUtils.isNotBlank(bo.getGrpStatus()), ZqGrpGroupInfo::getGrpStatus, bo.getGrpStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getAreaId()), ZqGrpGroupInfo::getAreaId, bo.getAreaId());
        lqw.eq(StringUtils.isNotBlank(bo.getCountyId()), ZqGrpGroupInfo::getCountyId, bo.getCountyId());
        lqw.eq(StringUtils.isNotBlank(bo.getCountyId2()), ZqGrpGroupInfo::getCountyId2, bo.getCountyId2());
        lqw.eq(StringUtils.isNotBlank(bo.getGridId()), ZqGrpGroupInfo::getGridId, bo.getGridId());
        lqw.eq(StringUtils.isNotBlank(bo.getIndustryMajorKind()), ZqGrpGroupInfo::getIndustryMajorKind, bo.getIndustryMajorKind());
        lqw.eq(StringUtils.isNotBlank(bo.getIndustrySubKind()), ZqGrpGroupInfo::getIndustrySubKind, bo.getIndustrySubKind());
        lqw.eq(StringUtils.isNotBlank(bo.getIndustryDetailKind()), ZqGrpGroupInfo::getIndustryDetailKind, bo.getIndustryDetailKind());
        lqw.eq(StringUtils.isNotBlank(bo.getBusiType()), ZqGrpGroupInfo::getBusiType, bo.getBusiType());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpCustType()), ZqGrpGroupInfo::getGrpCustType, bo.getGrpCustType());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpExpandType()), ZqGrpGroupInfo::getGrpExpandType, bo.getGrpExpandType());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpBusiDeptType()), ZqGrpGroupInfo::getGrpBusiDeptType, bo.getGrpBusiDeptType());
        lqw.eq(StringUtils.isNotBlank(bo.getProvinceNewoldType()), ZqGrpGroupInfo::getProvinceNewoldType, bo.getProvinceNewoldType());
        lqw.eq(StringUtils.isNotBlank(bo.getManagerId()), ZqGrpGroupInfo::getManagerId, bo.getManagerId());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpCreator()), ZqGrpGroupInfo::getGrpCreator, bo.getGrpCreator());
        // 注意：这里移除了对 grpCreateTime 的精确匹配，因为时间范围查询在 queryPageList 方法中处理
        lqw.eq(StringUtils.isNotBlank(bo.getProvManagerEmpId()), ZqGrpGroupInfo::getProvManagerEmpId, bo.getProvManagerEmpId());

        // 实体客户经理OA账号模糊查询
        lqw.like(StringUtils.isNotBlank(bo.getCustManagerOauser()), ZqGrpGroupInfo::getCustManagerOauser, bo.getCustManagerOauser());

        lqw.like(StringUtils.isNotBlank(bo.getProvManagerName()), ZqGrpGroupInfo::getProvManagerName, bo.getProvManagerName());
        lqw.eq(StringUtils.isNotBlank(bo.getCityManagerEmpId()), ZqGrpGroupInfo::getCityManagerEmpId, bo.getCityManagerEmpId());
        lqw.like(StringUtils.isNotBlank(bo.getCityManagerName()), ZqGrpGroupInfo::getCityManagerName, bo.getCityManagerName());
        lqw.eq(StringUtils.isNotBlank(bo.getCountyManagerEmpId()), ZqGrpGroupInfo::getCountyManagerEmpId, bo.getCountyManagerEmpId());
        lqw.like(StringUtils.isNotBlank(bo.getCountyManagerName()), ZqGrpGroupInfo::getCountyManagerName, bo.getCountyManagerName());
        lqw.eq(bo.getCreateTime() != null, ZqGrpGroupInfo::getCreateTime, bo.getCreateTime());
        lqw.eq(bo.getUpdateTime() != null, ZqGrpGroupInfo::getUpdateTime, bo.getUpdateTime());

        // 添加时间范围查询条件
        // 处理分配时间范围查询
        if (bo.getAssignTimeStart() != null && bo.getAssignTimeEnd() != null) {
            lqw.between(ZqGrpGroupInfo::getGrpCreateTime, bo.getAssignTimeStart(), bo.getAssignTimeEnd());
        } else if (bo.getAssignTimeStart() != null) {
            lqw.ge(ZqGrpGroupInfo::getGrpCreateTime, bo.getAssignTimeStart());
        } else if (bo.getAssignTimeEnd() != null) {
            lqw.le(ZqGrpGroupInfo::getGrpCreateTime, bo.getAssignTimeEnd());
        }

        // 处理创建时间范围查询
        if (bo.getCreateTimeStart() != null && bo.getCreateTimeEnd() != null) {
            lqw.between(ZqGrpGroupInfo::getGrpCreateTime, bo.getCreateTimeStart(), bo.getCreateTimeEnd());
        } else if (bo.getCreateTimeStart() != null) {
            lqw.ge(ZqGrpGroupInfo::getGrpCreateTime, bo.getCreateTimeStart());
        } else if (bo.getCreateTimeEnd() != null) {
            lqw.le(ZqGrpGroupInfo::getGrpCreateTime, bo.getCreateTimeEnd());
        }

        // 数据权限处理 - 根据登录用户的区域代码进行数据权限过滤
//        LoginUser loginUser = LoginHelper.getLoginUser();
//        String areaCode = loginUser.getAreaCode();
//
//        if (StringUtils.isNotBlank(areaCode)) {
//            // 判断区域代码级别
//            if ("330000".equals(areaCode) || areaCode.endsWith("0000")) {
//                // 省级用户(330000)，可查看全省数据，不添加过滤条件
//                log.info("省级用户，查看全省数据");
//            } else if (areaCode.endsWith("00")) {
//                // 地市级用户(如330100)，只能查看本市数据
//                log.info("地市级用户，areaCode: {}", areaCode);
//                lqw.eq(ZqGrpGroupInfo::getAreaId, areaCode);
//            } else {
//                // 区县级用户(如330102)，只能查看本县数据
//                log.info("区县级用户，areaCode: {}", areaCode);
//                lqw.eq(ZqGrpGroupInfo::getCountyId, areaCode);
//            }
//        }
        LoginUser loginUser = LoginHelper.getLoginUser();
        SysUser user = sysUserService.selectUserById(loginUser.getUserId());
        SysDept dept = sysDeptService.selectDeptById(user.getDeptId());
        SysAreaVo area = sysAreaService.queryById(Long.valueOf(dept.getAreaCode()));
        String areaCode = String.valueOf(area.getId());
        String areaType = area.getType();

        if (StringUtils.isNotBlank(areaCode)) {
            if(areaCode.endsWith("0000") || "一级".equals(areaType)){
                // 省级用户，可查看全省数据，不添加过滤条件
                log.info("省级用户，查看全省数据");
            }else if("二级".equals(areaType)){
                // 地市级用户，只能查看本市数据
                log.info("地市级用户，areaCode: {}", areaCode);
                lqw.eq(ZqGrpGroupInfo::getAreaId, areaCode);
            }else{
                // 区县级用户，只能查看本县数据
                log.info("区县级用户，areaCode: {}", areaCode);
                lqw.eq(ZqGrpGroupInfo::getCountyId, areaCode);
            }
        }

        return lqw;
    }

    /**
     * 构建用于导出的查询条件包装器
     *
     * @param bo 查询条件
     * @param areaCode 区域代码
     * @return LambdaQueryWrapper
     */
    private LambdaQueryWrapper<ZqGrpGroupInfo> buildExportQueryWrapper(ZqGrpGroupInfoBo bo, String areaCode) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ZqGrpGroupInfo> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getGrpId() != null, ZqGrpGroupInfo::getGrpId, bo.getGrpId());
        lqw.like(StringUtils.isNotBlank(bo.getGrpName()), ZqGrpGroupInfo::getGrpName, bo.getGrpName());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpCode()), ZqGrpGroupInfo::getGrpCode, bo.getGrpCode());
        lqw.eq(StringUtils.isNotBlank(bo.getHqGrpCode()), ZqGrpGroupInfo::getHqGrpCode, bo.getHqGrpCode());
        lqw.eq(StringUtils.isNotBlank(bo.getCustId()), ZqGrpGroupInfo::getCustId, bo.getCustId());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpLevel()), ZqGrpGroupInfo::getGrpLevel, bo.getGrpLevel());
        // 处理集团级别查询条件：如果 grplevel 为空则查询不等于 Y 的数据（包括 null 和空字符串），如果不为空则按参数查询
        if (StringUtils.isBlank(bo.getGrpLevel())) {
            lqw.and(wrapper -> wrapper
                .ne(ZqGrpGroupInfo::getGrpLevel, "Y")
                .or()
                .isNull(ZqGrpGroupInfo::getGrpLevel)
                .or()
                .eq(ZqGrpGroupInfo::getGrpLevel, "")
            );
        }
        lqw.eq(StringUtils.isNotBlank(bo.getGrpStatus()), ZqGrpGroupInfo::getGrpStatus, bo.getGrpStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getAreaId()), ZqGrpGroupInfo::getAreaId, bo.getAreaId());
        lqw.eq(StringUtils.isNotBlank(bo.getCountyId()), ZqGrpGroupInfo::getCountyId, bo.getCountyId());
        lqw.eq(StringUtils.isNotBlank(bo.getCountyId2()), ZqGrpGroupInfo::getCountyId2, bo.getCountyId2());
        lqw.eq(StringUtils.isNotBlank(bo.getGridId()), ZqGrpGroupInfo::getGridId, bo.getGridId());
        lqw.eq(StringUtils.isNotBlank(bo.getIndustryMajorKind()), ZqGrpGroupInfo::getIndustryMajorKind, bo.getIndustryMajorKind());
        lqw.eq(StringUtils.isNotBlank(bo.getIndustrySubKind()), ZqGrpGroupInfo::getIndustrySubKind, bo.getIndustrySubKind());
        lqw.eq(StringUtils.isNotBlank(bo.getIndustryDetailKind()), ZqGrpGroupInfo::getIndustryDetailKind, bo.getIndustryDetailKind());
        lqw.eq(StringUtils.isNotBlank(bo.getBusiType()), ZqGrpGroupInfo::getBusiType, bo.getBusiType());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpCustType()), ZqGrpGroupInfo::getGrpCustType, bo.getGrpCustType());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpExpandType()), ZqGrpGroupInfo::getGrpExpandType, bo.getGrpExpandType());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpBusiDeptType()), ZqGrpGroupInfo::getGrpBusiDeptType, bo.getGrpBusiDeptType());
        lqw.eq(StringUtils.isNotBlank(bo.getProvinceNewoldType()), ZqGrpGroupInfo::getProvinceNewoldType, bo.getProvinceNewoldType());
        lqw.eq(StringUtils.isNotBlank(bo.getManagerId()), ZqGrpGroupInfo::getManagerId, bo.getManagerId());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpCreator()), ZqGrpGroupInfo::getGrpCreator, bo.getGrpCreator());
        lqw.eq(StringUtils.isNotBlank(bo.getProvManagerEmpId()), ZqGrpGroupInfo::getProvManagerEmpId, bo.getProvManagerEmpId());
        lqw.in(bo.getGrpIds() != null && !bo.getGrpIds().isEmpty(), ZqGrpGroupInfo::getGrpId, bo.getGrpIds());

        // 实体客户经理OA账号模糊查询
        lqw.like(StringUtils.isNotBlank(bo.getCustManagerOauser()), ZqGrpGroupInfo::getCustManagerOauser, bo.getCustManagerOauser());

        lqw.like(StringUtils.isNotBlank(bo.getProvManagerName()), ZqGrpGroupInfo::getProvManagerName, bo.getProvManagerName());
        lqw.eq(StringUtils.isNotBlank(bo.getCityManagerEmpId()), ZqGrpGroupInfo::getCityManagerEmpId, bo.getCityManagerEmpId());
        lqw.like(StringUtils.isNotBlank(bo.getCityManagerName()), ZqGrpGroupInfo::getCityManagerName, bo.getCityManagerName());
        lqw.eq(StringUtils.isNotBlank(bo.getCountyManagerEmpId()), ZqGrpGroupInfo::getCountyManagerEmpId, bo.getCountyManagerEmpId());
        lqw.like(StringUtils.isNotBlank(bo.getCountyManagerName()), ZqGrpGroupInfo::getCountyManagerName, bo.getCountyManagerName());
        lqw.eq(bo.getCreateTime() != null, ZqGrpGroupInfo::getCreateTime, bo.getCreateTime());
        lqw.eq(bo.getUpdateTime() != null, ZqGrpGroupInfo::getUpdateTime, bo.getUpdateTime());

        // 添加时间范围查询条件
        // 处理创建时间范围查询
        if (bo.getCreateTimeStart() != null && bo.getCreateTimeEnd() != null) {
            lqw.between(ZqGrpGroupInfo::getCreateTime, bo.getCreateTimeStart(), bo.getCreateTimeEnd());
        } else if (bo.getCreateTimeStart() != null) {
            lqw.ge(ZqGrpGroupInfo::getCreateTime, bo.getCreateTimeStart());
        } else if (bo.getCreateTimeEnd() != null) {
            lqw.le(ZqGrpGroupInfo::getCreateTime, bo.getCreateTimeEnd());
        }

        // 数据权限处理 - 根据传入的areaCode进行数据权限过滤
        if (StringUtils.isNotBlank(areaCode)) {
            // 判断区域代码级别
            if ("330000".equals(areaCode) || areaCode.endsWith("0000")) {
                // 省级用户(330000)，可查看全省数据，不添加过滤条件
                log.info("省级用户导出，查看全省数据");
            } else if (areaCode.endsWith("00")) {
                // 地市级用户(如330100)，只能查看本市数据
                log.info("地市级用户导出，areaCode: {}", areaCode);
                lqw.eq(ZqGrpGroupInfo::getAreaId, areaCode);
            } else {
                // 区县级用户(如330102)，只能查看本县数据
                log.info("区县级用户导出，areaCode: {}", areaCode);
                lqw.eq(ZqGrpGroupInfo::getCountyId, areaCode);
            }
        }

        return lqw;
    }

    /**
     * 新增政企集团客户信息
     */
    @Override
    public Boolean insertByBo(ZqGrpGroupInfoBo bo) {
        ZqGrpGroupInfo add = BeanUtil.toBean(bo, ZqGrpGroupInfo.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setGrpId(add.getGrpId());
        }
        return flag;
    }

    /**
     * 修改政企集团客户信息
     */
    @Override
    public Boolean updateByBo(ZqGrpGroupInfoBo bo) {

        // 先获取变更前的数据
        ZqGrpGroupInfo originalInfo = baseMapper.selectById(bo.getGrpId());

        if (originalInfo == null) {
            return false;
        }

        zqGrpGroupLogService.recordChangeLog(originalInfo, "T1"); // T1-集团基本信息变更

        // 创建更新对象
        ZqGrpGroupInfo update = BeanUtil.toBean(bo, ZqGrpGroupInfo.class);

        // 清空不能修改的字段，避免使用前端传递的脱敏数据
        // 这些字段将保持数据库中的原始值不变
        update.setGrpName(null); // 集团名称不能修改
        update.setProvManagerName(null); // 省级客户经理名字不能修改
        update.setCityManagerName(null); // 市级客户经理名字不能修改
        update.setCountyManagerName(null); // 县级客户经理名字不能修改


        // 执行更新操作
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 修改政企集团客户信息
     */
    @Override
    public Boolean updateByBo1(ZqGrpGroupInfoBo bo) {

        // 先获取变更前的数据
        ZqGrpGroupInfo originalInfo = baseMapper.selectById(bo.getGrpId());

        if (originalInfo == null) {
            return false; // 如果找不到原始记录，直接返回失败
        }

        // 记录变更日志
        zqGrpGroupLogService.recordChangeLog(originalInfo, "T2");

        // 创建更新对象
        ZqGrpGroupInfo update = new ZqGrpGroupInfo();
        update.setGrpId(bo.getGrpId());

        // 只更新非空的客户经理信息字段
        boolean hasUpdateFields = false;
        if (bo.getCustManagerEmpId() != null) {
            update.setCustManagerEmpId(bo.getCustManagerEmpId());
            hasUpdateFields = true;
        }
        if (bo.getCustManagerOauser() != null) {
            update.setCustManagerOauser(bo.getCustManagerOauser());
            hasUpdateFields = true;
        }
        if (bo.getCustManagerName() != null) {
            update.setCustManagerName(bo.getCustManagerName());
            hasUpdateFields = true;
        }

        // 如果没有任何需要更新的字段，直接返回成功
        if (!hasUpdateFields) {
            return true;
        }

        update.setUpdateTime(new Date());
        // 执行更新操作

        return baseMapper.updateById(update) > 0;
    }

    /**
     * 比较两个对象的属性是否完全相同
     * @param obj1 第一个对象
     * @param obj2 第二个对象
     * @return 如果属性完全相同返回true，否则返回false
     */
    private boolean isObjectsEqual(Object obj1, Object obj2) {
        if (obj1 == obj2) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        if (obj1.getClass() != obj2.getClass()) {
            return false;
        }

        // 直接使用手动比较方式，比较所有可能被修改的属性
        ZqGrpGroupInfo info1 = (ZqGrpGroupInfo) obj1;
        ZqGrpGroupInfo info2 = (ZqGrpGroupInfo) obj2;

        // 比较所有可能被修改的属性
        return java.util.Objects.equals(info1.getGrpId(), info2.getGrpId()) &&
               java.util.Objects.equals(info1.getGrpCode(), info2.getGrpCode()) &&
               java.util.Objects.equals(info1.getHqGrpCode(), info2.getHqGrpCode()) &&
               java.util.Objects.equals(info1.getCustId(), info2.getCustId()) &&
               java.util.Objects.equals(info1.getGrpLevel(), info2.getGrpLevel()) &&
               java.util.Objects.equals(info1.getGrpStatus(), info2.getGrpStatus()) &&
               java.util.Objects.equals(info1.getAreaId(), info2.getAreaId()) &&
               java.util.Objects.equals(info1.getCountyId(), info2.getCountyId()) &&
               java.util.Objects.equals(info1.getCountyId2(), info2.getCountyId2()) &&
               java.util.Objects.equals(info1.getGridId(), info2.getGridId()) &&
               java.util.Objects.equals(info1.getIndustryMajorKind(), info2.getIndustryMajorKind()) &&
               java.util.Objects.equals(info1.getIndustrySubKind(), info2.getIndustrySubKind()) &&
               java.util.Objects.equals(info1.getIndustryDetailKind(), info2.getIndustryDetailKind()) &&
               java.util.Objects.equals(info1.getBusiType(), info2.getBusiType()) &&
               java.util.Objects.equals(info1.getGrpCustType(), info2.getGrpCustType()) &&
               java.util.Objects.equals(info1.getGrpExpandType(), info2.getGrpExpandType()) &&
               java.util.Objects.equals(info1.getGrpBusiDeptType(), info2.getGrpBusiDeptType()) &&
               java.util.Objects.equals(info1.getProvinceNewoldType(), info2.getProvinceNewoldType()) &&
               java.util.Objects.equals(info1.getManagerId(), info2.getManagerId()) &&
               java.util.Objects.equals(info1.getGrpCreator(), info2.getGrpCreator()) &&
               java.util.Objects.equals(info1.getGrpCreateTime(), info2.getGrpCreateTime()) &&
               java.util.Objects.equals(info1.getProvManagerEmpId(), info2.getProvManagerEmpId()) &&
               java.util.Objects.equals(info1.getProvManagerOauser(), info2.getProvManagerOauser()) &&
               java.util.Objects.equals(info1.getProvManagerName(), info2.getProvManagerName()) &&
               java.util.Objects.equals(info1.getCityManagerEmpId(), info2.getCityManagerEmpId()) &&
               java.util.Objects.equals(info1.getCityManagerOauser(), info2.getCityManagerOauser()) &&
               java.util.Objects.equals(info1.getCityManagerName(), info2.getCityManagerName()) &&
               java.util.Objects.equals(info1.getCountyManagerEmpId(), info2.getCountyManagerEmpId()) &&
               java.util.Objects.equals(info1.getCountyManagerOauser(), info2.getCountyManagerOauser()) &&
               java.util.Objects.equals(info1.getCountyManagerName(), info2.getCountyManagerName());
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ZqGrpGroupInfo entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除政企集团客户信息
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 获取政企集团客户信息相关字典数据
     */
    @Override
    public Map<String, List<SysDictData>> getDictData() {
        Map<String, List<SysDictData>> result = new HashMap<>();

        // 使用一个SQL查询获取所有需要的字段
        LambdaQueryWrapper<ZqGrpGroupInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.select(
            ZqGrpGroupInfo::getAreaId,
            ZqGrpGroupInfo::getCountyId,
            ZqGrpGroupInfo::getIndustryMajorKind,
            ZqGrpGroupInfo::getIndustrySubKind,
            ZqGrpGroupInfo::getGrpCustType,
            ZqGrpGroupInfo::getGrpLevel,
            ZqGrpGroupInfo::getGrpExpandType,
            ZqGrpGroupInfo::getGrpBusiDeptType,
            ZqGrpGroupInfo::getGrpStatus,
            ZqGrpGroupInfo::getProvManagerName,
            ZqGrpGroupInfo::getProvManagerOauser,
            ZqGrpGroupInfo::getCityManagerName,
            ZqGrpGroupInfo::getCityManagerOauser,
            ZqGrpGroupInfo::getCountyManagerName,
            ZqGrpGroupInfo::getCountyManagerOauser
        );

        List<ZqGrpGroupInfo> dataList = baseMapper.selectList(wrapper);

        // 使用Stream API处理数据并去重
        Map<String, Set<String>> distinctValues = new HashMap<>();

        dataList.forEach(item -> {
            // 地市
            addDistinctValue(distinctValues, "areaList", item.getAreaId());
            // 县市
            addDistinctValue(distinctValues, "countyList", item.getCountyId());
            // 行业大类
            addDistinctValue(distinctValues, "industryMajorList", item.getIndustryMajorKind());
            // 行业亚类
            addDistinctValue(distinctValues, "industrySubList", item.getIndustrySubKind());
            // 集团类别
            addDistinctValue(distinctValues, "grpCustTypeList", item.getGrpCustType());
            // 集团级别
            addDistinctValue(distinctValues, "grpLevelList", item.getGrpLevel());
            // 拓展客户类型
            addDistinctValue(distinctValues, "grpExpandTypeList", item.getGrpExpandType());
            // 客户归属分类
            addDistinctValue(distinctValues, "grpBusiDeptTypeList", item.getGrpBusiDeptType());
            // 集团状态
            addDistinctValue(distinctValues, "grpStatusList", item.getGrpStatus());

            // 省级经理
            addDistinctValue(distinctValues, "provManagerNameList", item.getProvManagerName());
            addDistinctValue(distinctValues, "provManagerOauserList", item.getProvManagerOauser());

            // 市级经理
            addDistinctValue(distinctValues, "cityManagerNameList", item.getCityManagerName());
            addDistinctValue(distinctValues, "cityManagerOauserList", item.getCityManagerOauser());

            // 县级经理
            addDistinctValue(distinctValues, "countyManagerNameList", item.getCountyManagerName());
            addDistinctValue(distinctValues, "countyManagerOauserList", item.getCountyManagerOauser());
        });

        // 转换为SysDictData列表
        distinctValues.forEach((key, values) -> {
            List<SysDictData> dictList = values.stream()
                .filter(StringUtils::isNotBlank)
                .map(value -> {
                    SysDictData dictData = new SysDictData();
                    dictData.setDictValue(value);
                    dictData.setDictLabel(value);
                    return dictData;
                })
                .collect(Collectors.toList());
            result.put(key, dictList);
        });

        return result;
    }

    @Override
    public String getUserByStt() {
        String stt = "";
        LoginUser loginUser = LoginHelper.getLoginUser();
        Integer deptLevel =  repSysPersonNewDMapper.getDeptLevel(loginUser.getUsername());
        if(deptLevel>=1){
            stt= GrpExpandTypeenum.PROVINCE_ZT.getCode();
        }
        return stt;
    }

    /**
     * 添加去重值到Map中
     */
    private void addDistinctValue(Map<String, Set<String>> map, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            map.computeIfAbsent(key, k -> new HashSet<>()).add(value);
        }
    }

    @Override
    public ZqGrpGroupInfo selectByHqGrpCode(String hqGrpCode) {
        return baseMapper.selectByHqGrpCode(hqGrpCode);
    }
    /**
     * 根据areaCode分页查询政企集团客户信息列表（用于流式导出）
     *
     * @param bo 查询条件
     * @param page 分页参数
     * @param areaCode 区域代码
     * @return 政企集团客户信息列表
     */
    @Override
    public List<ZqGrpGroupInfoVo2> exportPageList(ZqGrpGroupInfoBo bo, Page page, String areaCode) {
        // 构建查询条件
        LambdaQueryWrapper<ZqGrpGroupInfo> lqw = buildExportQueryWrapper(bo, areaCode);


        List<ZqGrpGroupInfoVo2> resultList;
        if (page != null) {
            // 执行分页查询，使用 ZqGrpGroupInfoVo2（没有 @Sensitive 注解）
            IPage<ZqGrpGroupInfoVo2> resultPage = baseMapper.selectVoPage(page, lqw, ZqGrpGroupInfoVo2.class);
            resultList = resultPage.getRecords();
        } else {
            // 执行全量查询
            resultList = baseMapper.selectVoList(lqw, ZqGrpGroupInfoVo2.class);
        }

        // 进行字典翻译
        translateDictLabelsForVo2(resultList);

        return resultList;
    }
    private void translateDictLabels(List<ZqGrpGroupInfoVo> list) {

        // 应用翻译结果
        for (ZqGrpGroupInfoVo vo : list) {
            if (StringUtils.isNotBlank(vo.getAreaId())) {
                try {
                    Long areaId = Long.valueOf(vo.getAreaId());
                    SysAreaVo area = sysAreaService.queryById(areaId);
                    if (area != null) {
                        vo.setAreaId(area.getName());
                    }
                } catch (NumberFormatException e) {
                    log.warn("地市ID格式错误: {}", vo.getAreaId());
                }
            }

            if (StringUtils.isNotBlank(vo.getCountyId())) {
                try {
                    Long countyId = Long.valueOf(vo.getCountyId());
                    SysAreaVo county = sysAreaService.queryById(countyId);
                    if (county != null) {
                        vo.setCountyId(county.getName());
                    }
                } catch (NumberFormatException e) {
                    log.warn("县分ID格式错误: {}", vo.getCountyId());
                }
            }

            if (StringUtils.isNotBlank(vo.getIndustryMajorKind())) {
                SysCategoryVo industryMajorKind = sysCategoryService.queryById(vo.getIndustryMajorKind());
                if (industryMajorKind != null) {
                    vo.setIndustryMajorKind(industryMajorKind.getName());
                }
            }

            if (StringUtils.isNotBlank(vo.getIndustrySubKind())) {
                SysCategoryVo industrySubKind = sysCategoryService.queryById(vo.getIndustrySubKind());
                if (industrySubKind != null) {
                    vo.setIndustrySubKind(industrySubKind.getName());
                }
            }

            if (StringUtils.isNotBlank(vo.getIndustryDetailKind())) {
                SysCategoryVo industryDetailKind = sysCategoryService.queryById(vo.getIndustryDetailKind());
                if (industryDetailKind != null) {
                    vo.setIndustryDetailKind(industryDetailKind.getName());
                }
            }
        }
    }

    /**
     * 对 ZqGrpGroupInfoVo2 进行字典翻译
     */
    private void translateDictLabelsForVo2(List<ZqGrpGroupInfoVo2> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 应用翻译结果
        for (ZqGrpGroupInfoVo2 vo : list) {
            if (StringUtils.isNotBlank(vo.getAreaId())) {
                try {
                    Long areaId = Long.valueOf(vo.getAreaId());
                    SysAreaVo area = sysAreaService.queryById(areaId);
                    if (area != null) {
                        vo.setAreaId(area.getName());
                    }
                } catch (NumberFormatException e) {
                    log.warn("地市ID格式错误: {}", vo.getAreaId());
                }
            }

            if (StringUtils.isNotBlank(vo.getCountyId())) {
                try {
                    Long countyId = Long.valueOf(vo.getCountyId());
                    SysAreaVo county = sysAreaService.queryById(countyId);
                    if (county != null) {
                        vo.setCountyId(county.getName());
                    }
                } catch (NumberFormatException e) {
                    log.warn("县分ID格式错误: {}", vo.getCountyId());
                }
            }

            if (StringUtils.isNotBlank(vo.getIndustryMajorKind())) {
                SysCategoryVo industryMajorKind = sysCategoryService.queryById(vo.getIndustryMajorKind());
                if (industryMajorKind != null) {
                    vo.setIndustryMajorKind(industryMajorKind.getName());
                }
            }

            if (StringUtils.isNotBlank(vo.getIndustrySubKind())) {
                SysCategoryVo industrySubKind = sysCategoryService.queryById(vo.getIndustrySubKind());
                if (industrySubKind != null) {
                    vo.setIndustrySubKind(industrySubKind.getName());
                }
            }

            if (StringUtils.isNotBlank(vo.getIndustryDetailKind())) {
                SysCategoryVo industryDetailKind = sysCategoryService.queryById(vo.getIndustryDetailKind());
                if (industryDetailKind != null) {
                    vo.setIndustryDetailKind(industryDetailKind.getName());
                }
            }
        }
    }
}
