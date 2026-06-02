package com.crm.system.mapper.topp;

import com.crm.system.domain.dao.topp.HxProjectContractDo;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 划小项目合同关联 Mapper
 */
@Mapper
public interface HxProjectContractMapper extends BaseMapperPlus<HxProjectContractMapper, HxProjectContractDo, HxProjectContractDo> {

    /**
     * 根据合同ID查询关联记录
     */
    default List<HxProjectContractDo> selectByContractId(Long contractId) {
        LambdaQueryWrapper<HxProjectContractDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(HxProjectContractDo::getContractId, contractId);
        lqw.eq(HxProjectContractDo::getIsValid, "1");
        return selectList(lqw);
    }

    /**
     * 逻辑删除（置 is_valid='0'）
     */
    default int updateInvalidById(Long id) {
        LambdaUpdateWrapper<HxProjectContractDo> lqw = Wrappers.lambdaUpdate();
        lqw.eq(HxProjectContractDo::getId, id);
        lqw.set(HxProjectContractDo::getIsValid, "0");
        return update(null, lqw);
    }
}
