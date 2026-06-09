package com.crm.system.service;

import com.crm.system.domain.dto.OppoSupportInfoDTO;

import java.util.List;

/**
 * 商机支撑信息Service接口
 */
public interface IOppoSupportInfoService {

    /**
     * 获取商机支撑信息列表
     *
     * @return 商机支撑信息列表
     */
    List<OppoSupportInfoDTO> selectOppoSupportList();
}
