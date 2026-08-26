package com.crm.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.dao.FtpCustBaseInfoDo;
import com.crm.system.domain.dao.MaintenanceDetailDo;
import com.crm.system.domain.dao.MaintenanceDetailNewDo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.dao.oldsyssyn.OldSysOrgDo;
import com.crm.system.domain.dao.sys.SysArea;
import com.crm.system.mapper.InterCountyCodeMapper;
import com.crm.system.mapper.MaintenanceDetailMapper;
import com.crm.system.mapper.MaintenanceDetailNewMapper;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.mapper.cust.FtpCustBaseInfoMapper;
import com.crm.system.mapper.oldsyssyn.OldSysOrgMapper;
import com.crm.system.mapper.sys.SysAreaMapper;
import com.crm.system.service.IMaintenanceDetailSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 维系预警明细写入 Service 实现。
 * <p>
 * 复刻旧系统 MaintenanceDetailService.setAreaAndOrgNew 的地市/区县/归属部门解析链，
 * 以及 TcUserCustWarningNewJob 中 maintenance_detail / maintenance_detail_new 的逐列赋值。
 * <p>
 * 注意新旧口径差异：
 * - area_id/county_id 存的是 sys_area.id_（内部主键，如地市 330100、区县 33010x）。
 * - 新库 sys_area.type_ 为数字 "1省/2地市/3区县"，旧库为中文"一/二/三级"，本类两者兼容判断。
 * - manager 信息取自旧 sys_user（findByOaAccount），与旧系统 findByOaId 对齐（manager_id=customer_no）。
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceDetailSyncServiceImpl implements IMaintenanceDetailSyncService {

    private final MaintenanceDetailMapper maintenanceDetailMapper;
    private final MaintenanceDetailNewMapper maintenanceDetailNewMapper;
    private final SysAreaMapper sysAreaMapper;
    private final FtpCustBaseInfoMapper ftpCustBaseInfoMapper;
    private final InterCountyCodeMapper interCountyCodeMapper;
    private final SysUsersMapper sysUsersMapper;
    private final OldSysOrgMapper sysOrgMapper;
    private final com.crm.system.service.ISysUserCustNoService sysUserCustNoService;

    /** transAreaByName 硬编码地市名 → sys_area.id_（复刻 MaintenanceDetailService.transAreaByName） */
    private static final Map<String, Long> CITY_AREA_ID = new HashMap<>();

    static {
        CITY_AREA_ID.put("杭州", 330100L);
        CITY_AREA_ID.put("宁波", 330200L);
        CITY_AREA_ID.put("温州", 330300L);
        CITY_AREA_ID.put("嘉兴", 330400L);
        CITY_AREA_ID.put("湖州", 330500L);
        CITY_AREA_ID.put("绍兴", 330600L);
        CITY_AREA_ID.put("金华", 330700L);
        CITY_AREA_ID.put("衢州", 330800L);
        CITY_AREA_ID.put("舟山", 330900L);
        CITY_AREA_ID.put("台州", 331000L);
        CITY_AREA_ID.put("丽水", 331100L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void writeSuccess(ToppOrderBo orderBo, Map<String, Object> extra) {
        if (orderBo == null || extra == null) {
            return;
        }
        String grpCode = str(extra.get("grpCode"));
        String custGrpId = str(extra.get("custGrpId"));
        String areaName = str(extra.get("areaName"));
        String countyName = str(extra.get("countyName"));
        String oaIdDev = str(extra.get("oaIdDev"));
        String oaNameDev = str(extra.get("oaNameDev"));

        AreaResult area = resolveAreaForDetail(custGrpId, areaName, countyName);
        FtpCustBaseInfoDo cust = ftpByGrpCode(grpCode);
        String isImpPrt = cust != null ? cust.getIsImpPrt() : null;
        String grpType = cust != null ? cust.getGrpType() : null;

        // manager 取旧 sys_user，对齐旧系统 findByOaId（manager_id=customer_no、user_id=sys_user.id_）
        SysUsers manager = StringUtils.isNotBlank(oaIdDev) ? sysUsersMapper.findByOaAccount(oaIdDev) : null;
        String managerId;
        String managerName;
        Long userId;
        if (manager != null) {
            managerId = manager.getCustomerNo();
            managerName = manager.getNameCh();
            userId = manager.getId();
        } else {
            // 新旧差异：新框架已按 zqzt_sys_user 建单，但旧 sys_user 无此账号；退化用 OA 信息，status 仍为 1（工单已存在）
            log.warn("【维系明细】建单成功但旧 sys_user 未找到客户经理, oaId={}, orderId={}", oaIdDev, orderBo.getId());
            managerId = oaIdDev;
            managerName = oaNameDev;
            userId = null;
        }

        // maintenance_detail（status=1）
        MaintenanceDetailDo d = new MaintenanceDetailDo();
        d.setOrderType(orderBo.getOrderType());
        d.setDetailId(orderBo.getInfoId());
        d.setGrpCode(grpCode);
        d.setCustGrpId(custGrpId);
        d.setGrpName(orderBo.getCustName());
        d.setOrderId(orderBo.getId());
        d.setManagerId(managerId);
        d.setManagerName(managerName);
        d.setUserId(userId);
        d.setStatus(1);
        d.setOrderCategory("C");
        d.setCreateTime(new Date());
        d.setIsImpPrt(isImpPrt);
        d.setGrpType(grpType);
        d.setAreaId(area.areaId);
        d.setCountyId(area.countyId);
        d.setBusinessDep(area.businessDep);
        // 新口径：area_id_new/county_id_new 按客户经理归属地市（对齐 maintenance_detail_new）
        setAreaAndOrg(d, manager);
        maintenanceDetailMapper.insert(d);

        // maintenance_detail_new（status=1）——area/county 走客户经理组织维度，简化为其所在地市（orderBo.areaId）
        Long mgrArea = orderBo.getAreaId();
        MaintenanceDetailNewDo dn = new MaintenanceDetailNewDo();
        dn.setOrderType(orderBo.getOrderType());
        dn.setDetailId(orderBo.getInfoId());
        dn.setGrpCode(grpCode);
        dn.setManagerId(managerId);
        dn.setManagerName(managerName);
        dn.setUserId(userId);
        dn.setOrderId(orderBo.getId());
        dn.setStatus(1);
        dn.setIsImpPrt(isImpPrt);
        dn.setGrpType(grpType);
        dn.setCreateTime(new Date());
        dn.setOrderCategory("C");
        dn.setAreaId(d.getAreaIdNew());
        dn.setCountyId(d.getCountyIdNew());
        maintenanceDetailNewMapper.insert(dn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignSuccess(Long detailId, ToppOrderBo orderBo, Map<String, Object> extra) {
        if (orderBo == null || extra == null) {
            return;
        }
        MaintenanceDetailDo d = maintenanceDetailMapper.selectById(detailId);
        if (d == null || !Integer.valueOf(0).equals(d.getStatus())) {
            log.warn("【维系明细】手动指派时未找到status=0明细, detailId={}", detailId);
            return;
        }
        String grpCode = d.getGrpCode();
        // 指派场景：处理人为目标 userId（orderBo.custManagerId），不再引用预警原 oa_id_dev
        SysUsers manager = sysUsersMapper.selectById(orderBo.getCustManagerId());
        String managerId;
        String managerName;
        Long userId = orderBo.getCustManagerId();
        if (manager != null) {
            managerId = manager.getCustomerNo();
            if (StringUtils.isBlank(managerId)) {
                // 对齐旧系统：sys_user.customer_no 为空时，从 sys_user_cust_no 取第一条客户编号
                java.util.List<com.crm.system.domain.dao.SysUserCustNo> custNos =
                        sysUserCustNoService.findListByUserId(manager.getId().intValue());
                if (custNos != null && !custNos.isEmpty()) {
                    managerId = custNos.get(0).getCustomerNo();
                }
            }
            managerName = manager.getNameCh();
            userId = manager.getId();
        } else {
            managerId = str(extra.get("oaIdDev"));
            managerName = str(extra.get("oaNameDev"));
        }

        d.setOrderId(orderBo.getId());
        d.setManagerId(managerId);
        d.setManagerName(managerName);
        d.setUserId(userId);
        d.setStatus(1);
        if (StringUtils.isBlank(d.getGrpName())) {
            d.setGrpName(orderBo.getCustName());
        }
        setAreaAndOrg(d, manager);
        maintenanceDetailMapper.updateById(d);

        // 新口径维护明细（status=1）
        MaintenanceDetailNewDo dn = new MaintenanceDetailNewDo();
        dn.setOrderType(orderBo.getOrderType());
        dn.setDetailId(d.getDetailId());
        dn.setGrpCode(grpCode);
        dn.setManagerId(managerId);
        dn.setManagerName(managerName);
        dn.setUserId(userId);
        dn.setOrderId(orderBo.getId());
        dn.setStatus(1);
        dn.setIsImpPrt(d.getIsImpPrt());
        dn.setGrpType(d.getGrpType());
        dn.setCreateTime(new Date());
        dn.setOrderCategory(d.getOrderCategory());
        dn.setAreaId(d.getAreaIdNew());
        dn.setCountyId(d.getCountyIdNew());
        maintenanceDetailNewMapper.insert(dn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void writeNoManager(String orderType, Map<String, Object> row) {
        if (row == null) {
            return;
        }
        String grpCode = str(row.get("grp_code"));
        String custGrpId = str(row.get("cust_grp_id"));
        String areaName = str(row.get("area_name"));
        String countyName = str(row.get("county_name"));

        AreaResult area = resolveAreaForDetail(custGrpId, areaName, countyName);
        FtpCustBaseInfoDo cust = ftpByGrpCode(grpCode);

        MaintenanceDetailDo d = new MaintenanceDetailDo();
        d.setOrderType(orderType);
        d.setDetailId(toLong(row.get("id_")));
        d.setGrpCode(grpCode);
        d.setCustGrpId(custGrpId);
        d.setGrpName(str(row.get("grp_name")));
        // status=0：manager_id/name 用 OA 原值（对齐旧系统 line 309/310 未被覆盖），无 order_id/user_id
        d.setManagerId(str(row.get("oa_id_dev")));
        d.setManagerName(str(row.get("oa_name_dev")));
        d.setUserId(null);
        d.setOrderId(null);
        d.setStatus(0);
        d.setOrderCategory("C");
        d.setCreateTime(new Date());
        d.setIsImpPrt(cust != null ? cust.getIsImpPrt() : null);
        d.setGrpType(cust != null ? cust.getGrpType() : null);
        d.setAreaId(area.areaId);
        d.setCountyId(area.countyId);
        d.setBusinessDep(area.businessDep);
        d.setAreaIdNew(null);
        d.setCountyIdNew(null);
        maintenanceDetailMapper.insert(d);
    }

    // ==================== 地市/区县/归属部门解析（复刻 setAreaAndOrgNew） ====================

    private AreaResult resolveAreaForDetail(String custGrpId, String areaName, String countyName) {
        AreaResult r = new AreaResult();
        if (StringUtils.isBlank(custGrpId)) {
            r.areaId = transAreaByName(areaName);
            r.businessDep = "22";
        } else {
            FtpCustBaseInfoDo custInfo = ftpByCustGrpId(custGrpId);
            if (custInfo != null) {
                SysArea county = sysAreaByBssCode(custInfo.getCountyId());
                if (county != null && isCountyLevel(county.getType())) {
                    r.countyId = county.getId();
                    if (StringUtils.isNotBlank(county.getBssCodeParent())) {
                        SysArea areaObj = sysAreaByBssCode(county.getBssCodeParent());
                        if (areaObj != null) {
                            r.areaId = areaObj.getId();
                        }
                    }
                }
                String dept = custInfo.getGrpBusiDeptType();
                r.businessDep = (StringUtils.isBlank(dept) || "99".equals(dept)) ? "22" : dept;
            } else {
                r.businessDep = "22";
            }
            if (r.areaId == null) {
                r.areaId = transAreaByName(areaName);
            }
        }
        // county_id 兜底：INTER_COUNTY_CODE（区县名 [+ 地市名] → bss_code → sys_area.id_）
        if ((r.countyId == null || r.countyId == 0L) && StringUtils.isNotBlank(countyName)) {
            String bss;
            if ("市本级（集团）".equals(countyName) || "市本级（公众）".equals(countyName)) {
                bss = interCountyCodeMapper.selectCountyIdByNameAndCity(countyName, areaName);
            } else {
                bss = interCountyCodeMapper.selectCountyIdByName(countyName);
            }
            if (StringUtils.isNotBlank(bss)) {
                SysArea a = sysAreaByBssCode(bss);
                if (a != null) {
                    r.countyId = a.getId();
                }
            }
        }
        return r;
    }

    /** 地市名 → sys_area.id_，未匹配返回 0（对齐旧系统 transAreaByName 的"其他"分支） */
    private Long transAreaByName(String areaName) {
        if (StringUtils.isBlank(areaName)) {
            return 0L;
        }
        for (Map.Entry<String, Long> e : CITY_AREA_ID.entrySet()) {
            if (areaName.contains(e.getKey())) {
                return e.getValue();
            }
        }
        return 0L;
    }

    /** 新库 type_ 为 "3"，旧库为 "三级"，兼容判断区县级 */
    private boolean isCountyLevel(String type) {
        return "3".equals(type) || "三级".equals(type);
    }

    private SysArea sysAreaByBssCode(String bssCode) {
        if (StringUtils.isBlank(bssCode)) {
            return null;
        }
        List<SysArea> list = sysAreaMapper.selectList(
                new LambdaQueryWrapper<SysArea>().eq(SysArea::getBssCode, bssCode).last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0);
    }

    private FtpCustBaseInfoDo ftpByGrpCode(String grpCode) {
        if (StringUtils.isBlank(grpCode)) {
            return null;
        }
        List<FtpCustBaseInfoDo> list = ftpCustBaseInfoMapper.selectList(
                new LambdaQueryWrapper<FtpCustBaseInfoDo>().eq(FtpCustBaseInfoDo::getGrpCode, grpCode).last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0);
    }

    private FtpCustBaseInfoDo ftpByCustGrpId(String custGrpId) {
        Integer cg = parseIntOrNull(custGrpId);
        if (cg == null) {
            return null;
        }
        List<FtpCustBaseInfoDo> list = ftpCustBaseInfoMapper.selectList(
                new LambdaQueryWrapper<FtpCustBaseInfoDo>().eq(FtpCustBaseInfoDo::getCustGrpId, cg).last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0);
    }

    private static String str(Object v) {
        return v == null ? null : v.toString();
    }

    private static Long toLong(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        try {
            return Long.parseLong(v.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parseIntOrNull(String v) {
        if (StringUtils.isBlank(v)) {
            return null;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** area/county/business_dep 解析结果 */
    private static class AreaResult {
        Long areaId;
        Long countyId;
        String businessDep;
    }

    /**
     * 新增维系预警报表的地市区县和行业部信息
     * @param maintenance
     * @param user
     */
    public void setAreaAndOrg(MaintenanceDetailDo maintenance, SysUsers user){
        //从集团客户维度改为客户经理维度
        if(null == user || null == user.getOrgId() ){
            return ;
        }
        OldSysOrgDo sysOrgDo = sysOrgMapper.selectById(user.getOrgId());
        if(ObjectUtil.isEmpty(sysOrgDo.getAreaId())){
            return ;
        }
        SysArea area = sysAreaMapper.selectById(sysOrgDo.getAreaId());
        if(null == area){
            return ;
        }
        if("一级".equals(area.getType()) || "二级".equals(area.getType())){
            //省本级和市本级
            maintenance.setAreaIdNew(area.getId());
            maintenance.setCountyIdNew(area.getId());
        }else if("三级".equals(area.getType())){
            SysArea parentArea = sysAreaMapper.selectById(area.getParent());
            //区县
            maintenance.setAreaIdNew(parentArea.getId());
            maintenance.setCountyIdNew(area.getId());
        }
    }
}
