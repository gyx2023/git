package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.core.domain.entity.SysDictData;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.generator.util.Desensitization;
import com.crm.system.service.IZqGrpGroupInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.vo.ZqGrpGroupInfoVo;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.domain.vo.ZqGrpGroupInfoVo;
import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.mapper.ZqGrpGroupInfoMapper;
import com.crm.system.service.IZqGrpGroupLogService;
import org.springframework.web.bind.annotation.RequestParam;


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
@Service
public class ZqGrpGroupInfoServiceImpl implements IZqGrpGroupInfoService {

    private final ZqGrpGroupInfoMapper baseMapper;
    private final IZqGrpGroupLogService zqGrpGroupLogService;

    /**
     * 查询政企集团客户信息
     */
    @Override
    public ZqGrpGroupInfoVo queryById(Long grpId){
        ZqGrpGroupInfoVo vo = baseMapper.selectVoById(grpId);
        return desensitizeVo(vo);
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
    public TableDataInfo<ZqGrpGroupInfoVo> queryPageList(ZqGrpGroupInfoBo bo, PageQuery pageQuery, String grpCreateTimeBegin, String grpCreateTimeEnd) {
        LambdaQueryWrapper<ZqGrpGroupInfo> lqw = buildQueryWrapper(bo);

        // 处理时间范围参数
        if (grpCreateTimeBegin != null && !grpCreateTimeBegin.isEmpty() && grpCreateTimeEnd != null && !grpCreateTimeEnd.isEmpty()) {
            try {
                // 前端已传入完整的时间格式(yyyy-MM-dd HH:mm:ss)，直接解析使用
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date beginDate = sdf.parse(grpCreateTimeBegin);
                Date endDate = sdf.parse(grpCreateTimeEnd);
                System.out.println("成功应用时间范围查询: " + grpCreateTimeBegin + " 到 " + grpCreateTimeEnd);
                lqw.between(ZqGrpGroupInfo::getGrpCreateTime, beginDate, endDate);

            } catch (Exception e) {
                System.out.println("时间参数解析失败: " + e.getMessage());
            }
        }

        Page<ZqGrpGroupInfoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        // 对查询结果进行脱敏处理
        List<ZqGrpGroupInfoVo> desensitizedList = result.getRecords().stream()
                .map(this::desensitizeVo)
                .collect(Collectors.toList());
        result.setRecords(desensitizedList);
        return TableDataInfo.build(result);
    }



    /**
     * 查询政企集团客户信息列表
     */
    @Override
    public List<ZqGrpGroupInfoVo> queryList(ZqGrpGroupInfoBo bo) {
        LambdaQueryWrapper<ZqGrpGroupInfo> lqw = buildQueryWrapper(bo);
        List<ZqGrpGroupInfoVo> list = baseMapper.selectVoList(lqw);
        // 对查询结果进行脱敏处理
        return list.stream()
                .map(this::desensitizeVo)
                .collect(Collectors.toList());
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
        System.out.println("[DEBUG] updateByBo called with grpId: " + bo.getGrpId());

        // 先获取变更前的数据
        ZqGrpGroupInfo originalInfo = baseMapper.selectById(bo.getGrpId());
        if (originalInfo != null) {
            System.out.println("[DEBUG] Retrieved originalInfo with grpId: " + originalInfo.getGrpId() + ", grpName: " + originalInfo.getGrpName());
        } else {
            System.out.println("[DEBUG] Failed to retrieve originalInfo for grpId: " + bo.getGrpId());
        }

        ZqGrpGroupInfo update = BeanUtil.toBean(bo, ZqGrpGroupInfo.class);

        validEntityBeforeSave(update);
        update.setCountyManagerName(null);
        System.out.println("[DEBUG] Created update object with grpId: " + update.getGrpId() + ", grpName: " + update.getGrpName());

        // 执行更新操作
        boolean success = baseMapper.updateById(update) > 0;
        System.out.println("[DEBUG] Update operation result: " + success);

        // 如果更新成功，记录变更日志
        if (success) {
            System.out.println("[DEBUG] Calling recordChangeLog for grpId: " + update.getGrpId());
            boolean logResult = zqGrpGroupLogService.recordChangeLog(originalInfo, update, "T1"); // T1-集团基本信息变更
            System.out.println("[DEBUG] recordChangeLog result: " + logResult);
        }

        return success;
    }

    /**
     * 修改政企集团客户信息
     */
    @Override
    public Boolean updateByBo1(ZqGrpGroupInfoBo bo) {
        System.out.println("[DEBUG] updateByBo called with grpId: " + bo.getGrpId());

        // 先获取变更前的数据
        ZqGrpGroupInfo originalInfo = baseMapper.selectById(bo.getGrpId());
        if (originalInfo != null) {
            System.out.println("[DEBUG] Retrieved originalInfo with grpId: " + originalInfo.getGrpId() + ", grpName: " + originalInfo.getGrpName());
        } else {
            System.out.println("[DEBUG] Failed to retrieve originalInfo for grpId: " + bo.getGrpId());
            return false; // 如果找不到原始记录，直接返回失败
        }

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
            System.out.println("[DEBUG] No fields to update, returning success");
            return true;
        }

        System.out.println("[DEBUG] Created update object with grpId: " + update.getGrpId());

        update.setUpdateTime(new Date());
        // 执行更新操作
        Boolean success = baseMapper.updateById(update) > 0;
        System.out.println("[DEBUG] Update operation result: " + success);

        // 如果更新成功，记录变更日志
        if (success) {
            System.out.println("[DEBUG] Calling recordChangeLog for grpId: " + update.getGrpId());
            // 获取更新后的完整对象用于日志记录
            ZqGrpGroupInfo updatedInfo = baseMapper.selectById(bo.getGrpId());
            boolean logResult = zqGrpGroupLogService.recordChangeLog(originalInfo, updatedInfo, "T2"); // T2-集团客户经理变更
            System.out.println("[DEBUG] recordChangeLog result: " + logResult);
        }

        return success;
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
