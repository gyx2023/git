package com.crm.system.mapper.accManager;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.dao.zbzq.RepSysPersonNewDDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 客户经理离职预警 - 跨数据源查询Mapper
 * <p>
 * 涉及三个数据源:
 * - slave_2 (DM/达梦): 人员信息、合同、维系人
 * - slave_1 (MySQL): 名单客户、自然客户、实体客户
 * - master (OceanBase): 商机
 *
 * @author lin_Yao
 * @date 2025/3/13
 */
@Mapper
public interface ResignEarlyWarningMapper {

    // ==================== DM/达梦查询 (slave_2) ====================

    /**
     * 从DM查询已离职人员 (ONJOB_FLAG='1' 表示离职)
     */
    @DS("slave_2")
    List<RepSysPersonNewDDo> selectResignedPersons();

    /**
     * 从DM查询合同的承办人/履行人
     */
    @DS("slave_2")
    List<Map<String, Object>> selectContractManagerByOaUser(@Param("oaUserName") String oaUserName);

    /**
     * 从DM查询基层维系人
     */
    @DS("slave_2")
    List<Map<String, Object>> selectContactPersonByOaUser(@Param("oaUserName") String oaUserName);

    /**
     * 从DM查询人员的在职状态
     */
    @DS("slave_2")
    RepSysPersonNewDDo selectUserStateByOaId(@Param("oaId") String oaId);

    // ==================== OB/MySQL查询 (slave_1) ====================

    /**
     * 从OB查询四级名单客户客户经理 A/B角
     */
    @DS("slave_1")
    List<Map<String, Object>> selectCustLevel4ManagerByOaUser(@Param("oaUserName") String oaUserName);

    /**
     * 从OB查询自然客户客户经理
     */
    @DS("slave_1")
    List<Map<String, Object>> selectNaturalCustManagerByOaUser(@Param("oaUserName") String oaUserName);

    /**
     * 从OB查询实体客户客户经理
     */
    @DS("slave_1")
    List<Map<String, Object>> selectEntityCustManagerByFullName(@Param("fullName") String fullName);

    // ==================== 主库查询 (master) ====================

    /**
     * 从主库查询商机的客户经理 (TOPP_OPPORTUNITY JOIN sys_user)
     */
    List<Map<String, Object>> selectOpportunityManagerByOaUser(@Param("oaUserName") String oaUserName);
}
