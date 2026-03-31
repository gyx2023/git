package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.vo.ZqGrpGroupInfoVo;

import java.util.List;

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
        return selectOne(new LambdaQueryWrapper<ZqGrpGroupInfo>()
            .eq(ZqGrpGroupInfo::getHqGrpCode, hqGrpCode));
    }

    /**
     * 全表删除zqzt_grp_group_info表数据
     *
     * @return 删除的记录数
     */
    int truncateZqGrpGroupInfo();

    /**
     * 从grp_group_info同步数据到zqzt_grp_group_info
     *
     * @return 同步的记录数
     */
    int syncFromGrpGroupInfo();

    /**
     * 获取待更新数据
     *
     * @param createDate 创建时间
     * @return 待更新列表
     */
    List<ZqGrpGroupInfo> getUpdQuery(String createDate);
}
