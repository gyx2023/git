package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.crm.system.domain.CustMaintInfo;
import com.crm.system.mapper.CustMaintInfoMapper;
import com.crm.system.service.ICustMaintInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户维护信息查询服务实现
 * 用于查询iob_gov2.CUST_MAINT_INFO表
 *
 * @author 肖英杰
 */
@Service
public class CustMaintInfoServiceImpl implements ICustMaintInfoService {

    @Autowired
    private CustMaintInfoMapper custMaintInfoMapper;

    /**
     * 根据user_id查询iob_gov2.CUST_MAINT_INFO表中status=1的记录
     *
     * @param userId 用户ID
     * @return 包含scv_num等信息的Map，如果用户不存在或状态不是1则返回null
     */
    @Override
    public Map<String, Object> getCustMaintInfoByUserId(String userId) {
        // 使用QueryWrapper构建查询条件
        QueryWrapper<CustMaintInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("USER_ID", userId).eq("STATUS", "1");

        List<CustMaintInfo> results = custMaintInfoMapper.selectList(queryWrapper);
        if (results != null && !results.isEmpty()) {
            // 取第一条记录
            CustMaintInfo info = results.get(0);
            Map<String, Object> result = new HashMap<>();
            result.put("svcNum", info.getSvcNum());           // 业务号码
            result.put("userId", info.getUserId());           // 用户编号
            result.put("custName", info.getCustName());       // 归属客户名称
            result.put("custGrpName", info.getCustGrpName()); // 收入归集集团名称
            return result;
        }
        return null;
    }

    /**
     * 批量根据user_id查询iob_gov2.CUST_MAINT_INFO表中status=1的记录
     *
     * @param userIds 用户ID列表
     * @return key为userId，value为包含scv_num等信息的Map
     */
    @Override
    public Map<String, Map<String, Object>> batchGetCustMaintInfoByUserIds(List<String> userIds) {
        Map<String, Map<String, Object>> resultMap = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return resultMap;
        }

        // 使用QueryWrapper构建查询条件
        QueryWrapper<CustMaintInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("STATUS", "1").in("USER_ID", userIds);

        List<CustMaintInfo> results = custMaintInfoMapper.selectList(queryWrapper);
        if (results != null && !results.isEmpty()) {
            for (CustMaintInfo info : results) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("svcNum", info.getSvcNum());           // 业务号码
                userInfo.put("userId", info.getUserId());           // 用户编号
                userInfo.put("custName", info.getCustName());       // 归属客户名称
                userInfo.put("custGrpName", info.getCustGrpName()); // 收入归集集团名称
                resultMap.put(info.getUserId(), userInfo);
            }
        }

        return resultMap;
    }

    @Override
    public List<CustMaintInfo> queryByCustGrpIdAndCustId(String custGrpId, String custId) {
        QueryWrapper<CustMaintInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("CUST_GRP_ID", custGrpId)
                    .eq("CUST_ID", custId)
                    .eq("STATUS", "1");
        return custMaintInfoMapper.selectList(queryWrapper);
    }
}
