package com.crm.system.mapper.topp;

import com.crm.system.domain.topp.ToppContractDo;
import com.crm.system.domain.vo.ContractIncomeExcelVo;
import com.crm.system.domain.vo.ToppContractExcelVo;
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

    /**
     * 分页查询合同收入数据列表
     *
     * @param areaId 区域ID
     * @param offset 起始位置
     * @param limit  每页条数
     * @return 合同收入数据列表
     */
    List<ContractIncomeExcelVo> getContractIncomeListPaged(@Param("areaId") Integer areaId,
                                                           @Param("offset") int offset,
                                                           @Param("limit") int limit);

    /**
     * 导出合同统计数据
     *
     * @param areaId        区域ID
     * @param nowYear       当前年
     * @param lastYear      上一年
     * @param nowYearMonth  当前年月
     * @param lastYearMonth 上月年月
     * @return 合同统计数据列表
     */
    List<ToppContractExcelVo> exportContractList(
            @Param("areaId") Integer areaId,
            @Param("nowYear") Integer nowYear,
            @Param("lastYear") Integer lastYear,
            @Param("nowYearMonth") String nowYearMonth,
            @Param("lastYearMonth") String lastYearMonth
    );

    /**
     * 分页导出合同统计数据
     */
    List<ToppContractExcelVo> exportContractListPaged(
            @Param("areaId") Integer areaId,
            @Param("nowYear") Integer nowYear,
            @Param("lastYear") Integer lastYear,
            @Param("nowYearMonth") String nowYearMonth,
            @Param("lastYearMonth") String lastYearMonth,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

}
