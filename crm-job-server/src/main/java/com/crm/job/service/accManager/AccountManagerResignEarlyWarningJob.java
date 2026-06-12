package com.crm.job.service.accManager;

import com.crm.common.core.domain.entity.SysDept;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.dao.accManager.WaringListTable;
import com.crm.system.domain.dao.zbzq.RepSysPersonNewDDo;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.service.IResignEarlyWarningService;
import com.crm.system.service.IWaringListTableService;
import com.crm.system.service.ISysAreaService;
import com.crm.system.service.ISysUsersService;
import com.crm.system.service.sys.ISysDeptService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户经理离职预警定时任务
 * <p>
 * 扫描离职人员及长期未登录人员，检测其在各业务系统中是否仍担任关键角色
 * （客户经理、商机负责人、合同承办人/履行人、基层维系人等），生成预警记录。
 *
 * @author lin_Yao
 * @date 2025/3/12
 */
@Slf4j
@Component
public class AccountManagerResignEarlyWarningJob {

    // ==================== 预警类型常量 ====================
    /** 预警类型: 离职人员 */
    private static final String WARNING_TYPE_1 = "1";
    /** 预警类型: 长期未登录 */
    private static final String WARNING_TYPE_2 = "2";
    /** 预警状态: 在职 */
    private static final Integer STATE_ONJOB = 0;
    /** 预警状态: 已离职 */
    private static final Integer STATE_RESIGNED = 1;

    // ==================== 角色标记常量 ====================
    private static final String CUST_LEVEL4_MANAGER = "cust_level4_manager";
    private static final String NATURAL_CUST_MANAGER = "natural_cust_manager";
    private static final String ENTITY_CUST_MANAGER = "entity_cust_manager";
    private static final String OPPORTUNITY_MANAGER = "opportunity_manager";
    private static final String CONTRACT_MANAGER = "contract_manager";
    private static final String CONTACT_PERSON = "contact_person";

    /** 长期未登录天数阈值 */
    private static final int LONG_TIME_NO_LOGIN_DAYS = 20;

    @Resource
    private IResignEarlyWarningService resignEarlyWarningService;

    @Resource
    private IWaringListTableService waringListTableService;

    @Resource
    private ISysUsersService sysUsersService;

    @Resource
    private ISysAreaService sysAreaService;

    @Resource
    private ISysDeptService sysDeptService;

    /**
     * 客户经理离职预警同步任务
     */
    @XxlJob(value = "accManagerResignSync")
    public void start() {
        String cycleId = new SimpleDateFormat("yyyyMMdd").format(new Date());
        log.info(">>>>>>>>客户经理离职预警开始执行,执行时间:{}", cycleId);
        XxlJobHelper.log("客户经理离职预警开始执行,执行时间:" + cycleId);

        try {
            long start = System.currentTimeMillis();
            // 离职人员预警处理
            queryTheInformationOfTheDepartingEmployee(cycleId);

            long end = System.currentTimeMillis();
            log.info(">>>>>>>>客户经理离职预警执行完成,耗时:{}毫秒", (end - start));
            XxlJobHelper.log("客户经理离职预警执行完成,耗时:" + (end - start) + "毫秒");

            // 查询长期未登录工号
            queryLongTimeNoLoginOaUser(cycleId);

            long userEnd = System.currentTimeMillis();
            log.info(">>>>>>>>客户经理离职预警查询长期未登录工号执行完成,耗时:{}毫秒", (userEnd - end));
            XxlJobHelper.log("查询长期未登录工号执行完成,耗时:" + (userEnd - end) + "毫秒");

        } catch (Exception e) {
            log.error(">>>>>>>>客户经理离职预警执行失败", e);
            XxlJobHelper.log("客户经理离职预警执行失败:" + e.getMessage());
        }
    }

    /**
     * 查询离职人员信息，逐人检测各业务系统角色挂靠情况，生成预警记录
     */
    private void queryTheInformationOfTheDepartingEmployee(String cycleId) {
        List<RepSysPersonNewDDo> list = resignEarlyWarningService.selectResignedPersons();

        if (list.isEmpty()) {
            log.info(">>>>>>>>无离职人员");
            XxlJobHelper.log("无离职人员");
            return;
        }
        log.info(">>>>>>>>查询到离职人员:{}条", list.size());
        XxlJobHelper.log("查询到离职人员:" + list.size() + "条");

        // 清空类型为离职人员的旧预警数据
        log.info(">>>>>>>>清空表数据");
        waringListTableService.deleteByWaringType(WARNING_TYPE_1);

        WaringListTable waringListTable;
        for (RepSysPersonNewDDo person : list) {
            waringListTable = new WaringListTable();
            waringListTable.setCycleId(cycleId);
            waringListTable.setOaUserName(person.getOaUserName() != null ? person.getOaUserName().toLowerCase() : "");
            waringListTable.setFullName(person.getFullName() != null ? person.getFullName() : "");
            waringListTable.setWaringType(WARNING_TYPE_1);
            waringListTable.setState(STATE_ONJOB);

            // 处理地区信息
            String areaName = person.getAreaName() != null ? person.getAreaName() : "";
            if (!areaName.isEmpty()) {
                SysAreaVo sysArea = escapeAreaName(areaName);
                if (sysArea == null) {
                    waringListTable.setAreaId(person.getAreaId() != null ? person.getAreaId() : "");
                    waringListTable.setAreaName(areaName);
                } else {
                    waringListTable.setAreaId(String.valueOf(sysArea.getId()));
                    waringListTable.setAreaName(sysArea.getName());
                }
            }
            waringListTable.setCountyId(person.getCountyId() != null ? person.getCountyId() : "");
            waringListTable.setCountyName(person.getCountyName() != null ? person.getCountyName() : "");

            // 构建用于角色检测的Map
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("OA_USER_NAME", person.getOaUserName());
            userMap.put("FULL_NAME", person.getFullName());

            // 检测各业务系统角色挂靠
            whetherCustLevel4Manager(userMap, waringListTable);
            whetherNaturalCustManager(userMap, waringListTable);
            // 实体客户客户经理 暂时没有OA账号先不跑
            // whetherEntityCustManager(userMap, waringListTable);
            whetherOpportunityManager(userMap, waringListTable);
            whetherContractManager(userMap, waringListTable);
            whetherContactPerson(userMap, waringListTable);

            // 有预警信息则插入表中
            if (waringListTable.getCustLevel4ManagerCount() != null && waringListTable.getCustLevel4ManagerCount() != 0
                    || waringListTable.getNaturalCustManagerCount() != null && waringListTable.getNaturalCustManagerCount() != 0
                    || waringListTable.getEntityCustManagerCount() != null && waringListTable.getEntityCustManagerCount() != 0
                    || waringListTable.getOpportunityManagerCount() != null && waringListTable.getOpportunityManagerCount() != 0
                    || waringListTable.getContractManagerCount() != null && waringListTable.getContractManagerCount() != 0) {
                waringListTable.setCreateTime(new Date());
                waringListTableService.insert(waringListTable);
            }
        }
        log.info(">>>>>>>>客户经理离职预警执行完成");
        XxlJobHelper.log("客户经理离职预警执行完成");
    }

    /**
     * 查询长期未登录工号，逐人检测各业务系统角色挂靠情况，生成预警记录
     */
    private void queryLongTimeNoLoginOaUser(String cycleId) {
        List<SysUsers> sysUserList = sysUsersService.selectFailureList(LONG_TIME_NO_LOGIN_DAYS);

        if (sysUserList.isEmpty()) {
            log.info(">>>>>>>>长期未登录工号:无");
            XxlJobHelper.log("长期未登录工号:无");
            return;
        }

        // 清空类型为长期未登录的旧预警数据
        waringListTableService.deleteByWaringType(WARNING_TYPE_2);

        log.info(">>>>>>>>长期未登录工号:{}条", sysUserList.size());
        XxlJobHelper.log("长期未登录工号:" + sysUserList.size() + "条");

        WaringListTable waringListTable;
        for (SysUsers sysUser : sysUserList) {
            waringListTable = new WaringListTable();
            waringListTable.setCycleId(cycleId);
            waringListTable.setOaUserName(sysUser.getOaAccount() != null ? sysUser.getOaAccount().toLowerCase() : "");
            waringListTable.setFullName(sysUser.getNameCh() != null ? sysUser.getNameCh() : "");
            waringListTable.setWaringType(WARNING_TYPE_2);

            // 查询离职状态
            String userState = getUserState(sysUser.getOaAccount());
            if (userState != null) {
                waringListTable.setState("1".equals(userState) ? STATE_ONJOB : STATE_RESIGNED);
            }

            // 处理地区信息：通过 org_id → sys_dept → area_code → sys_area
            fillAreaInfoForUser(sysUser, waringListTable);

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("OA_USER_NAME", sysUser.getOaAccount());

            // 检测各业务系统角色挂靠
            whetherCustLevel4Manager(userMap, waringListTable);
            whetherNaturalCustManager(userMap, waringListTable);
            // 实体客户客户经理 暂时没有OA账号先不跑
            // whetherEntityCustManager(userMap, waringListTable);
            whetherOpportunityManager(userMap, waringListTable);
            whetherContractManager(userMap, waringListTable);
            whetherContactPerson(userMap, waringListTable);

            // 有预警信息则插入表中
            if (waringListTable.getCustLevel4ManagerCount() != null && waringListTable.getCustLevel4ManagerCount() != 0
                    || waringListTable.getNaturalCustManagerCount() != null && waringListTable.getNaturalCustManagerCount() != 0
                    || waringListTable.getEntityCustManagerCount() != null && waringListTable.getEntityCustManagerCount() != 0
                    || waringListTable.getOpportunityManagerCount() != null && waringListTable.getOpportunityManagerCount() != 0
                    || waringListTable.getContractManagerCount() != null && waringListTable.getContractManagerCount() != 0) {
                waringListTable.setCreateTime(new Date());
                waringListTableService.insert(waringListTable);
            }
        }
        log.info(">>>>>>>>长期未登录工号预警执行完成");
        XxlJobHelper.log("长期未登录工号预警执行完成");
    }

    /**
     * 通过用户所属组织查询地区信息，填充到预警记录中
     */
    private void fillAreaInfoForUser(SysUsers sysUser, WaringListTable waringListTable) {
        if (sysUser.getOrgId() == null) {
            return;
        }
        try {
            SysDept dept = sysDeptService.selectDeptById(sysUser.getOrgId());
            if (dept == null || dept.getAreaCode() == null) {
                return;
            }
            SysAreaVo area = sysAreaService.queryById(Long.parseLong(dept.getAreaCode()));
            if (area == null) {
                return;
            }

            if ("一级".equals(area.getType()) || "二级".equals(area.getType())) {
                waringListTable.setAreaId(String.valueOf(area.getId()));
                waringListTable.setAreaName(area.getName());
            } else {
                // 三级: 当前area是区县, 父级是地市
                waringListTable.setCountyId(String.valueOf(area.getId()));
                waringListTable.setCountyName(area.getName());

                if (area.getParent() != null) {
                    SysAreaVo parent = sysAreaService.queryById(area.getParent());
                    if (parent != null) {
                        waringListTable.setAreaId(String.valueOf(parent.getId()));
                        waringListTable.setAreaName(parent.getName());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("查询用户地区信息失败, userId:{}, orgId:{}", sysUser.getId(), sysUser.getOrgId(), e);
        }
    }

    // ==================== 各业务角色检测方法 ====================

    /**
     * 四级名单客户客户经理 A/B角
     */
    private void whetherCustLevel4Manager(Map<String, Object> user, WaringListTable waringListTable) {
        String oaUserName = user.get("OA_USER_NAME").toString();
        List<Map<String, Object>> list = resignEarlyWarningService.selectCustLevel4ManagerByOaUser(oaUserName);

        if (list.isEmpty()) {
            log.debug(">>>>>>>>该用户不属于四级名单客户客户经理 A/B角:({})", oaUserName);
        } else {
            waringListTable.setCustLevel4Manager(CUST_LEVEL4_MANAGER);
            waringListTable.setCustLevel4ManagerCount(list.size());
        }
    }

    /**
     * 自然客户客户经理
     */
    private void whetherNaturalCustManager(Map<String, Object> user, WaringListTable waringListTable) {
        String oaUserName = user.get("OA_USER_NAME").toString();
        List<Map<String, Object>> list = resignEarlyWarningService.selectNaturalCustManagerByOaUser(oaUserName);

        if (list.isEmpty()) {
            log.debug(">>>>>>>>该用户不属于自然客户客户经理:({})", oaUserName);
        } else {
            waringListTable.setNaturalCustManager(NATURAL_CUST_MANAGER);
            waringListTable.setNaturalCustManagerCount(list.size());
        }
    }

    /**
     * 实体客户客户经理 (暂不启用)
     */
    @SuppressWarnings("unused")
    private void whetherEntityCustManager(Map<String, Object> user, WaringListTable waringListTable) {
        String fullName = user.get("FULL_NAME") != null ? user.get("FULL_NAME").toString() : "";
        if (fullName.isEmpty()) {
            return;
        }
        List<Map<String, Object>> list = resignEarlyWarningService.selectEntityCustManagerByFullName(fullName);

        if (list.isEmpty()) {
            log.debug(">>>>>>>>该用户不属于实体客户客户经理:({})", user.get("OA_USER_NAME"));
        } else {
            waringListTable.setEntityCustManager(ENTITY_CUST_MANAGER);
            waringListTable.setEntityCustManagerCount(list.size());
        }
    }

    /**
     * 商机的客户经理
     */
    private void whetherOpportunityManager(Map<String, Object> user, WaringListTable waringListTable) {
        String oaUserName = user.get("OA_USER_NAME").toString();
        List<Map<String, Object>> list = resignEarlyWarningService.selectOpportunityManagerByOaUser(oaUserName);

        if (list.isEmpty()) {
            log.debug(">>>>>>>>该用户不属于商机的客户经理:({})", oaUserName);
        } else {
            waringListTable.setOpportunityManager(OPPORTUNITY_MANAGER);
            waringListTable.setOpportunityManagerCount(list.size());
        }
    }

    /**
     * 合同的承办人/履行人
     */
    private void whetherContractManager(Map<String, Object> user, WaringListTable waringListTable) {
        String oaUserName = user.get("OA_USER_NAME").toString();
        List<Map<String, Object>> list = resignEarlyWarningService.selectContractManagerByOaUser(oaUserName);

        if (list.isEmpty()) {
            log.debug(">>>>>>>>该用户不属于合同的承办人/履行人:({})", oaUserName);
        } else {
            waringListTable.setContractManager(CONTRACT_MANAGER);
            waringListTable.setContractManagerCount(list.size());
        }
    }

    /**
     * 基层维系人
     */
    private void whetherContactPerson(Map<String, Object> user, WaringListTable waringListTable) {
        String oaUserName = user.get("OA_USER_NAME").toString();
        List<Map<String, Object>> list = resignEarlyWarningService.selectContactPersonByOaUser(oaUserName);

        if (list.isEmpty()) {
            log.debug(">>>>>>>>该用户不属于基层维系人:({})", oaUserName);
        } else {
            waringListTable.setContactPerson(CONTACT_PERSON);
            waringListTable.setContactPersonCount(list.size());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 解析地区名称，将浙江省本级映射为省本级，其他按地市名匹配
     */
    private SysAreaVo escapeAreaName(String areaName) {
        if (areaName == null || areaName.length() < 3) {
            return null;
        }
        String str = areaName.substring(0, 3);

        // 浙江省特殊处理
        if ("浙江省".equals(str)) {
            SysAreaVo area = new SysAreaVo();
            area.setName("省本级");
            area.setId(330000L);
            return area;
        }

        // 与浙江地市列表匹配
        List<SysAreaVo> cityAreas = sysAreaService.queryZhejiangCities();
        for (SysAreaVo cityArea : cityAreas) {
            if (cityArea.getName() != null && cityArea.getName().equals(str)) {
                return cityArea;
            }
        }
        return null;
    }

    /**
     * 查询OA账号的在职状态
     * @return ONJOB_FLAG值，未查到返回null
     */
    private String getUserState(String oaId) {
        if (oaId == null || oaId.isEmpty()) {
            return null;
        }
        Map<String, Object> result = resignEarlyWarningService.selectUserStateByOaId(oaId.toLowerCase());
        if (result == null || result.isEmpty()) {
            return null;
        }
        Object onjobFlag = result.get("ONJOB_FLAG");
        return onjobFlag != null ? onjobFlag.toString() : null;
    }

    /**
     * 从Map中安全获取String值
     */
    private String getMapString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }
}
