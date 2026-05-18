package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.system.domain.PersonnelInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 人员信息Mapper接口
 */
@Mapper
public interface PersonnelInfoMapper extends BaseMapper<PersonnelInfo> {

    /**
     * 根据用户ID查询人员信息
     */
    PersonnelInfo selectByUserId(@Param("userId") String userId);
}
