package com.crm.system.mapper.topp;

import com.crm.system.domain.dao.topp.HxProjectDo;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 划小项目 Mapper
 */
@Mapper
public interface HxProjectMapper extends BaseMapperPlus<HxProjectMapper, HxProjectDo, HxProjectDo> {

    /**
     * 根据商机ID查询划小项目列表
     * 老系统 HQL: from HxProject where opportunity.id=?
     */
    default List<HxProjectDo> selectByOppoId(Long oppoId) {
        LambdaQueryWrapper<HxProjectDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(HxProjectDo::getOppoId, oppoId);
        return selectList(lqw);
    }
}
