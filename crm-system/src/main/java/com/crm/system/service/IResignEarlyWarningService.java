package com.crm.system.service;

import com.crm.system.domain.dao.zbzq.RepSysPersonNewDDo;

import java.util.List;
import java.util.Map;

/**
 * 客户经理离职预警Service接口
 *
 * @author lin_Yao
 * @date 2025/3/13
 */
public interface IResignEarlyWarningService {

    // ==================== DM/达梦查询 (slave_2) ====================

    /**
     * 从DM查询已离职人员 (ONJOB_FLAG='1' 表示离职)
     */
    List<RepSysPersonNewDDo> selectResignedPersons();

    /**
     * 从DM查询合同的承办人/履行人
     */
    List<Map<String, Object>> selectContractManagerByOaUser(String oaUserName);

    /**
     * 从DM查询基层维系人
     */
    List<Map<String, Object>> selectContactPersonByOaUser(String oaUserName);

    /**
     * 从DM查询人员的在职状态
     */
    RepSysPersonNewDDo selectUserStateByOaId(String oaId);

    // ==================== OB/MySQL查询 (slave_1) ====================

    /**
     * 从OB查询四级名单客户客户经理 A/B角
     */
    List<Map<String, Object>> selectCustLevel4ManagerByOaUser(String oaUserName);

    /**
     * 从OB查询自然客户客户经理
     */
    List<Map<String, Object>> selectNaturalCustManagerByOaUser(String oaUserName);

    /**
     * 从OB查询实体客户客户经理
     */
    List<Map<String, Object>> selectEntityCustManagerByFullName(String fullName);

    // ==================== 主库查询 (master) ====================

    /**
     * 从主库查询商机的客户经理 (TOPP_OPPORTUNITY JOIN sys_user)
     */
    List<Map<String, Object>> selectOpportunityManagerByOaUser(String oaUserName);
}
