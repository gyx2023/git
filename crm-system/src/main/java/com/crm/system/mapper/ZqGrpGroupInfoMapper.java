package com.crm.system.mapper;

import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.vo.ZqGrpGroupInfoVo;
import com.crm.common.core.mapper.BaseMapperPlus;

/**
 * 政企集团客户信息Mapper接口
 *
 * @author crm
 * @date 2025-11-20
 */
public interface ZqGrpGroupInfoMapper extends BaseMapperPlus<ZqGrpGroupInfoMapper, ZqGrpGroupInfo, ZqGrpGroupInfoVo> {

    /**
     * 根据总部集团编码查询政企集团客户信息
     *
     * @param hqGrpCode 总部集团编码
     * @return 政企集团客户信息
     */
    default ZqGrpGroupInfo selectByHqGrpCode(String hqGrpCode) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ZqGrpGroupInfo>()
                .eq(ZqGrpGroupInfo::getHqGrpCode, hqGrpCode));
    }
}