package com.crm.system.mapper.topp;

import com.crm.system.domain.topp.ToppContractDo;
import com.crm.system.domain.vo.ContractIncomeExcelVo;
import com.crm.system.domain.vo.topp.ToppContractVo;
import com.crm.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author crm
 * @date 2025-12-16
 */
public interface ToppContractMapper extends BaseMapperPlus<ToppContractMapper, ToppContractDo, ToppContractVo> {

    /**
     * 查询合同收入数据列表
     *
     * @param areaId 区域ID
     * @return 合同收入数据列表
     */
    List<ContractIncomeExcelVo> getContractIncomeList(@Param("areaId") Integer areaId);

}
