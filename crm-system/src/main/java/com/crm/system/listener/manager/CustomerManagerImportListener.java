package com.crm.system.listener.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.system.domain.dto.GrpGroupImportDTO;
import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.system.domain.DwdRepSysPersonD;
import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.domain.vo.CustomerManagerImportResultVo;
import com.crm.system.domain.vo.CustomerManagerTemplateVo;
import com.crm.system.domain.vo.DwdRepSysPersonDVo;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.domain.zbzq.RepSysPersonNewDDo;
import com.crm.system.mapper.DwdRepSysPersonDMapper;
import com.crm.system.mapper.zbzq.RepSysPersonNewDMapper;
import com.crm.system.service.ISysAreaService;
import com.crm.system.service.ISysDeptService;
import com.crm.system.service.ISysUserService;
import com.crm.system.service.IZqGrpGroupInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户经理导入监听器
 * 处理客户经理数据导入的具体业务逻辑
 *
 * @author wcf
 * @date 2025-12-01
 */
@Component
@Slf4j
public class CustomerManagerImportListener {
    @Autowired
    private RepSysPersonNewDMapper repSysPersonNewDMapper;
    @Autowired
    private DwdRepSysPersonDMapper baseMapper;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private ISysDeptService sysDeptService;
    @Autowired
    private ISysAreaService sysAreaService;
    @Autowired
    private IZqGrpGroupInfoService zqGrpGroupInfoService;

    public List<CustomerManagerImportResultVo> processImportData(List<CustomerManagerTemplateVo> dataList, Long createUserId) {
        List<GrpGroupImportDTO> canBindManagers = getCanBindManagers(createUserId);

        // 将可绑定的客户经理按OA用户名建立索引（统一转为小写），提高查找效率并实现大小写兼容
        Map<String, GrpGroupImportDTO> canBindManagerMap = canBindManagers.stream()
            .collect(Collectors.toMap(
                item -> item.getOaUserName().toLowerCase(),
                item -> item,
                (existing, replacement) -> existing // 如果有重复键，保留第一个
            ));

        List<CustomerManagerImportResultVo> results = new ArrayList<>();

        for (CustomerManagerTemplateVo templateVo : dataList) {
            CustomerManagerImportResultVo result = new CustomerManagerImportResultVo();
            result.setHqGrpCode(templateVo.getHqGrpCode());
            result.setCustManagerOauser(templateVo.getCustManagerOauser());

            // 检查权限：custManagerOauser 是否在 canBindManagers 的 oaUserName 里（大小写兼容）
            String oaUserNameLower = templateVo.getCustManagerOauser() != null ?
                templateVo.getCustManagerOauser().toLowerCase() : "";

            if (!canBindManagerMap.containsKey(oaUserNameLower)) {
                result.setProcessResult("失败");
                result.setErrorMessage("无权限绑定此客户经理或该经理不存在:" + templateVo.getCustManagerOauser());
                results.add(result);
                continue;
            }

            // 有权限，更新 zqzt_grp_group_info 表的 cust_manager 相关字段
            try {
                ZqGrpGroupInfo groupInfo = zqGrpGroupInfoService.selectByHqGrpCode(templateVo.getHqGrpCode());
                if (groupInfo == null) {
                    result.setProcessResult("失败");
                    result.setErrorMessage("未找到总部集团编码对应的数据:" + templateVo.getHqGrpCode());
                    results.add(result);
                    continue;
                }

                // 获取有权限的客户经理信息（使用小写key查找）
                GrpGroupImportDTO managerInfo = canBindManagerMap.get(oaUserNameLower);

                // 更新集团信息中的客户经理字段（OA账号统一使用小写）
                groupInfo.setCustManagerEmpId(managerInfo.getEmpId());
                groupInfo.setCustManagerOauser(managerInfo.getOaUserName().toLowerCase());
                groupInfo.setCustManagerName(managerInfo.getFullName());

                // 构建更新用的 BO 对象
                ZqGrpGroupInfoBo updateBo = new ZqGrpGroupInfoBo();
                updateBo.setGrpId(groupInfo.getGrpId());
                updateBo.setCustManagerEmpId(groupInfo.getCustManagerEmpId());
                updateBo.setCustManagerOauser(groupInfo.getCustManagerOauser());
                updateBo.setCustManagerName(groupInfo.getCustManagerName());

                // 执行更新操作
                Boolean updateSuccess = zqGrpGroupInfoService.updateByBo(updateBo);

                if (updateSuccess) {
                    result.setProcessResult("成功");
                    result.setErrorMessage("");
                } else {
                    result.setProcessResult("失败");
                    result.setErrorMessage("更新数据库失败");
                }
            } catch (Exception e) {
                log.error("处理客户经理导入时发生异常", e);
                result.setProcessResult("失败");
                result.setErrorMessage("系统异常:" + e.getMessage());
            }

            results.add(result);
        }

        return results;
    }

    /**
     * 获取可绑定的客户经理
     * @param createUserId
     * @return
     */
    private List<GrpGroupImportDTO> getCanBindManagers(Long createUserId) {
        List<GrpGroupImportDTO> canBindManagers = new ArrayList<>();

        SysUser user = userService.selectUserById(createUserId);
        Integer deptLevel = repSysPersonNewDMapper.getDeptLevel(user.getUserName());

        if(deptLevel >= 1){
            Page<RepSysPersonNewDDo> page = new Page<>(1, Integer.MAX_VALUE);
            Page<RepSysPersonNewDDo> managersPage = repSysPersonNewDMapper.selectProvincialManagers(page, null);
            List<RepSysPersonNewDDo> managers = managersPage.getRecords();
            for (RepSysPersonNewDDo manager : managers) {
                GrpGroupImportDTO dto = new GrpGroupImportDTO();
                dto.setEmpId(manager.getEmpId());
                dto.setOaUserName(manager.getOaUserName());
                dto.setFullName(manager.getFullName());
                canBindManagers.add(dto);
            }
            return canBindManagers;
        }

        SysDept dept = sysDeptService.selectDeptById(user.getDeptId());
        SysAreaVo area = sysAreaService.queryById(Long.valueOf(dept.getAreaCode()));

        if("一级".equals(area.getType())){
            LambdaQueryWrapper<DwdRepSysPersonD> lqw = Wrappers.lambdaQuery();
            lqw.eq(DwdRepSysPersonD::getOnjobFlag, 0);
            List<DwdRepSysPersonDVo> managers = baseMapper.selectVoList(lqw);
            for (DwdRepSysPersonDVo manager : managers) {
                GrpGroupImportDTO dto = new GrpGroupImportDTO();
                dto.setEmpId(manager.getEmpId());
                dto.setOaUserName(manager.getOaUserName());
                dto.setFullName(manager.getFullName());
                canBindManagers.add(dto);
            }
            return canBindManagers;
        }else if("二级".equals(area.getType())){
            LambdaQueryWrapper<DwdRepSysPersonD> lqw = Wrappers.lambdaQuery();
            lqw.eq(DwdRepSysPersonD::getAreaId, area.getEmpAreaId())
                .eq(DwdRepSysPersonD::getOnjobFlag, 0);
            List<DwdRepSysPersonDVo> managers = baseMapper.selectVoList(lqw);
            for (DwdRepSysPersonDVo manager : managers) {
                GrpGroupImportDTO dto = new GrpGroupImportDTO();
                dto.setEmpId(manager.getEmpId());
                dto.setOaUserName(manager.getOaUserName());
                dto.setFullName(manager.getFullName());
                canBindManagers.add(dto);
            }
            return canBindManagers;
        }else if("三级".equals(area.getType())){
            LambdaQueryWrapper<DwdRepSysPersonD> lqw = Wrappers.lambdaQuery();
            lqw.eq(DwdRepSysPersonD::getCountyId, area.getEmpCountyId())
                .eq(DwdRepSysPersonD::getOnjobFlag, 0);
            List<DwdRepSysPersonDVo> managers = baseMapper.selectVoList(lqw);
            for (DwdRepSysPersonDVo manager : managers) {
                GrpGroupImportDTO dto = new GrpGroupImportDTO();
                dto.setEmpId(manager.getEmpId());
                dto.setOaUserName(manager.getOaUserName());
                dto.setFullName(manager.getFullName());
                canBindManagers.add(dto);
            }
            return canBindManagers;
        }

        return canBindManagers;
    }
}
