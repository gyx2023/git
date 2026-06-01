package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.system.domain.dao.SysUserCustNo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统用户customer_no Mapper接口
 */
@Mapper
public interface SysUserCustNoMapper extends BaseMapper<SysUserCustNo> {

    /**
     * 根据用户ID查询客户编号列表
     */
    List<SysUserCustNo> selectListByUserId(@Param("userId") Integer userId);
}
