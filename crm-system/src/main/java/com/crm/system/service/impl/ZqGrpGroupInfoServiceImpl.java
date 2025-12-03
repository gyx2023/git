package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.core.domain.dto.RoleDTO;
import com.crm.common.core.domain.entity.SysDictData;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.helper.LoginHelper;
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
import com.crm.system.domain.bo.DwdRepSysPersonDBo;
import com.crm.system.domain.vo.DwdRepSysPersonDVo;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.service.IDwdRepSysPersonDService;
import com.crm.system.service.ISysAreaService;
import com.crm.common.helper.LoginHelper;

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
    private final IDwdRepSysPersonDService iDwdRepSysPersonDService;
    private final ISysAreaService iSysAreaService;

    /**
     * 查询政企集团客户信息
     */
    @Override
    public ZqGrpGroupInfoVo queryById(Long grpId){
        ZqGrpGroupInfoVo vo = baseMapper.selectVoById(grpId);
        // 获取当前用户角色，0为超级管理员
        String currentUserRole = LoginHelper.isAdmin() ? "0" : "1"; // 简化处理，后续可根据实际角色体系调整
        boolean isSensitive = !"0".equals(currentUserRole); // 0为超级管理员，不脱敏；其他角色脱敏
        if (isSensitive) {
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
        // 获取当前用户角色，0为超级管理员
        String currentUserRole = LoginHelper.isAdmin() ? "0" : "1"; // 简化处理，后续可根据实际角色体系调整
        boolean isSensitive = !"0".equals(currentUserRole); // 0为超级管理员，不脱敏；其他角色脱敏

        // 根据用户角色决定是否对查询结果进行脱敏处理
        if (isSensitive) {
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
                System.out.println("成功应用时间范围查询: " + grpCreateTimeBegin + " 到 " + grpCreateTimeEnd);
                lqw.between(ZqGrpGroupInfo::getGrpCreateTime, beginDate, endDate);

            } catch (Exception e) {
                System.out.println("时间参数解析失败: " + e.getMessage());
            }
        }

        Page<ZqGrpGroupInfoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        // 获取当前用户角色，0为超级管理员
        String currentUserRole = LoginHelper.isAdmin() ? "0" : "1"; // 简化处理，后续可根据实际角色体系调整
        boolean isSensitive = !"0".equals(currentUserRole); // 0为超级管理员，不脱敏；其他角色脱敏

        // 根据用户角色决定是否对查询结果进行脱敏处理
        if (isSensitive) {
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
        // 获取当前用户角色，0为超级管理员
        String currentUserRole = LoginHelper.isAdmin() ? "0" : "1"; // 简化处理，后续可根据实际角色体系调整
        boolean isSensitive = !"0".equals(currentUserRole); // 0为超级管理员，不脱敏；其他角色脱敏

        // 根据用户角色决定是否对查询结果进行脱敏处理
        if (isSensitive) {
            return list.stream()
                    .map(this::desensitizeVo)
                    .collect(Collectors.toList());
        }
        return list;
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
            lqw.between(ZqGrpGroupInfo::getCreateTime, bo.getAssignTimeStart(), bo.getAssignTimeEnd());
        }
        // 处理创建时间范围查询
        if (bo.getCreateTimeStart() != null && bo.getCreateTimeEnd() != null) {
            lqw.between(ZqGrpGroupInfo::getCreateTime, bo.getCreateTimeStart(), bo.getCreateTimeEnd());
        }

        //-------------------------------数据权限处理-------------------------------
        LoginUser loginUser = LoginHelper.getLoginUser();
        String areaCode = loginUser.getAreaCode();
        // 如果不是省本级(330000)，则需要进行数据权限过滤
        if (!"330000".equals(areaCode)) {
            // 判断区域代码级别
            if (areaCode.endsWith("0000")) {
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
        System.out.println("[DEBUG] updateByBo called with grpId: " + bo.getGrpId());

        // 先获取变更前的数据
        ZqGrpGroupInfo originalInfo = baseMapper.selectById(bo.getGrpId());
        if (originalInfo != null) {
            System.out.println("[DEBUG] Retrieved originalInfo with grpId: " + originalInfo.getGrpId() + ", grpName: " + originalInfo.getGrpName());
        } else {
            System.out.println("[DEBUG] Failed to retrieve originalInfo for grpId: " + bo.getGrpId());
            return false;
        }

        // 创建更新对象
        ZqGrpGroupInfo update = BeanUtil.toBean(bo, ZqGrpGroupInfo.class);

        validEntityBeforeSave(update);

        // 清空不能修改的字段，避免使用前端传递的脱敏数据
        // 这些字段将保持数据库中的原始值不变
        update.setGrpName(null); // 集团名称不能修改
        update.setProvManagerName(null); // 省级客户经理名字不能修改
        update.setCityManagerName(null); // 市级客户经理名字不能修改
        update.setCountyManagerName(null); // 县级客户经理名字不能修改

        System.out.println("[DEBUG] Created update object with grpId: " + update.getGrpId());

        // 执行更新操作
        boolean success = baseMapper.updateById(update) > 0;
        System.out.println("[DEBUG] Update operation result: " + success);

        // 如果更新成功，记录变更日志
        if (success) {
            System.out.println("[DEBUG] Calling recordChangeLog for grpId: " + update.getGrpId());
            // 重新从数据库获取更新后的完整数据，包含未脱敏的集团名称和客户经理名字
            ZqGrpGroupInfo updatedInfo = baseMapper.selectById(bo.getGrpId());

            // 比较修改前后的对象属性，只有当属性发生变化时才记录日志
            // 使用自定义方法比较对象属性是否相同
            boolean hasChanges = !isObjectsEqual(originalInfo, updatedInfo);
            if (hasChanges) {
                boolean logResult = zqGrpGroupLogService.recordChangeLog(originalInfo, updatedInfo, "T1"); // T1-集团基本信息变更
                System.out.println("[DEBUG] recordChangeLog result: " + logResult);
            } else {
                System.out.println("[DEBUG] No changes detected, skipping log record");
            }
        }

        return success;
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
