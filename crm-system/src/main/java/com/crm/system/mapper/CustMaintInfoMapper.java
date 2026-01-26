package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.CustMaintInfo;
import com.crm.system.domain.vo.CustMaintInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
@DS("slave_2")  // 指定使用达梦数据库
public interface CustMaintInfoMapper extends BaseMapperPlus<CustMaintInfoMapper, CustMaintInfo, CustMaintInfoVo> {

    /**
     * 批量根据用户ID查询有效的客户维护信息及服务号码
     *
     * @param userIds 用户ID列表
     * @return key为userId，value为服务号码的Map
     */
    List<Map<String, Object>> selectValidSvcNumByUserIds(@Param("userIds") List<String> userIds);
}
