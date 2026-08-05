package com.crm.system.service.impl;

import com.crm.system.domain.dao.zbzq.RepSysPersonNewDDo;
import com.crm.system.mapper.accManager.ResignEarlyWarningMapper;
import com.crm.system.service.IResignEarlyWarningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 客户经理离职预警Service业务层处理
 *
 * @author lin_Yao
 * @date 2025/3/13
 */
@Slf4j
@Service
public class ResignEarlyWarningServiceImpl implements IResignEarlyWarningService {

    @Resource
    private ResignEarlyWarningMapper resignEarlyWarningMapper;

    /**
     * 从DM查询已离职人员 (ONJOB_FLAG='1' 表示离职)
     */
    @Override
    public List<RepSysPersonNewDDo> selectResignedPersons() {
        return resignEarlyWarningMapper.selectResignedPersons();
    }

    /**
     * 从DM查询合同的承办人/履行人
     */
    @Override
    public List<Map<String, Object>> selectContractManagerByOaUser(String oaUserName) {
        return resignEarlyWarningMapper.selectContractManagerByOaUser(oaUserName);
    }

    /**
     * 从DM查询基层维系人
     */
    @Override
    public List<Map<String, Object>> selectContactPersonByOaUser(String oaUserName) {
        return resignEarlyWarningMapper.selectContactPersonByOaUser(oaUserName);
    }

    /**
     * 从DM查询人员的在职状态
     */
    @Override
    public RepSysPersonNewDDo selectUserStateByOaId(String oaId) {
        return resignEarlyWarningMapper.selectUserStateByOaId(oaId);
    }

    /**
     * 从OB查询四级名单客户客户经理 A/B角
     */
    @Override
    public List<Map<String, Object>> selectCustLevel4ManagerByOaUser(String oaUserName) {
        return resignEarlyWarningMapper.selectCustLevel4ManagerByOaUser(oaUserName);
    }

    /**
     * 从OB查询自然客户客户经理
     */
    @Override
    public List<Map<String, Object>> selectNaturalCustManagerByOaUser(String oaUserName) {
        return resignEarlyWarningMapper.selectNaturalCustManagerByOaUser(oaUserName);
    }

    /**
     * 从OB查询实体客户客户经理
     */
    @Override
    public List<Map<String, Object>> selectEntityCustManagerByFullName(String fullName) {
        return resignEarlyWarningMapper.selectEntityCustManagerByFullName(fullName);
    }

    /**
     * 从主库查询商机的客户经理 (TOPP_OPPORTUNITY JOIN sys_user)
     */
    @Override
    public List<Map<String, Object>> selectOpportunityManagerByOaUser(String oaUserName) {
        return resignEarlyWarningMapper.selectOpportunityManagerByOaUser(oaUserName);
    }
}
