package com.crm.job.service.warning;

import com.crm.system.service.warning.WarningRecord;

import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.utils.DateUtils;
import com.crm.job.mapper.warning.WarningUpCommonMapper;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.dto.BossLeaderDto;
import com.crm.system.domain.dto.LeaderInfoDto;
import com.crm.system.mapper.SysUsersMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 升级预警领导查找服务
 * <p>
 * 提取 HmdUpWarningStrategy 和 WideUpWarningStrategy 中完全重复的领导查找逻辑。
 * 通过 WarningUpCommonMapper 接口操作不同的 Mapper 实现。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarningLeaderService {

    private static final int BOSS_POS_CODE = 1;
    private static final int VICE_BOSS_POS_CODE = 2;

    private final SysUsersMapper sysUsersMapper;

    @Lazy
    @Resource
    private IWarningStartService warningStartService;

    /**
     * 区县领导查找：总经理 → 副总经理 → 地市接口人兜底
     */
    public List<LeaderInfoDto> findAreaLeaders(WarningUpCommonMapper mapper, String bssCode,
                                                String areaName, Long areaId,
                                                String orderType, String leaderTaskKey) {
        List<LeaderInfoDto> leaders = queryLeaders(mapper, bssCode, BOSS_POS_CODE, "1");
        if (!leaders.isEmpty()) return leaders;

        leaders = queryLeaders(mapper, bssCode, VICE_BOSS_POS_CODE, "1");
        if (!leaders.isEmpty()) return leaders;

        log.warn("区县总经理和副总经理均未找到，使用地市接口人兜底, area={}, bssCode={}", areaName, bssCode);
        return convertToLeaders(warningStartService.findDefaultHandlerByProcessAndArea(
                orderType, leaderTaskKey, areaId));
    }

    /**
     * 市本级领导查找：
     * 总经理 → 副总经理 → 匹配表映射 → 映射后的总经理 → 映射后的副总经理 → 系统角色兜底
     */
    public List<LeaderInfoDto> findOrgLeaders(WarningUpCommonMapper mapper, String countyId,
                                               String countyName, String areaName,
                                               String orderType, String leaderTaskKey) {
        List<LeaderInfoDto> leaders = queryLeaders(mapper, countyId, BOSS_POS_CODE, "2");
        if (!leaders.isEmpty()) return leaders;

        leaders = queryLeaders(mapper, countyId, VICE_BOSS_POS_CODE, "2");
        if (!leaders.isEmpty()) return leaders;

        java.util.Map<String, Object> match = mapper.selectMatchOrgCode(countyId);
        if (match != null) {
            String mdmOrgCodeNew = getString(match.get("mdmOrgCodeNew"));
            if (mdmOrgCodeNew != null) {
                leaders = queryLeaders(mapper, mdmOrgCodeNew, BOSS_POS_CODE, "2");
                if (!leaders.isEmpty()) return leaders;

                leaders = queryLeaders(mapper, mdmOrgCodeNew, VICE_BOSS_POS_CODE, "2");
                if (!leaders.isEmpty()) return leaders;
            }
        }

        log.warn("市本级部门总经理和副总经理均未找到，使用地市接口人兜底, countyId={}, name={}", countyId, countyName);
        return convertToLeaders(warningStartService.findDefaultHandlerByProcessAndArea(
                orderType, leaderTaskKey, null));
    }

    /**
     * 通过 OA 账号查找本地 sys_user，获取 user_id 和 dept_id
     */
    public LeaderInfoDto getUserByOa(String oaUserName) {
        LeaderInfoDto li = new LeaderInfoDto();
        li.setOaUserName(oaUserName);
        if (oaUserName == null) {
            return li;
        }
        try {
            SysUsers localUser = sysUsersMapper.findByOaAccount(oaUserName.toLowerCase());
            if (localUser != null) {
                li.setUserId(localUser.getId());
                li.setDeptId(localUser.getOrgId());
            }
        } catch (Exception e) {
            log.debug("OA用户未在本地sys_user中找到: {}", oaUserName);
        }
        return li;
    }

    // ==================== private helpers ====================

    private List<LeaderInfoDto> queryLeaders(WarningUpCommonMapper mapper, String code, int posCode, String bossFlag) {
        List<LeaderInfoDto> result = new ArrayList<>();
        List<BossLeaderDto> list = mapper.selectBossLeaders(code, posCode, bossFlag);
        for (BossLeaderDto row : list) {
            String oaUserName = row.getOaUserName();
            if (oaUserName != null) {
                result.add(getUserByOa(oaUserName));
            }
        }
        return result;
    }

    private List<LeaderInfoDto> convertToLeaders(List<SysUser> users) {
        List<LeaderInfoDto> result = new ArrayList<>();
        if (users == null) return result;
        for (SysUser user : users) {
            LeaderInfoDto li = new LeaderInfoDto();
            li.setUserId(user.getUserId());
            li.setDeptId(user.getDeptId());
            li.setNickName(user.getNickName());
            li.setPhone(user.getPhonenumber());
            result.add(li);
        }
        return result;
    }

    private String getString(Object v) {
        return v == null ? null : v.toString();
    }

    // ==================== WarningRecord 构建 ====================

    /**
     * 构建一条升级预警记录（HmdUp / WideUp 通用）
     */
    public WarningRecord buildRecord(Long areaId, String custName, String bssCode, String mdmOrgCode,
                                      LeaderInfoDto leader, String yyyyMM,
                                      String orderType, String orderTypeName, String orderFlag) {
        String codeInfo = mdmOrgCode != null ? mdmOrgCode : (bssCode != null ? bssCode : "");
        SysUsers users = new SysUsers();
        users.setId(leader.getUserId());
        users.setOrgId(leader.getDeptId());
        users.setNameCh(leader.getFullName());
        WarningRecord record = new WarningRecord();
        record.setSysUser(users);
        record.setAreaId(areaId);
        record.setOrderType(orderType);
        record.setOrderName(DateUtils.dateTime()
                + custName + "_账期" + yyyyMM + "_" + orderTypeName);
        record.setCustManagerId(leader.getUserId());

        Map<String, Object> extra = new HashMap<>();
        extra.put("wide_second_new_orderFlag", orderFlag);
        extra.put("wide_second_new_codeInfo", codeInfo);
        extra.put("wide_second_new_period", yyyyMM);
        extra.put("bssCode", bssCode);
        extra.put("mdmOrgCode", mdmOrgCode);
        extra.put("_needOrderNumber", true);
        extra.put("manager_handle_name", "总经理/副总经理处理");
        if (leader.getDeptId() != null) {
            extra.put("orgId", leader.getDeptId());
            extra.put("nickName", leader.getNickName());
            extra.put("phone", leader.getPhone());
        }
        record.setExtraVariables(extra);

        return record;
    }
}
