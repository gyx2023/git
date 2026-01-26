package com.crm.system.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.CustMaintInfo;
import com.crm.system.mapper.CustMaintInfoMapper;
import com.crm.system.service.ICustMaintInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户维护信息服务实现
 *
 * @author 万超凡
 * @date 2026-01-23
 */
@Service
public class CustMaintInfoServiceImpl implements ICustMaintInfoService {

    @Autowired
    private CustMaintInfoMapper custMaintInfoMapper;

    /**
     * 批量根据用户ID查询有效的客户维护信息及服务号码
     *
     * @param userIds 用户ID列表
     * @return key为userId，value为服务号码的Map
     */
    @Override
    @DS("slave_2")  // 使用达梦数据库
    public Map<String, String> selectValidSvcNumByUserIds(List<String> userIds) {

        List<Map<String, Object>> results = custMaintInfoMapper.selectValidSvcNumByUserIds(userIds);

        Map<String, String> resultMap = new HashMap<>();
        if (results != null) {
            for (Map<String, Object> result : results) {
                String userId = (String) result.get("key");
                String svcNum = (String) result.get("value");
                if (userId != null && svcNum != null) {
                    resultMap.put(userId, svcNum);
                }
            }
        }
        return resultMap;
    }

}
