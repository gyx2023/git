package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.dto.RoleDTO;
import com.crm.common.core.domain.entity.SysDictData;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.helper.LoginHelper;
import com.crm.common.utils.StringUtils;
import com.crm.generator.util.Desensitization;
import com.crm.system.domain.SysArea;
import com.crm.system.domain.SysCategory;
import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.domain.param.SysParamDo;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.domain.vo.SysCategoryVo;
import com.crm.system.domain.vo.ZqGrpGroupInfoVo;
import com.crm.system.mapper.SysAreaMapper;
import com.crm.system.mapper.SysCategoryMapper;
import com.crm.system.mapper.ZqGrpGroupInfoMapper;
import com.crm.system.mapper.param.SysParamMapper;
import com.crm.system.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.crm.common.constant.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

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
    private final IDwdRepSysPersonDService iDwdRepSysPersonDService;
    private final ISysCategoryService sysCategoryService; // 添加行业分类服务
    private final SysCategoryMapper sysCategoryMapper; // 添加行业分类Mapper
    @Resource
    private SysParamMapper sysParamMapper;

    private static final String SENSITIVE_ROLE = "SENSITIVE_ROLE";

    /**
     * 查询政企集团客户信息
     */
    @Override
    public ZqGrpGroupInfoVo queryById(Long grpId){
        ZqGrpGroupInfoVo vo = baseMapper.selectVoById(grpId);

        SysParamDo sysParamDO = sysParamMapper.selectOne(new LambdaQueryWrapper<SysParamDo>().eq(SysParamDo::getType, "SENSITIVE_ROLE"));
        List<String> sensitiveRoles = Arrays.asList(sysParamDO.getValue().split(","));
        LoginUser loginUser = LoginHelper.getLoginUser();
        List<RoleDTO> roles = loginUser.getRoles();
        boolean isSensitive = sensitiveRoles.stream()
            .anyMatch(roleStr -> roles.stream()
                .anyMatch(roleDTO -> roleStr.equals(String.valueOf(roleDTO.getRoleId()))));

        if (!isSensitive) {
            return desensitizeVo(vo);
        }
        return vo;
    }

    /**
     * 查询政企集团客户信息列表
     */
    @Override
    public TableDataInfo<ZqGrpGroupInfoVo> queryPageList(ZqGrpGroupInfoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ZqGrpGroupInfo> lqw = buildQueryWrapper(bo);

        Page<ZqGrpGroupInfoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);

        SysParamDo sysParamDO = sysParamMapper.selectOne(new LambdaQueryWrapper<SysParamDo>().eq(SysParamDo::getType, "SENSITIVE_ROLE"));
        List<String> sensitiveRoles = Arrays.asList(sysParamDO.getValue().split(","));
        LoginUser loginUser = LoginHelper.getLoginUser();
        List<RoleDTO> roles = loginUser.getRoles();
        boolean isSensitive = sensitiveRoles.stream()
                .anyMatch(roleStr -> roles.stream()
                        .anyMatch(roleDTO -> roleStr.equals(String.valueOf(roleDTO.getRoleId()))));
        // 根据用户角色决定是否对查询结果进行脱敏处理
        if (!isSensitive) {
            List<ZqGrpGroupInfoVo> desensitizedList = result.getRecords().stream()
                    .map(this::desensitizeVo)
                    .collect(Collectors.toList());
            result.setRecords(desensitizedList);
        }
        return TableDataInfo.build(result);
    }

    /**
     * 查询政企集团客户信息列表
     */
    @Override
    public TableDataInfo<ZqGrpGroupInfoVo> queryPageList(ZqGrpGroupInfoBo bo, PageQuery pageQuery, String grpCreateTimeBegin, String grpCreateTimeEnd) {
        LambdaQueryWrapper<ZqGrpGroupInfo> lqw = buildQueryWrapper(bo);

        // 处理时间范围参数
        if (grpCreateTimeBegin != null && !grpCreateTimeBegin.isEmpty() && grpCreateTimeEnd != null && !grpCreateTimeEnd.isEmpty()) {
            try {
                // 前端已传入完整的时间格式(yyyy-MM-dd HH:mm:ss)，直接解析使用
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date beginDate = sdf.parse(grpCreateTimeBegin);
                Date endDate = sdf.parse(grpCreateTimeEnd);
                lqw.between(ZqGrpGroupInfo::getCreateTime, beginDate, endDate);
            } catch (Exception e) {
                log.warn("时间参数解析失败: {}", e.getMessage());
            }
        }

        Page<ZqGrpGroupInfoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);

        SysParamDo sysParamDO = sysParamMapper.selectOne(new LambdaQueryWrapper<SysParamDo>().eq(SysParamDo::getType, "SENSITIVE_ROLE"));
        List<String> sensitiveRoles = Arrays.asList(sysParamDO.getValue().split(","));
        LoginUser loginUser = LoginHelper.getLoginUser();
        List<RoleDTO> roles = loginUser.getRoles();
        boolean isSensitive = sensitiveRoles.stream()
            .anyMatch(roleStr -> roles.stream()
                .anyMatch(roleDTO -> roleStr.equals(String.valueOf(roleDTO.getRoleId()))));

        // 根据用户角色决定是否对查询结果进行脱敏处理
        if (!isSensitive) {
            List<ZqGrpGroupInfoVo> desensitizedList = result.getRecords().stream()
                    .map(this::desensitizeVo)
                    .collect(Collectors.toList());
            result.setRecords(desensitizedList);
        }

        return TableDataInfo.build(result);
    }

    /**
     * 查询政企集团客户信息列表
     */
    @Override
    public List<ZqGrpGroupInfoVo> queryList(ZqGrpGroupInfoBo bo) {
        LambdaQueryWrapper<ZqGrpGroupInfo> lqw = buildQueryWrapper(bo);
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
        return exportList(bo, areaCode, true); // 默认需要脱敏
    }

    /**
     * 根据areaCode查询政企集团客户信息列表（用于异步任务导出）
     *
     * @param bo 查询条件
     * @param areaCode 区域代码
     * @param needDesensitize 是否需要脱敏
     * @return 政企集团客户信息列表
     */
    public List<ZqGrpGroupInfoVo> exportList(ZqGrpGroupInfoBo bo, String areaCode, boolean needDesensitize) {
        LambdaQueryWrapper<ZqGrpGroupInfo> lqw = buildExportQueryWrapper(bo, areaCode);

        // 获取总数量
        long totalCount = baseMapper.selectCount(lqw);

        if (totalCount == 0) {
            return new ArrayList<>();
        }

        // 分批处理，每批5000条
        int batchSize = 5000;
        List<ZqGrpGroupInfoVo> allResults = new ArrayList<>();

        for (int offset = 0; offset < totalCount; offset += batchSize) {
            // 使用分页查询获取当前批次的数据
            Page<ZqGrpGroupInfo> page = new Page<>((offset / batchSize) + 1, batchSize, false);
            Page<ZqGrpGroupInfoVo> resultPage = baseMapper.selectVoPage(page, lqw);
            List<ZqGrpGroupInfoVo> batchList = resultPage.getRecords();

            // 对当前批次数据进行字典翻译
            translateDictLabels(batchList);

            // 根据needDesensitize参数决定是否对当前批次数据进行脱敏处理
            if (needDesensitize) {
                batchList = batchList.stream()
                        .map(this::desensitizeVo)
                        .collect(Collectors.toList());
            }

            allResults.addAll(batchList);
        }

        return allResults;
    }

    /**
     * 对字典字段进行翻译
     * @param list 集团客户信息列表
     */
    private void translateDictLabels(List<ZqGrpGroupInfoVo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 收集所有需要翻译的ID
        Set<Long> areaIds = new HashSet<>(); // 包含地市和县分ID
        Set<String> industryIds = new HashSet<>(); // 行业大类、亚类、细类

        for (ZqGrpGroupInfoVo vo : list) {
            // 收集地市ID
            if (StringUtils.isNotBlank(vo.getAreaId())) {
                try {
                    areaIds.add(Long.valueOf(vo.getAreaId()));
                } catch (NumberFormatException e) {
                    log.warn("地市ID格式错误: {}", vo.getAreaId());
                }
            }

            // 收集县分ID
            if (StringUtils.isNotBlank(vo.getCountyId())) {
                try {
                    areaIds.add(Long.valueOf(vo.getCountyId()));
                } catch (NumberFormatException e) {
                    log.warn("县分ID格式错误: {}", vo.getCountyId());
                }
            }

            // 收集行业ID（大类、亚类、细类）
            if (StringUtils.isNotBlank(vo.getIndustryMajorKind())) {
                industryIds.add(vo.getIndustryMajorKind());
            }
            if (StringUtils.isNotBlank(vo.getIndustrySubKind())) {
                industryIds.add(vo.getIndustrySubKind());
            }
            if (StringUtils.isNotBlank(vo.getIndustryDetailKind())) {
                industryIds.add(vo.getIndustryDetailKind());
            }
        }

        // 批量查询所有地市/县分数据
        Map<Long, String> areaMap = new HashMap<>();
        if (!areaIds.isEmpty()) {
            // 使用Mapper进行批量查询，不用缓存
            List<SysArea> areaList = sysAreaMapper.selectBatchIds(areaIds);
            for (SysArea area : areaList) {
                areaMap.put(area.getId(), area.getName());
            }
        }

        // 批量查询所有行业数据
        Map<String, String> industryMap = new HashMap<>();
        if (!industryIds.isEmpty()) {
            // 使用Mapper进行批量查询，不用缓存
            List<SysCategory> industryList = sysCategoryMapper.selectBatchIds(industryIds);
            for (SysCategory category : industryList) {
                industryMap.put(category.getId(), category.getName());
            }
        }

        // 遍历列表，直接从Map中获取名称进行替换
        for (ZqGrpGroupInfoVo vo : list) {
            // 转换地市ID为名称
            if (StringUtils.isNotBlank(vo.getAreaId())) {
                try {
                    String areaName = areaMap.get(Long.valueOf(vo.getAreaId()));
                    if (areaName != null) {
                        vo.setAreaId(areaName);
                    }
                } catch (NumberFormatException e) {
                    log.warn("地市ID格式错误: {}", vo.getAreaId());
                }
            }

            // 转换县分ID为名称
            if (StringUtils.isNotBlank(vo.getCountyId())) {
                try {
                    String countyName = areaMap.get(Long.valueOf(vo.getCountyId()));
                    if (countyName != null) {
                        vo.setCountyId(countyName);
                    }
                } catch (NumberFormatException e) {
                    log.warn("县分ID格式错误: {}", vo.getCountyId());
                }
            }

            // 转换行业大类ID为名称
            if (StringUtils.isNotBlank(vo.getIndustryMajorKind())) {
                String industryName = industryMap.get(vo.getIndustryMajorKind());
                if (industryName != null) {
                    vo.setIndustryMajorKind(industryName);
                }
            }

            // 转换行业亚类ID为名称
            if (StringUtils.isNotBlank(vo.getIndustrySubKind())) {
                String industryName = industryMap.get(vo.getIndustrySubKind());
                if (industryName != null) {
                    vo.setIndustrySubKind(industryName);
                }
            }

            // 转换行业细类ID为名称
            if (StringUtils.isNotBlank(vo.getIndustryDetailKind())) {
                String industryName = industryMap.get(vo.getIndustryDetailKind());
                if (industryName != null) {
                    vo.setIndustryDetailKind(industryName);
                }
            }
        }
    }

    /**
     * 对ZqGrpGroupInfoVo对象进行脱敏处理
     * @param vo 要脱敏的ZqGrpGroupInfoVo对象
     * @return 脱敏后的ZqGrpGroupInfoVo对象
     */
    private ZqGrpGroupInfoVo desensitizeVo(ZqGrpGroupInfoVo vo) {
        if (vo == null) {
            return null;
        }

        // 对集团名称进行脱敏
        vo.setGrpName(Desensitization.desensitizeCompanyName(vo.getGrpName()));

        // 对省、市、县客户经理姓名进行脱敏
        vo.setProvManagerName(Desensitization.desensitizeName(vo.getProvManagerName()));
        vo.setCityManagerName(Desensitization.desensitizeName(vo.getCityManagerName()));
        vo.setCountyManagerName(Desensitization.desensitizeName(vo.getCountyManagerName()));

        return vo;
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

        // 构建 provManagerOauser、cityManagerOauser 和 countyManagerOauser 的 OR 条件
        String provManagerOauser = bo.getProvManagerOauser();
        String cityManagerOauser = bo.getCityManagerOauser();
        String countyManagerOauser = bo.getCountyManagerOauser();

        if (StringUtils.isNotBlank(provManagerOauser) || StringUtils.isNotBlank(cityManagerOauser) || StringUtils.isNotBlank(countyManagerOauser)) {
            lqw.and(wrapper -> {
                // 省级经理条件
                if (StringUtils.isNotBlank(provManagerOauser)) {
                    wrapper.or(w -> w.like(ZqGrpGroupInfo::getProvManagerOauser, provManagerOauser));
                }

                // 市级经理条件
                if (StringUtils.isNotBlank(cityManagerOauser)) {
                    wrapper.or(w -> w.like(ZqGrpGroupInfo::getCityManagerOauser, cityManagerOauser));
                }

                // 县区级经理条件
                if (StringUtils.isNotBlank(countyManagerOauser)) {
                    wrapper.or(w -> w.like(ZqGrpGroupInfo::getCountyManagerOauser, countyManagerOauser));
                }
            });
        }

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
            lqw.between(ZqGrpGroupInfo::getCreateTime, bo.getCreateTimeStart(), bo.getCreateTimeEnd());
        } else if (bo.getCreateTimeStart() != null) {
            lqw.ge(ZqGrpGroupInfo::getCreateTime, bo.getCreateTimeStart());
        } else if (bo.getCreateTimeEnd() != null) {
            lqw.le(ZqGrpGroupInfo::getCreateTime, bo.getCreateTimeEnd());
        }

        // 数据权限处理 - 根据传入的areaCode进行数据权限过滤
        LoginUser loginUser = LoginHelper.getLoginUser();
        String areaCode = loginUser.getAreaCode();
        // 如果不是省本级(330000)，则需要进行数据权限过滤
        if (!"330000".equals(areaCode)) {
            // 判断区域代码级别
            if (areaCode.endsWith("0000")) {
                log.info("省本级");
                // 省级用户(虽然前面已经排除了330000，但为了完整性保留此判断)
                // 不添加过滤条件，可查看全省数据
            } else if (areaCode.endsWith("00")) {
                // 地市级用户(如330100)，只能查看本市数据
                lqw.eq(ZqGrpGroupInfo::getAreaId, areaCode);
            } else {
                // 区县级用户(如330102)，可查看所属地市的所有数据
                // 提取地市代码(前4位+00)
                String cityCode = areaCode.substring(0, 4) + "00";
                lqw.eq(ZqGrpGroupInfo::getAreaId, cityCode);
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

        // 构建 provManagerOauser、cityManagerOauser 和 countyManagerOauser 的 OR 条件
        String provManagerOauser = bo.getProvManagerOauser();
        String cityManagerOauser = bo.getCityManagerOauser();
        String countyManagerOauser = bo.getCountyManagerOauser();

        if (StringUtils.isNotBlank(provManagerOauser) || StringUtils.isNotBlank(cityManagerOauser) || StringUtils.isNotBlank(countyManagerOauser)) {
            lqw.and(wrapper -> {
                // 省级经理条件
                if (StringUtils.isNotBlank(provManagerOauser)) {
                    wrapper.or(w -> w.eq(ZqGrpGroupInfo::getProvManagerOauser, provManagerOauser));
                }

                // 市级经理条件
                if (StringUtils.isNotBlank(cityManagerOauser)) {
                    wrapper.or(w -> w.eq(ZqGrpGroupInfo::getCityManagerOauser, cityManagerOauser));
                }

                // 县区级经理条件
                if (StringUtils.isNotBlank(countyManagerOauser)) {
                    wrapper.or(w -> w.eq(ZqGrpGroupInfo::getCountyManagerOauser, countyManagerOauser));
                }
            });
        }

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
        // 如果不是省本级(330000)，则需要进行数据权限过滤
        if (!"330000".equals(areaCode)) {
            // 判断区域代码级别
            if (areaCode.endsWith("0000")) {
                log.info("省本级用户导出");
                // 省级用户(虽然前面已经排除了330000，但为了完整性保留此判断)
                // 不添加过滤条件，可查看全省数据
            } else if (areaCode.endsWith("00")) {
                // 地市级用户(如330100)，只能查看本市数据
                lqw.eq(ZqGrpGroupInfo::getAreaId, areaCode);
            } else {
                // 区县级用户(如330102)，可查看所属地市的所有数据
                // 提取地市代码(前4位+00)
                String cityCode = areaCode.substring(0, 4) + "00";
                lqw.eq(ZqGrpGroupInfo::getAreaId, cityCode);
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
            return false; // 如果找不到原始记录，直接返回失败} else {
        }

        // 根据集团客户拓展类型进行校验
        String expandType = originalInfo.getGrpExpandType();
        if ("01".equals(expandType)) {
            // 省直拓 - 省级客户经理不能为空
            if (bo.getProvManagerOauser() == null || bo.getProvManagerName() == null) {
                throw new RuntimeException("集团客户拓展类型为省直拓，省级客户经理信息不能为空");
            }
        } else if ("02".equals(expandType)) {
            // 市直拓 - 市级客户经理不能为空
            if (bo.getCityManagerOauser() == null || bo.getCityManagerName() == null) {
                throw new RuntimeException("集团客户拓展类型为市直拓，市级客户经理信息不能为空");
            }
        } else if ("03".equals(expandType)) {
            // 县直拓 - 县级客户经理不能为空
            if (bo.getCountyManagerOauser() == null || bo.getCountyManagerName() == null) {
                throw new RuntimeException("集团客户拓展类型为县直拓，县级客户经理信息不能为空");
            }
        }

        // 记录变更日志
        zqGrpGroupLogService.recordChangeLog(originalInfo, "T2");

        // 创建更新对象
        ZqGrpGroupInfo update = new ZqGrpGroupInfo();
        update.setGrpId(bo.getGrpId());

        // 只更新非空的客户经理信息字段
        boolean hasUpdateFields = false;
        if (bo.getProvManagerOauser() != null) {
            update.setProvManagerOauser(bo.getProvManagerOauser());
            hasUpdateFields = true;
        }
        if (bo.getProvManagerName() != null) {
            update.setProvManagerName(bo.getProvManagerName());
            hasUpdateFields = true;
        }
        if (bo.getCityManagerOauser() != null) {
            update.setCityManagerOauser(bo.getCityManagerOauser());
            hasUpdateFields = true;
        }
        if (bo.getCityManagerName() != null) {
            update.setCityManagerName(bo.getCityManagerName());
            hasUpdateFields = true;
        }
        if (bo.getCountyManagerOauser() != null) {
            update.setCountyManagerOauser(bo.getCountyManagerOauser());
            hasUpdateFields = true;
        }
        if (bo.getCountyManagerName() != null) {
            update.setCountyManagerName(bo.getCountyManagerName());
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

}
