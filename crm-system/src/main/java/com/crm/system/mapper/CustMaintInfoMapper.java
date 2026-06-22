package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.CustMaintInfo;
import com.crm.system.domain.vo.CustMaintInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
@DS("slave_2")  // 指定使用达梦数据库
public interface CustMaintInfoMapper extends BaseMapperPlus<CustMaintInfoMapper, CustMaintInfo, CustMaintInfoVo> {

    /**
     * 游标方式分页查询 - 第一页
     */
    List<CustMaintInfo> selectFirstPage(@Param("size") int size, @Param("params") String params);

    /**
     * 游标方式分页查询 - 后续页
     */
    List<CustMaintInfo> selectNextPage(@Param("lastEtlTime") String lastEtlTime,
                                       @Param("lastUserId") String lastUserId,
                                       @Param("size") int size,
                                       @Param("params") String params);
}
