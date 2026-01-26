package com.crm.system.service;

import com.crm.system.domain.CustMaintInfo;

import java.util.List;
import java.util.Map;

/**
 * 客户维护信息服务接口
 *
 * @author 万超凡
 * @date 2026-01-23
 */
public interface ICustMaintInfoService {

    /**
     * 批量根据用户ID查询有效的客户维护信息及服务号码
     *
     * @param userIds 用户ID列表
     * @return key为userId，value为服务号码的Map
     */
    Map<String, String> selectValidSvcNumByUserIds(List<String> userIds);
}
