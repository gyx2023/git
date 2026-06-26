package com.crm.system.service.impl;

import com.crm.system.mapper.HxProjectPutMapper;
import com.crm.system.service.IHxProjectPutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 划小数据推送工建系统 Service业务层处理
 *
 * @author shenxy
 * @date 2022/2/23
 */
@Slf4j
@Service
public class HxProjectPutServiceImpl implements IHxProjectPutService {

    @Resource
    private HxProjectPutMapper hxProjectPutMapper;

    /**
     * 查询需求与合同关系
     *
     * @return 需求与合同关系列表
     */
    @Override
    public List<Map<String, Object>> getRemandContract() {
        return hxProjectPutMapper.getRemandContract();
    }

    /**
     * 查询合同信息
     *
     * @return 合同信息列表
     */
    @Override
    public List<Map<String, Object>> getContractInfo() {
        return hxProjectPutMapper.getContractInfo();
    }
}
