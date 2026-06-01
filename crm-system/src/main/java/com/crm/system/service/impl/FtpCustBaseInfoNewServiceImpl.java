package com.crm.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dao.FtpCustBaseInfoNew;
import com.crm.system.domain.dao.sys.SysArea;
import com.crm.system.domain.dao.SysUserCustNo;
import com.crm.system.domain.dao.oldsyssyn.OldSysOrgDo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.mapper.FtpCustBaseInfoNewMapper;
import com.crm.system.mapper.sys.SysAreaMapper;
import com.crm.system.mapper.oldsyssyn.OldSysOrgMapper;
import com.crm.system.service.FtpCustBaseInfoNewService;
import com.crm.system.service.sys.ISysCodeService;
import com.crm.system.service.ISysUserCustNoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * FTP客户基础信息新表Service实现
 */
@Slf4j
@Service
public class FtpCustBaseInfoNewServiceImpl extends ServiceImpl<FtpCustBaseInfoNewMapper, FtpCustBaseInfoNew>
        implements FtpCustBaseInfoNewService {

    @Resource
    private OldSysOrgMapper oldSysOrgMapper;

    @Resource
    private SysAreaMapper sysAreaMapper;

    @Resource
    private ISysCodeService sysCodeService;

    @Resource
    private ISysUserCustNoService sysUserCustNoService;

    @Override
    public FtpCustBaseInfoNew getCustBaseNewInfo(SysUsers user, String grpName, String grpCode) {
        if (user == null || user.getOrgId() == null) {
            log.error("用户组织信息为空");
            return null;
        }

        //通过用户的orgId查询sys_org表获取组织信息
        OldSysOrgDo org = oldSysOrgMapper.selectById(user.getOrgId());
        if (org == null || org.getAreaId() == null) {
            log.error("获取用户组织区域信息失败，用户ID:{}", user.getId());
            return null;
        }

        //通过组织的areaId查询sys_area表获取区域信息
        SysArea area = sysAreaMapper.selectById(org.getAreaId());
        if (area == null) {
            log.error("根据areaId查询区域信息失败，areaId:{}", org.getAreaId());
            return null;
        }

        //区县id
        String countyId = String.valueOf(area.getBssCode());
        //区县名称
        String countyName = area.getName();
        //地市id
        String areaId;
        //地市名称
        String areaName;

        if ("三级".equals(area.getType())) {
            SysArea parentArea = sysAreaMapper.selectById(area.getParent());
            if (parentArea == null) {
                log.error("查询地市区域信息失败，父级ID:{}", area.getParent());
                return null;
            }
            areaId = String.valueOf(parentArea.getId());
            areaName = parentArea.getName();
        } else {
            // 地市
            areaId = String.valueOf(area.getId());
            areaName = area.getName();
        }

/*         //grp_name是加密字段,用做查询条件应该加密
        if (StringUtils.isNotEmpty(grpName)) {
            try {
                // grpName = AESUtil.aesEncrypt(grpName, aesKey);
            } catch (Exception e) {
                log.error("AES加密异常", e);
            }
        }*/

        return baseMapper.selectByCondition1(grpName, areaId, countyId, grpCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FtpCustBaseInfoNew saveCustBaseInfo(SysUsers user, String grpName, String grpDeptType, String grpCode, String custType) {
        if (StringUtils.isEmpty(grpName)) {
            log.info("客户名称为空，客户经理id:{}", user.getId());
            return null;
        }

        //通过用户的orgId查询sys_org表获取组织信息
        OldSysOrgDo org = oldSysOrgMapper.selectById(user.getOrgId());
        if (org == null || org.getAreaId() == null) {
            log.error("获取用户组织区域信息失败，用户ID:{}", user.getId());
            return null;
        }

        //通过组织的areaId查询sys_area表获取区域信息
        SysArea area = sysAreaMapper.selectById(org.getAreaId());

        if (area == null) {
            log.error("根据areaId查询区域信息失败，areaId:{}", org.getAreaId());
            return null;
        }

        String countyId = String.valueOf(area.getBssCode());
        String countyName = area.getName();
        String areaId;
        String areaName;

        // 判断区域类型：三级是区县，需要取父级作为地市
        if ("三级".equals(area.getType())) {
            // 区县，取父级作为地市
            SysArea parentArea = sysAreaMapper.selectById(area.getParent());
            if (parentArea == null) {
                log.error("查询地市区域信息失败，父级ID:{}", area.getParent());
                return null;
            }
            areaId = String.valueOf(parentArea.getId());
            areaName = parentArea.getName();
        } else {
            areaId = String.valueOf(area.getBssCode());
            areaName = area.getName();
        }

        /*// grp_name是加密字段
        if (StringUtils.isNotEmpty(grpName)) {
            try {
                // TODO: 需要实现AES加密
                // grpName = AESUtil.aesEncrypt(grpName, aesKey);
            } catch (Exception e) {
                log.error("AES加密异常", e);
            }
        }*/

        // 查询客户是否已存在
        FtpCustBaseInfoNew existingCust = baseMapper.selectByCondition2(grpName, areaId, countyId, custType, grpCode ,grpDeptType);

        if (existingCust != null) {
            return existingCust;
        }

        // 创建新客户
        FtpCustBaseInfoNew ftpCustBaseInfoNew = new FtpCustBaseInfoNew();
        ftpCustBaseInfoNew.setCountyId(countyId);
        ftpCustBaseInfoNew.setCountyName(countyName);
        ftpCustBaseInfoNew.setAreaId(areaId);
        ftpCustBaseInfoNew.setAreaName(areaName);
        ftpCustBaseInfoNew.setGrpName(grpName);
        ftpCustBaseInfoNew.setGrpCode(grpCode);
        ftpCustBaseInfoNew.setCustType(custType);
        ftpCustBaseInfoNew.setServManagerName(user.getNameCh());
        ftpCustBaseInfoNew.setServManagerPhone(user.getRelationTel());
        ftpCustBaseInfoNew.setGrpBusiDeptType(grpDeptType);
        // 客户行业名称
        String grpBusiDeptTypeName = sysCodeService.findNameByCodes("unicom_type_new", grpDeptType);
        ftpCustBaseInfoNew.setGrpBusiDeptTypeName(grpBusiDeptTypeName);

        // 查询用户客户编号列表
        List<SysUserCustNo> userCustNo = sysUserCustNoService.findListByUserId(user.getId().intValue());
        if(userCustNo != null && !userCustNo.isEmpty()){
            ftpCustBaseInfoNew.setServManagerId(userCustNo.get(0).getCustomerNo());
        }
        // 设置客户经理编号
        if (StringUtils.isNotEmpty(user.getCustomerNo())) {
            //客户经理编号设置为当前登录人得
            ftpCustBaseInfoNew.setServManagerId(user.getCustomerNo());
        }
        this.save(ftpCustBaseInfoNew);
        return ftpCustBaseInfoNew;
    }
}
