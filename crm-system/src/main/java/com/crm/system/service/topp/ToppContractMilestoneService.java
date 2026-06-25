package com.crm.system.service.topp;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.system.domain.dao.topp.ToppContractMilestoneDo;
import com.crm.system.domain.vo.topp.ToppContractMilestoneVo;
import com.crm.system.mapper.topp.ToppContractMilestoneMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 合同里程碑服务
 */
@Service
@RequiredArgsConstructor
public class ToppContractMilestoneService {

    private final ToppContractMilestoneMapper baseMapper;

    /**
     * 根据合同ID查询里程碑
     */
    public ToppContractMilestoneVo getByContractId(Long contractId) {
        ToppContractMilestoneDo milestone = baseMapper.selectByContractId(contractId);
        if (milestone == null) return null;
        return BeanUtil.copyProperties(milestone, ToppContractMilestoneVo.class);
    }

    /**
     * 保存或更新里程碑
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(ToppContractMilestoneDo milestone) {
        if (milestone.getId() != null) {
            baseMapper.updateById(milestone);
        } else {
            baseMapper.insert(milestone);
        }
    }

    /**
     * 根据合同ID删除里程碑
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByContractId(Long contractId) {
        LambdaQueryWrapper<ToppContractMilestoneDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(ToppContractMilestoneDo::getContractId, contractId);
        baseMapper.delete(lqw);
    }
}
