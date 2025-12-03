package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.vo.ZqGrpGroupInfoVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 政企集团客户信息Mapper接口
 *
 * @author crm
 * @date 2025-11-20
 */
public interface ZqGrpGroupInfoMapper extends BaseMapperPlus<ZqGrpGroupInfoMapper, ZqGrpGroupInfo, ZqGrpGroupInfoVo> {

    /**
     * 根据hqGrpCode查询政企集团客户信息
     *
     * @param hqGrpCode 总部集团编码
     * @return 政企集团客户信息
     */
    ZqGrpGroupInfo selectByHqGrpCode(String hqGrpCode);
}