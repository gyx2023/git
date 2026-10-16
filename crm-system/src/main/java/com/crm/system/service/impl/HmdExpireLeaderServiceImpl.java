package com.crm.system.service.impl;

import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.dao.oldsyssyn.OldSysOrgDo;
import com.crm.system.domain.dto.BossLeaderDto;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.mapper.HmdExpireLeaderMapper;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.mapper.oldsyssyn.OldSysOrgMapper;
import com.crm.system.service.IHmdExpireLeaderService;
import com.crm.system.service.ISysAreaService;
import com.crm.system.service.topp.IToppOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 红名单到期预警(000030) 领导候选人 Service 实现
 * <p>
 * 链路（复刻老框架 FtpTcUserCustWarningService.getLeaderList，L1468-1516）：
 * 流程当前客户经理（流程变量 manager → 当前 manager_handle 任务 assignee → 兜底工单 cust_manager_id）
 * → sys_user → sys_user.org_id → sys_org.area_id → sys_area：
 * <ul>
 *   <li>区县（type=三级/3）：以 area.bss_code 走 dwd_rep_gw_pst_d 领导查询 bossFlag=1，总经理(1)→副总经理(2)；</li>
 *   <li>地市/市本级（其他）：按经理 OA 到 dwd_rep_sys_person_d 取 COUNTY_ID，走 bossFlag=2 总经理(1)→副总经理(2)，
 *       再 countyId → jingfen_zero_leader_org_match 映射 mdmOrgCodeNew → 映射后总经理→副总经理。</li>
 * </ul>
 * 总经理档与副总经理档（市本级另有匹配表映射档）<b>取并集</b>一起返回，与老框架
 * getLeaderList（L1468-1516 对 GM/VGM/映射 GM/映射 VGM 逐档 addAll）一致；按 userId 去重。
 * DWD 领导 OA 需能在本地 sys_user（sysUsersMapper.findByOaAccount）命中才可作为候选人下发。
 * 地市接口人兜底（job-server WarningStartService.findDefaultHandlerByProcessAndArea 依赖 flow_user 流程配置，
 * 不在 crm-system 可达）未实现：全部档位为空时记日志并返回空列表。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HmdExpireLeaderServiceImpl implements IHmdExpireLeaderService {

    /** 总经理职位代码 */
    private static final int BOSS_POS_CODE = 1;
    /** 副总经理职位代码 */
    private static final int VICE_BOSS_POS_CODE = 2;
    /** 区县领导查询标志（对应 getBossList bossFlag=1，code 为 bss_code） */
    private static final String BOSS_FLAG_AREA = "1";
    /** 市本级组织部门领导查询标志（对应 getBossList bossFlag=2，code 为 COUNTY_ID/MDM_ORG_CODE） */
    private static final String BOSS_FLAG_ORG = "2";

    private final IToppOrderService orderService;
    private final SysUsersMapper sysUsersMapper;
    private final OldSysOrgMapper oldSysOrgMapper;
    private final ISysAreaService sysAreaService;
    private final HmdExpireLeaderMapper leaderMapper;
    private final RuntimeService runtimeService;
    private final TaskService taskService;

    @Override
    public List<Map<String, Object>> listLeaders(Long orderId) {
        if (orderId == null) {
            log.warn("【hmd_warn_leader】orderId 为空，返回空领导名单");
            return Collections.emptyList();
        }
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null) {
            log.warn("【hmd_warn_leader】工单不存在: orderId={}", orderId);
            return Collections.emptyList();
        }
        // 客户经理基准：对齐老框架"流程上一处理人"语义（getLeaderList → queryPrevAssigneeWithUnEnd），
        // 取流程当前 manager_handle 处理人，而非工单 cust_manager_id / creator_
        Long managerUserId = resolveCurrentManagerUserId(order);
        if (managerUserId == null) {
            log.warn("【hmd_warn_leader】工单无当前客户经理: orderId={}", orderId);
            return Collections.emptyList();
        }
        SysUsers manager = sysUsersMapper.selectById(managerUserId);
        if (manager == null) {
            log.warn("【hmd_warn_leader】客户经理本地用户不存在: orderId={}, managerUserId={}", orderId, managerUserId);
            return Collections.emptyList();
        }
        log.info("【hmd_warn_leader】开始获取领导: orderId={}, managerUserId={}, oaAccount={}, orgId={}",
                orderId, managerUserId, manager.getOaAccount(), manager.getOrgId());

        // 1. 解析客户经理归属区域（SysUsers.org_id → sys_org.area_id → sys_area）
        String areaType = null;
        String bssCode = null;
        if (manager.getOrgId() != null) {
            OldSysOrgDo org = oldSysOrgMapper.selectById(manager.getOrgId());
            if (org != null && org.getAreaId() != null) {
                SysAreaVo area = sysAreaService.queryById(Long.valueOf(org.getAreaId()));
                if (area != null) {
                    areaType = area.getType();
                    bssCode = area.getBssCode();
                }
            }
        }
        log.debug("【hmd_warn_leader】客户经理归属区域: areaType={}, bssCode={}", areaType, bssCode);

        // 2. 区县（三级）→ bss_code 走区县领导；地市/市本级（其余）→ 按 OA 走 DWD 人员 COUNTY_ID
        if (isCountyLevel(areaType)) {
            if (StringUtils.isBlank(bssCode)) {
                log.warn("【hmd_warn_leader】客户经理归属区县但 bss_code 为空: orderId={}, managerUserId={}", orderId, managerUserId);
                return Collections.emptyList();
            }
            return areaLeaders(bssCode);
        }
        return orgLeaders(manager.getOaAccount());
    }

    /**
     * 解析工单"当前客户经理"，对齐老框架 {@code ProcessHelper.queryPrevAssigneeWithUnEnd}
     * （取流程中最近一个 hmd_warn_manager_deal 任务的 assignee）。
     * <p>
     * 顺序：流程变量 {@code manager}（hmd_warn_approval.bpmn 中 manager_handle 的处理人，
     * 转派时由 {@code ToppOrderServiceImpl.transferOrder} 覆盖为新经理）→ 当前 manager_handle 任务 assignee
     * → 工单 cust_manager_id。
     * </p>
     * <p>
     * 不使用 creator_：老框架建单时 creator 落的是系统账号（sysUserService.get(1)），
     * 作为客户经理基准会取错人，进而算错领导名单。
     * </p>
     */
    private Long resolveCurrentManagerUserId(ToppOrderVo order) {
        String piId = order.getPiId();
        if (StringUtils.isNotBlank(piId)) {
            try {
                Long fromVariable = toUserId(runtimeService.getVariable(piId, "manager"));
                if (fromVariable != null) {
                    return fromVariable;
                }
            } catch (Exception e) {
                log.debug("【hmd_warn_leader】读取流程变量 manager 失败: piId={}", piId, e);
            }
            try {
                List<Task> tasks = taskService.createTaskQuery()
                        .processInstanceId(piId)
                        .taskDefinitionKey("manager_handle")
                        .orderByTaskCreateTime().desc()
                        .listPage(0, 1);
                if (!tasks.isEmpty()) {
                    Long fromTask = toUserId(tasks.get(0).getAssignee());
                    if (fromTask != null) {
                        return fromTask;
                    }
                }
            } catch (Exception e) {
                log.debug("【hmd_warn_leader】查询当前 manager_handle 任务失败: piId={}", piId, e);
            }
        }
        // 流程不可用/已结束时兜底用工单客户经理（不使用 creator_）
        return order.getCustManagerId();
    }

    /**
     * 流程变量/任务 assignee 可能是",,123,,"形式，统一清洗为 Long。
     */
    private Long toUserId(Object value) {
        if (value == null) {
            return null;
        }
        String s = value.toString().replace(",", "").trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ==================== 区县：总经理 ∪ 副总经理 ====================

    private List<Map<String, Object>> areaLeaders(String bssCode) {
        List<Map<String, Object>> leaders = new ArrayList<>();
        leaders.addAll(queryLeaders(bssCode, BOSS_POS_CODE, BOSS_FLAG_AREA));
        leaders.addAll(queryLeaders(bssCode, VICE_BOSS_POS_CODE, BOSS_FLAG_AREA));
        List<Map<String, Object>> result = dedupe(leaders);
        if (result.isEmpty()) {
            log.warn("【hmd_warn_leader】区县总经理和副总经理均未找到(或无本地用户)，返回空: bssCode={}", bssCode);
        }
        return result;
    }

    // ==================== 地市/市本级：总经理 ∪ 副总经理 ∪ (匹配表映射总经理 ∪ 映射副总经理) ====================

    private List<Map<String, Object>> orgLeaders(String oaAccount) {
        if (StringUtils.isBlank(oaAccount)) {
            log.warn("【hmd_warn_leader】地市客户经理 OA 为空，无法定位 COUNTY_ID，返回空");
            return Collections.emptyList();
        }
        Map<String, Object> person = leaderMapper.selectPersonCountyByOa(oaAccount);
        String countyId = person != null ? getString(person.get("countyId")) : null;
        if (StringUtils.isBlank(countyId)) {
            log.warn("【hmd_warn_leader】OA 未在 dwd_rep_sys_person_d 找到，返回空: oaAccount={}", oaAccount);
            return Collections.emptyList();
        }
        List<Map<String, Object>> leaders = new ArrayList<>();
        leaders.addAll(queryLeaders(countyId, BOSS_POS_CODE, BOSS_FLAG_ORG));
        leaders.addAll(queryLeaders(countyId, VICE_BOSS_POS_CODE, BOSS_FLAG_ORG));
        Map<String, Object> match = leaderMapper.selectMatchOrgCode(countyId);
        if (match != null) {
            String mdmOrgCodeNew = getString(match.get("mdmOrgCodeNew"));
            if (StringUtils.isNotBlank(mdmOrgCodeNew)) {
                leaders.addAll(queryLeaders(mdmOrgCodeNew, BOSS_POS_CODE, BOSS_FLAG_ORG));
                leaders.addAll(queryLeaders(mdmOrgCodeNew, VICE_BOSS_POS_CODE, BOSS_FLAG_ORG));
            }
        }
        List<Map<String, Object>> result = dedupe(leaders);
        if (result.isEmpty()) {
            log.warn("【hmd_warn_leader】市本级总经理/副总经理/映射领导均未找到(或无本地用户)，返回空: oaAccount={}, countyId={}",
                    oaAccount, countyId);
        }
        return result;
    }

    // ==================== 公共查询与转换 ====================

    /**
     * 按 code/职位/flag 查 DWD 领导，并将 OA 映射到本地 sys_user。
     * 仅返回可落地的候选人（OA 在本地 sys_user 命中），保证前端选中后可作为任务处理人。
     */
    private List<Map<String, Object>> queryLeaders(String code, int posCode, String bossFlag) {
        List<BossLeaderDto> rows = leaderMapper.selectBossLeaders(code, posCode, bossFlag);
        List<Map<String, Object>> result = new ArrayList<>();
        Set<String> seenOa = new HashSet<>();
        for (BossLeaderDto row : rows) {
            String oa = row.getOaUserName();
            if (StringUtils.isBlank(oa) || !seenOa.add(oa.toLowerCase())) {
                continue;
            }
            SysUsers local;
            try {
                local = sysUsersMapper.findByOaAccount(oa.toLowerCase());
            } catch (Exception e) {
                log.warn("【hmd_warn_leader】查询本地 sys_user 异常，跳过该 OA: oa={}", oa, e);
                continue;
            }
            if (local == null || local.getId() == null) {
                log.debug("【hmd_warn_leader】DWD 领导 OA 无本地 sys_user，跳过: oa={}", oa);
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("userId", local.getId());
            item.put("userName", displayName(local, row));
            result.add(item);
        }
        return result;
    }

    private String displayName(SysUsers local, BossLeaderDto row) {
        if (local != null && StringUtils.isNotBlank(local.getNameCh())) {
            return local.getNameCh();
        }
        if (local != null && StringUtils.isNotBlank(local.getName())) {
            return local.getName();
        }
        return row != null ? row.getFullName() : null;
    }

    /** 新库 sys_area.type_ 兼容 "三级" 与 "3" */
    private boolean isCountyLevel(String type) {
        return "三级".equals(type) || "3".equals(type);
    }

    /** 多档（总经理/副总经理/映射档）并集后按 userId 去重，保持档内 DWD 返回顺序 */
    private List<Map<String, Object>> dedupe(List<Map<String, Object>> leaders) {
        if (leaders.isEmpty()) {
            return leaders;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (Map<String, Object> item : leaders) {
            Object uid = item.get("userId");
            if (uid instanceof Number) {
                if (!seen.add(((Number) uid).longValue())) {
                    continue;
                }
            }
            result.add(item);
        }
        return result;
    }

    private String getString(Object v) {
        return v == null ? null : v.toString();
    }
}
