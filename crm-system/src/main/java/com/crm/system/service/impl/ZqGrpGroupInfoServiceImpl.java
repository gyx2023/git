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
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.domain.vo.ZqGrpGroupInfoVo;
import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.mapper.ZqGrpGroupInfoMapper;
import com.crm.system.service.IZqGrpGroupInfoService;
import com.crm.common.core.domain.entity.SysDictData;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;


import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * 政企集团客户信息Service业务层处理
 *
 * @author crm
 * @date 2025-11-20
 */
@RequiredArgsConstructor
@Service
public class ZqGrpGroupInfoServiceImpl implements IZqGrpGroupInfoService {

    private final ZqGrpGroupInfoMapper baseMapper;

    /**
     * 查询政企集团客户信息
     */
    @Override
    public ZqGrpGroupInfoVo queryById(Long grpId){
        return baseMapper.selectVoById(grpId);
    }

    /**
     * 查询政企集团客户信息列表
     */
    @Override
    public TableDataInfo<ZqGrpGroupInfoVo> queryPageList(ZqGrpGroupInfoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ZqGrpGroupInfo> lqw = buildQueryWrapper(bo);
        // 打印SQL查询语句和参数，用于调试
        System.out.println("查询SQL: " + lqw.getCustomSqlSegment());
        System.out.println("完整SQL: " + lqw.getTargetSql());
        System.out.println("assignTimeStart: " + bo.getAssignTimeStart());
        System.out.println("assignTimeEnd: " + bo.getAssignTimeEnd());
        System.out.println("createTimeStart: " + bo.getCreateTimeStart());
        System.out.println("createTimeEnd: " + bo.getCreateTimeEnd());

        // 添加日志查看MyBatis-Plus执行的SQL
        System.out.println("参数表达式: " + lqw.getParamNameValuePairs());

        Page<ZqGrpGroupInfoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询政企集团客户信息列表
     */
    @Override
    public List<ZqGrpGroupInfoVo> queryList(ZqGrpGroupInfoBo bo) {
        LambdaQueryWrapper<ZqGrpGroupInfo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

/**
 * 构建查询条件包装器
 * 根据传入的业务对象(bo)中的参数，构建LambdaQueryWrapper用于数据库查询条件组装
 * @param bo 包含查询参数的业务对象
 * @return 返回组装好的LambdaQueryWrapper对象
 */
    private LambdaQueryWrapper<ZqGrpGroupInfo> buildQueryWrapper(ZqGrpGroupInfoBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ZqGrpGroupInfo> lqw = Wrappers.lambdaQuery();


    // 精确查询条件 - 使用eq方法，当参数不为空时添加查询条件
        lqw.eq(bo.getGrpId() != null, ZqGrpGroupInfo::getGrpId, bo.getGrpId()); // 群组ID


    // 模糊查询条件 - 使用like方法，当参数不为空时添加查询条件
        lqw.like(StringUtils.isNotBlank(bo.getGrpName()), ZqGrpGroupInfo::getGrpName, bo.getGrpName()); // 群组名称


    // 以下均为精确查询条件，根据业务对象中的各个字段值是否为空来决定是否添加查询条件
        lqw.eq(StringUtils.isNotBlank(bo.getGrpCode()), ZqGrpGroupInfo::getGrpCode, bo.getGrpCode()); // 群组编码
        lqw.eq(StringUtils.isNotBlank(bo.getHqGrpCode()), ZqGrpGroupInfo::getHqGrpCode, bo.getHqGrpCode()); // 总部群组编码
        lqw.eq(StringUtils.isNotBlank(bo.getCustId()), ZqGrpGroupInfo::getCustId, bo.getCustId()); // 客户ID
        lqw.eq(StringUtils.isNotBlank(bo.getGrpLevel()), ZqGrpGroupInfo::getGrpLevel, bo.getGrpLevel()); // 群组级别
        lqw.eq(StringUtils.isNotBlank(bo.getGrpStatus()), ZqGrpGroupInfo::getGrpStatus, bo.getGrpStatus()); // 群组状态
        lqw.eq(StringUtils.isNotBlank(bo.getAreaId()), ZqGrpGroupInfo::getAreaId, bo.getAreaId()); // 区域ID
        lqw.eq(StringUtils.isNotBlank(bo.getCountyId()), ZqGrpGroupInfo::getCountyId, bo.getCountyId()); // 县区ID
        lqw.eq(StringUtils.isNotBlank(bo.getCountyId2()), ZqGrpGroupInfo::getCountyId2, bo.getCountyId2()); // 县区ID2
        lqw.eq(StringUtils.isNotBlank(bo.getGridId()), ZqGrpGroupInfo::getGridId, bo.getGridId()); // 网格ID
        lqw.eq(StringUtils.isNotBlank(bo.getIndustryMajorKind()), ZqGrpGroupInfo::getIndustryMajorKind, bo.getIndustryMajorKind()); // 行业大类
        lqw.eq(StringUtils.isNotBlank(bo.getIndustrySubKind()), ZqGrpGroupInfo::getIndustrySubKind, bo.getIndustrySubKind()); // 行业中类
        lqw.eq(StringUtils.isNotBlank(bo.getIndustryDetailKind()), ZqGrpGroupInfo::getIndustryDetailKind, bo.getIndustryDetailKind()); // 行业小类
        lqw.eq(StringUtils.isNotBlank(bo.getBusiType()), ZqGrpGroupInfo::getBusiType, bo.getBusiType()); // 业务类型
        lqw.eq(StringUtils.isNotBlank(bo.getGrpCustType()), ZqGrpGroupInfo::getGrpCustType, bo.getGrpCustType()); // 群组客户类型
        lqw.eq(StringUtils.isNotBlank(bo.getGrpExpandType()), ZqGrpGroupInfo::getGrpExpandType, bo.getGrpExpandType()); // 群组拓展类型
        lqw.eq(StringUtils.isNotBlank(bo.getGrpBusiDeptType()), ZqGrpGroupInfo::getGrpBusiDeptType, bo.getGrpBusiDeptType()); // 群组业务部门类型
        lqw.eq(StringUtils.isNotBlank(bo.getProvinceNewoldType()), ZqGrpGroupInfo::getProvinceNewoldType, bo.getProvinceNewoldType()); // 省份新旧类型
        lqw.eq(StringUtils.isNotBlank(bo.getManagerId()), ZqGrpGroupInfo::getManagerId, bo.getManagerId()); // 负责人ID
        lqw.eq(StringUtils.isNotBlank(bo.getGrpCreator()), ZqGrpGroupInfo::getGrpCreator, bo.getGrpCreator()); // 群组创建人


    // 时间查询条件
        lqw.eq(bo.getGrpCreateTime() != null, ZqGrpGroupInfo::getGrpCreateTime, bo.getGrpCreateTime()); // 群组创建时间


    // 省级经理员工ID查询条件
        lqw.eq(StringUtils.isNotBlank(bo.getProvManagerEmpId()), ZqGrpGroupInfo::getProvManagerEmpId, bo.getProvManagerEmpId()); // 省级经理员工ID
        // 市级经理员工ID查询条件
        lqw.eq(StringUtils.isNotBlank(bo.getCityManagerEmpId()), ZqGrpGroupInfo::getCityManagerEmpId, bo.getCityManagerEmpId()); // 市级经理员工ID
        // 县区级经理员工ID查询条件
        lqw.eq(StringUtils.isNotBlank(bo.getCountyManagerEmpId()), ZqGrpGroupInfo::getCountyManagerEmpId, bo.getCountyManagerEmpId()); // 县区级经理员工ID

        // 处理三个OA账号查询条件，使用OR关系连接
        boolean hasOaCondition = false;
        
        // 如果有OA相关条件，则添加到主查询条件中
        if (StringUtils.isNotBlank(bo.getProvManagerOauser()) || 
            StringUtils.isNotBlank(bo.getProvManagerName()) || 
            StringUtils.isNotBlank(bo.getCityManagerOauser()) || 
            StringUtils.isNotBlank(bo.getCityManagerName()) || 
            StringUtils.isNotBlank(bo.getCountyManagerOauser()) || 
            StringUtils.isNotBlank(bo.getCountyManagerName())) {
            
            hasOaCondition = true;
            
            // 使用单个and条件包裹所有OR条件，并显式指定Consumer类型
            lqw.and((Consumer<LambdaQueryWrapper<ZqGrpGroupInfo>>) wrapper -> {
                // 省级经理条件
                if (StringUtils.isNotBlank(bo.getProvManagerOauser()) && StringUtils.isNotBlank(bo.getProvManagerName())) {
                    wrapper.or(w -> w.like(ZqGrpGroupInfo::getProvManagerOauser, bo.getProvManagerOauser())
                                     .or()
                                     .like(ZqGrpGroupInfo::getProvManagerName, bo.getProvManagerName()));
                } else if (StringUtils.isNotBlank(bo.getProvManagerOauser())) {
                    wrapper.or().like(ZqGrpGroupInfo::getProvManagerOauser, bo.getProvManagerOauser());
                } else if (StringUtils.isNotBlank(bo.getProvManagerName())) {
                    wrapper.or().like(ZqGrpGroupInfo::getProvManagerName, bo.getProvManagerName());
                }
                
                // 市级经理条件
                if (StringUtils.isNotBlank(bo.getCityManagerOauser()) && StringUtils.isNotBlank(bo.getCityManagerName())) {
                    wrapper.or(w -> w.like(ZqGrpGroupInfo::getCityManagerOauser, bo.getCityManagerOauser())
                                     .or()
                                     .like(ZqGrpGroupInfo::getCityManagerName, bo.getCityManagerName()));
                } else if (StringUtils.isNotBlank(bo.getCityManagerOauser())) {
                    wrapper.or().like(ZqGrpGroupInfo::getCityManagerOauser, bo.getCityManagerOauser());
                } else if (StringUtils.isNotBlank(bo.getCityManagerName())) {
                    wrapper.or().like(ZqGrpGroupInfo::getCityManagerName, bo.getCityManagerName());
                }
                
                // 县区级经理条件
                if (StringUtils.isNotBlank(bo.getCountyManagerOauser()) && StringUtils.isNotBlank(bo.getCountyManagerName())) {
                    wrapper.or(w -> w.like(ZqGrpGroupInfo::getCountyManagerOauser, bo.getCountyManagerOauser())
                                     .or()
                                     .like(ZqGrpGroupInfo::getCountyManagerName, bo.getCountyManagerName()));
                } else if (StringUtils.isNotBlank(bo.getCountyManagerOauser())) {
                    wrapper.or().like(ZqGrpGroupInfo::getCountyManagerOauser, bo.getCountyManagerOauser());
                } else if (StringUtils.isNotBlank(bo.getCountyManagerName())) {
                    wrapper.or().like(ZqGrpGroupInfo::getCountyManagerName, bo.getCountyManagerName());
                }
            });
        }


    // 创建时间和更新时间查询条件
        lqw.eq(bo.getCreateTime() != null, ZqGrpGroupInfo::getCreateTime, bo.getCreateTime()); // 创建时间
        lqw.eq(bo.getUpdateTime() != null, ZqGrpGroupInfo::getUpdateTime, bo.getUpdateTime()); // 更新时间

        // 处理时间范围查询参数
        // 分配时间范围查询
        lqw.between(bo.getAssignTimeStart() != null && bo.getAssignTimeEnd() != null, 
            ZqGrpGroupInfo::getGrpCreateTime, bo.getAssignTimeStart(), bo.getAssignTimeEnd());
        lqw.ge(bo.getAssignTimeStart() != null && bo.getAssignTimeEnd() == null, 
            ZqGrpGroupInfo::getGrpCreateTime, bo.getAssignTimeStart());
        lqw.le(bo.getAssignTimeStart() == null && bo.getAssignTimeEnd() != null, 
            ZqGrpGroupInfo::getGrpCreateTime, bo.getAssignTimeEnd());

        // 创建时间范围查询
        lqw.between(bo.getCreateTimeStart() != null && bo.getCreateTimeEnd() != null, 
            ZqGrpGroupInfo::getCreateTime, bo.getCreateTimeStart(), bo.getCreateTimeEnd());
        lqw.ge(bo.getCreateTimeStart() != null && bo.getCreateTimeEnd() == null, 
            ZqGrpGroupInfo::getCreateTime, bo.getCreateTimeStart());
        lqw.le(bo.getCreateTimeStart() == null && bo.getCreateTimeEnd() != null, 
            ZqGrpGroupInfo::getCreateTime, bo.getCreateTimeEnd());

    // 返回组装好的查询条件包装器
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
        ZqGrpGroupInfo update = BeanUtil.toBean(bo, ZqGrpGroupInfo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
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

}
