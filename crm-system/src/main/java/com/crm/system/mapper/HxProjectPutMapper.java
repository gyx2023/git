package com.crm.system.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 划小数据推送工建系统 Mapper接口
 *
 * @author shenxy
 * @date 2022/2/23
 */
@Mapper
public interface HxProjectPutMapper {

    /**
     * 查询需求与合同关系
     *
     * @return 需求与合同关系列表
     */
    List<Map<String, Object>> getRemandContract();

    /**
     * 查询合同信息
     *
     * @return 合同信息列表
     */
    List<Map<String, Object>> getContractInfo();
}
