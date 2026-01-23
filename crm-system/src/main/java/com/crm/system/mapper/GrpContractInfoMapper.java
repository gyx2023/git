package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.GrpContractInfo;
import com.crm.system.domain.vo.GrpContractInfoVo;
import com.crm.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * GrpContractInfo 表 Mapper 接口
 * 使用@DS("oracle")注解确保查询Oracle数据库
 *
 * @author 万超凡
 * @date 2026-01-23
 */
@Mapper
@DS("oracle")  // 指定使用Oracle数据库
public interface GrpContractInfoMapper extends BaseMapperPlus<GrpContractInfoMapper, GrpContractInfo, GrpContractInfoVo> {

    /**
     * 根据合同ID查询单条数据
     *
     * @param contractId 合同ID
     * @return 合同数据
     */
    GrpContractInfo selectByContractId(@Param("contractId") String contractId);

    /**
     * 查询所有合同数据
     *
     * @return 合同数据列表
     */
    List<GrpContractInfo> selectAll();

    /**
     * 根据集团ID查询
     *
     * @param grpId 集团ID
     * @return 合同数据列表
     */
    List<GrpContractInfo> selectByGrpId(@Param("grpId") Long grpId);
}
