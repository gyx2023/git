package com.crm.system.service.topp;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dao.topp.ToppContractIncludeTypeMoneyDo;
import com.crm.system.domain.vo.topp.ToppContractIncludeTypeMoneyVo;
import com.crm.system.mapper.topp.ToppContractIncludeTypeMoneyMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 合同分项收入明细服务
 * 老系统对照：ContractIncludeTypeMoneyService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToppContractIncludeTypeMoneyService {

    private final ToppContractIncludeTypeMoneyMapper baseMapper;

    /**
     * 保存分项收入（对照老系统 saveTypeMoney line 25）
     * 先删除旧数据，再解析 includeTypeMoney 字符串插入新数据
     *
     * @param includeTypeMoney 格式: "code__money,code__money,..."
     * @param contractId 合同ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveTypeMoney(String includeTypeMoney, Long contractId) {
        // 1. 删除该合同旧的明细记录
        deleteMoneyByContractId(contractId);

        // 2. 解析并插入新记录
        if (StringUtils.isNotBlank(includeTypeMoney)) {
            String[] includeTypes = includeTypeMoney.split(",");
            for (String json : includeTypes) {
                String[] parts = json.split("__");
                if (parts.length != 2) continue;

                String code = parts[0];
                String money = parts[1];

                ToppContractIncludeTypeMoneyDo citm = new ToppContractIncludeTypeMoneyDo();
                citm.setContractId(contractId);
                citm.setIncludeType(code);
                // 老系统：单位由万元改成元，入库时除以10000
                try {
                    citm.setValue(new BigDecimal(money).divide(new BigDecimal("10000")));
                } catch (NumberFormatException e) {
                    log.warn("分项收入金额格式错误: money={}", money);
                    continue;
                }
                baseMapper.insert(citm);
            }
        }
    }

    /**
     * 根据合同ID删除明细（对照老系统 deleteMoneyByContractId line 60）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMoneyByContractId(Long contractId) {
        LambdaQueryWrapper<ToppContractIncludeTypeMoneyDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(ToppContractIncludeTypeMoneyDo::getContractId, contractId);
        baseMapper.delete(lqw);
    }

    /**
     * 根据合同ID查询明细（对照老系统 getCitmByContractId line 51）
     */
    public List<ToppContractIncludeTypeMoneyVo> getByContractId(Long contractId) {
        LambdaQueryWrapper<ToppContractIncludeTypeMoneyDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(ToppContractIncludeTypeMoneyDo::getContractId, contractId);
        List<ToppContractIncludeTypeMoneyDo> list = baseMapper.selectList(lqw);
        return BeanUtil.copyToList(list, ToppContractIncludeTypeMoneyVo.class);
    }
}
