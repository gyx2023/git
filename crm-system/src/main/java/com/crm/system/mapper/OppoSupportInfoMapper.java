package com.crm.system.mapper;

import com.crm.system.domain.dto.OppoSupportInfoDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商机支撑信息Mapper接口
 */
@Mapper
public interface OppoSupportInfoMapper {

    /**
     * 获取商机支撑信息列表（当前年度的商机数据，按支撑经理关联用户信息）
     *
     * @return 商机支撑信息列表
     */
    List<OppoSupportInfoDTO> selectOppoSupportList();
}
