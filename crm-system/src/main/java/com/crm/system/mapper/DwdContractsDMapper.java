package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.DwdContractsDDo;
import com.crm.system.domain.vo.DwdContractsDDoVo;
import com.crm.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * DWD_CONTRACTS_D 表 Mapper 接口
 * 使用@DS("dameng")注解确保查询达梦数据库
 *
 * @author 万超凡
 * @date 2026-01-19
 */
@Mapper
@DS("dameng")  // 指定使用达梦数据库
public interface DwdContractsDMapper extends BaseMapperPlus<DwdContractsDMapper, DwdContractsDDo, DwdContractsDDoVo> {

    /**
     * 根据流水号查询单条数据
     *
     * @param arrairNo 业务流水号
     * @return 合同数据
     */
    DwdContractsDDo selectByArrairNo(@Param("arrairNo") String arrairNo);

    /**
     * 查询所有合同数据
     *
     * @return 合同数据列表
     */
    List<DwdContractsDDo> selectAll();

    /**
     * 根据合同名称查询
     *
     * @param contractName 合同名称
     * @return 合同数据
     */
    DwdContractsDDo selectByContractName(String contractName);

    /**
     * 根据客户编码模糊查询
     *
     * @param customerCode 客户编码
     * @return 合同数据列表
     */
    List<DwdContractsDDo> selectByCustomerCode(String customerCode);

    /**
     * 根据地区名称查询
     *
     * @param areaName 地区名称
     * @return 合同数据列表
     */
    List<DwdContractsDDo> selectByAreaName(String areaName);

    /**
     * 根据合同类型查询
     *
     * @param contractType 合同类型
     * @return 合同数据列表
     */
    List<DwdContractsDDo> selectByContractType(String contractType);
}
