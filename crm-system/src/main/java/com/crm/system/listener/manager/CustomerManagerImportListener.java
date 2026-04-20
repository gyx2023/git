package com.crm.system.listener.manager;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.common.core.domain.dto.GrpGroupImportDTO;
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
import com.crm.system.service.IDwdRepSysPersonDService;
import com.crm.system.service.ISysAreaService;
import com.crm.system.service.ISysDeptService;
import com.crm.system.service.ISysUserService;
import com.crm.system.service.IZqGrpGroupInfoService;
import com.crm.system.service.impl.SysDeptServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private SysDeptServiceImpl sysDeptService;
    @Autowired
    private ISysAreaService sysAreaService;
    public List<CustomerManagerImportResultVo> processImportData(List<CustomerManagerTemplateVo> dataList, Long createUserId) {
        List<GrpGroupImportDTO> canBindManagers = getCanBindManagers(createUserId);


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
            List<RepSysPersonNewDDo> managers = repSysPersonNewDMapper.selectProvincialManagers();
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
