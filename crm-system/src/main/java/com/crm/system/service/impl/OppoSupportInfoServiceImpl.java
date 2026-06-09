package com.crm.system.service.impl;

import com.crm.system.domain.dto.OppoSupportInfoDTO;
import com.crm.system.mapper.OppoSupportInfoMapper;
import com.crm.system.service.IOppoSupportInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 商机支撑信息Service业务层处理
 */
@Slf4j
@Service
public class OppoSupportInfoServiceImpl implements IOppoSupportInfoService {

    @Resource
    private OppoSupportInfoMapper oppoSupportInfoMapper;

    /**
     * 获取商机支撑信息列表
     *
     * @return 商机支撑信息列表
     */
    @Override
    public List<OppoSupportInfoDTO> selectOppoSupportList() {
        return oppoSupportInfoMapper.selectOppoSupportList();
    }
}
