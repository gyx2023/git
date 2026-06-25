package com.crm.system.mapper.topp;

import com.crm.system.domain.dao.topp.ToppContractMilestoneDo;
import com.crm.system.domain.vo.topp.ToppContractMilestoneVo;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

/**
 * 合同里程碑 Mapper
 */
@Mapper
public interface ToppContractMilestoneMapper extends BaseMapperPlus<ToppContractMilestoneMapper, ToppContractMilestoneDo, ToppContractMilestoneVo> {

    /**
     * 根据合同ID查询里程碑
     */
    default ToppContractMilestoneDo selectByContractId(Long contractId) {
        LambdaQueryWrapper<ToppContractMilestoneDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(ToppContractMilestoneDo::getContractId, contractId);
        return selectOne(lqw);
    }
}
