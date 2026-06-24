package com.crm.job.service.warning.strategy;

import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.utils.DateUtils;
import com.crm.job.mapper.warning.WideUpWarningMapper;
import com.crm.job.service.warning.AbstractWarningQueryStrategy;
import com.crm.job.service.warning.IWarningStartService;
import com.crm.job.service.warning.WarningRecord;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.mapper.SysUsersMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 宽带升级预警查询策略
 * <p>
 * 对应旧系统 WarningUpJob.wideWarningUpJob → startTaskWarnUp
 * 查询有预警数据的区县 + 市本级组织部门 → 查找各级领导 → 为每位领导生成一条 WarningRecord
 * <p>
 * 领导查找优先级（区县）：
 * 1. 总经理(POS_CODE=1) → 2. 副总经理(POS_CODE=2) → 3. 系统角色兜底
 * <p>
 * 领导查找优先级（市本级）：
 * 1. 总经理 → 2. 副总经理 → 3. 匹配表映射 → 4. 映射后的总经理 → 5. 映射后的副总经理 → 6. 系统角色兜底
 *
 * @author crm
 */
@Component
public class WideUpWarningStrategy extends AbstractWarningQueryStrategy {

    private static final int BOSS_POS_CODE = 1;
    private static final int VICE_BOSS_POS_CODE = 2;
    private static final String BOSS_FLAG_AREA = "1";     // 区县
    private static final String BOSS_FLAG_ORG = "2";      // 市本级

    @Resource
    private WideUpWarningMapper mapper;

    @Resource
    private SysUsersMapper sysUsersMapper;

    @Lazy
    @Resource
    private IWarningStartService warningStartService;

    @Override
    public String getOrderType() {
        return "new_wide_up";
    }

    @Override
    protected String getOrderTypeName() {
        return "宽带升级预警";
    }

    @Override
    protected List<WarningRecord> doQuery(String yyyyMM) {
        List<WarningRecord> records = new ArrayList<>();

        // ========== 一、区县维度 ==========
        List<Map<String, Object>> areas = mapper.selectWarningAreas(yyyyMM);
        log.info("【{}】查询到预警区县: {}个", getOrderTypeName(), areas.size());

        for (Map<String, Object> area : areas) {
            Long areaId = toLong(area.get("id_"));
            String areaName = getString(area.get("name_"));
            String bssCode = getString(area.get("bss_code"));

            // 按优先级查找领导
            List<LeaderInfo> areaLeaders = findAreaLeaders(bssCode, areaName, areaId);
            if (areaLeaders.isEmpty()) {
                log.warn("【{}】区县未找到领导: {}", getOrderTypeName(), areaName);
                continue;
            }

            for (LeaderInfo leader : areaLeaders) {
                records.add(buildRecord(areaId, areaName, bssCode, null, leader, yyyyMM, BOSS_FLAG_AREA));
            }
        }

        // ========== 二、市本级组织部门维度 ==========
        List<Map<String, Object>> orgs = mapper.selectOrgDepartments(yyyyMM);
        log.info("【{}】查询到预警市本级部门: {}个", getOrderTypeName(), orgs.size());

        for (Map<String, Object> org : orgs) {
            String countyId = getString(org.get("countyId"));
            String countyName = getString(org.get("countyName"));
            String areaName = getString(org.get("areaName"));

            // 按优先级查找领导
            List<LeaderInfo> orgLeaders = findOrgLeaders(countyId, countyName, areaName);
            if (orgLeaders.isEmpty()) {
                log.warn("【{}】市本级部门未找到领导: countyId={}, name={}", getOrderTypeName(), countyId, countyName);
                continue;
            }

            for (LeaderInfo leader : orgLeaders) {
                records.add(buildRecord(Long.parseLong(countyId), countyName, null, countyId, leader, yyyyMM, BOSS_FLAG_ORG));
            }
        }

        return records;
    }

    // ==================== 领导查找（区县） ====================

    /**
     * 区县领导查找：总经理 → 副总经理 → 地市接口人兜底（sys_flow_user 配置）
     */
    private List<LeaderInfo> findAreaLeaders(String bssCode, String areaName, Long areaId) {
        // 1. 查总经理
        List<LeaderInfo> leaders = queryLeaders(bssCode, BOSS_POS_CODE, BOSS_FLAG_AREA);
        if (!leaders.isEmpty()) return leaders;

        // 2. 查副总经理
        leaders = queryLeaders(bssCode, VICE_BOSS_POS_CODE, BOSS_FLAG_AREA);
        if (!leaders.isEmpty()) return leaders;

        // 3. 兜底：查询 sys_flow_user 配置的地市接口人（对应老系统 getWarnUpDeveloper）
        log.warn("【{}】区县总经理和副总经理均未找到，使用地市接口人兜底, area={}, bssCode={}",
                getOrderTypeName(), areaName, bssCode);
        return convertToLeaders(warningStartService.findDefaultHandlerByProcessAndArea(
                getOrderType(), "leader", areaId));
    }

    /**
     * 查询区县或市本级的领导（调用 DM 数据库）
     */
    private List<LeaderInfo> queryLeaders(String code, int posCode, String bossFlag) {
        List<LeaderInfo> result = new ArrayList<>();
        List<Map<String, Object>> list = mapper.selectBossLeaders(code, posCode, bossFlag);
        for (Map<String, Object> row : list) {
            String oaUserName = getString(row.get("OA_USER_NAME"));
            String fullName = getString(row.get("FULL_NAME"));
            if (oaUserName != null) {
                LeaderInfo li = getUserByOa(oaUserName);
                result.add(li);
            }
        }
        return result;
    }

    /**
     * 将 SysUser 列表转换为 LeaderInfo 列表
     */
    private List<LeaderInfo> convertToLeaders(List<SysUser> users) {
        List<LeaderInfo> result = new ArrayList<>();
        if (users == null) return result;
        for (SysUser user : users) {
            LeaderInfo li = new LeaderInfo();
            li.userId = user.getUserId();
            li.deptId = user.getDeptId();
            li.nickName = user.getNickName();
            li.phone = user.getPhonenumber();
            result.add(li);
        }
        return result;
    }

    /**
     * 通过 OA 账号查找本地 sys_user，获取 user_id 和 dept_id
     */
    private LeaderInfo getUserByOa(String oaUserName) {
        LeaderInfo li = new LeaderInfo();
        li.oaUserName = oaUserName;
        if (oaUserName == null) {
            return li;
        }
        try {
            com.crm.system.domain.dao.SysUsers localUser = sysUsersMapper.findByOaAccount(oaUserName.toLowerCase());
            if (localUser != null) {
                li.userId = localUser.getId();
                li.deptId = localUser.getOrgId();
            }
        } catch (Exception e) {
            log.debug("OA用户未在本地sys_user中找到: {}", oaUserName);
        }
        return li;
    }

    // ==================== 领导查找（市本级） ====================

    /**
     * 市本级领导查找：
     * 总经理 → 副总经理 → 匹配表映射 → 映射后的总经理 → 映射后的副总经理 → 系统角色兜底
     */
    private List<LeaderInfo> findOrgLeaders(String countyId, String countyName, String areaName) {
        // 1. 查总经理
        List<LeaderInfo> leaders = queryLeaders(countyId, BOSS_POS_CODE, BOSS_FLAG_ORG);
        if (!leaders.isEmpty()) return leaders;

        // 2. 查副总经理
        leaders = queryLeaders(countyId, VICE_BOSS_POS_CODE, BOSS_FLAG_ORG);
        if (!leaders.isEmpty()) return leaders;

        // 3. 匹配表映射
        Map<String, Object> match = mapper.selectMatchOrgCode(countyId);
        if (match != null) {
            String mdmOrgCodeNew = getString(match.get("mdmOrgCodeNew"));
            if (mdmOrgCodeNew != null) {
                // 4. 映射后的总经理
                leaders = queryLeaders(mdmOrgCodeNew, BOSS_POS_CODE, BOSS_FLAG_ORG);
                if (!leaders.isEmpty()) return leaders;

                // 5. 映射后的副总经理
                leaders = queryLeaders(mdmOrgCodeNew, VICE_BOSS_POS_CODE, BOSS_FLAG_ORG);
                if (!leaders.isEmpty()) return leaders;
            }
        }

        // 6. 兜底：查询 sys_flow_user 配置的地市接口人（对应老系统 getWarnUpDeveloper）
        log.warn("【{}】市本级部门总经理和副总经理均未找到，使用地市接口人兜底, countyId={}, name={}",
                getOrderTypeName(), countyId, countyName);
        return convertToLeaders(warningStartService.findDefaultHandlerByProcessAndArea(
                getOrderType(), "leader", null));
    }

    // ==================== WarningRecord 构建 ====================

    private WarningRecord buildRecord(Long areaId,String custName, String bssCode, String mdmOrgCode,
                                       LeaderInfo leader, String yyyyMM, String orderFlag) {
        String codeInfo = mdmOrgCode != null ? mdmOrgCode : (bssCode != null ? bssCode : "");
        SysUsers users = new SysUsers();
        users.setId(leader.userId);
        users.setOrgId(leader.deptId);
        users.setNameCh(leader.fullName);
        WarningRecord record = new WarningRecord();
        record.setSysUser( users);
        record.setAreaId(areaId);
        record.setOrderType(getOrderType());
        record.setOrderName(DateUtils.dateTime()
            + custName + "_账期" + yyyyMM + "_" + getOrderTypeName());
      //  record.setCustName(custName);
        record.setCustManagerId(leader.userId);

        Map<String, Object> extra = new HashMap<>();
        extra.put("wide_second_new_orderFlag", orderFlag);
        extra.put("wide_second_new_codeInfo", codeInfo);
        extra.put("wide_second_new_period", yyyyMM);
        extra.put("orderFlag", orderFlag);
        extra.put("codeInfo", codeInfo);
        extra.put("bssCode", bssCode);
        extra.put("mdmOrgCode", mdmOrgCode);
        extra.put("_needOrderNumber", true);
        extra.put("orderCategory", "C");
        extra.put("manager_handle_name", "总经理/副总经理处理");
        if (leader.deptId != null) {
            extra.put("orgId", leader.deptId);
            extra.put("nickName", leader.nickName);
            extra.put("phone", leader.phone);
        }
        record.setExtraVariables(extra);

        return record;
    }

    @Override
    public String buildDealUrl(Long orderId, Map<String, Object> extra) {
        // 指向新框架前端 Vue 页面，taskId 由老框架待办列表拼接
        return "/wideWarning/deal?orderId=" + orderId;
    }

    // ==================== 辅助方法 ====================

    private String getString(Object v) {
        return v == null ? null : v.toString();
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        try {
            if (v instanceof Number) return ((Number) v).longValue();
            return Long.parseLong(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 领导信息 */
    private static class LeaderInfo {
        Long userId;
        Long deptId;
        String nickName;
        String phone;
        String oaUserName;
        String fullName;
    }
}
