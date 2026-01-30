package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.GrpContractOtherInfo;
import com.crm.system.domain.vo.GrpContractOtherInfoVo;
import com.crm.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * GrpContractOtherInfo 表 Mapper 接口
 * 使用@DS("oracle")注解确保查询Oracle数据库
 *
 * @author 万超凡
 * @date 2026-01-23
 */
@Mapper
@DS("oracle")  // 指定使用Oracle数据库
public interface GrpContractOtherInfoMapper extends BaseMapperPlus<GrpContractOtherInfoMapper, GrpContractOtherInfo, GrpContractOtherInfoVo> {

    /**
     * 根据ID查询单条数据
     *
     * @param id ID
     * @return 扩展信息数据
     */
    GrpContractOtherInfo selectById(@Param("id") Long id);

    /**
     * 查询所有扩展信息数据
     *
     * @return 扩展信息数据列表
     */
    List<GrpContractOtherInfo> selectAll();

    /**
     * 根据合同ID查询
     *
     * @param contractId 合同ID
     * @return 扩展信息数据列表
     */
    List<GrpContractOtherInfo> selectByContractId(@Param("contractId") String contractId);

    /**
     * 根据信息标识查询
     *
     * @param infoTag 信息标识
     * @return 扩展信息数据列表
     */
    List<GrpContractOtherInfo> selectByInfoTag(@Param("infoTag") String infoTag);
}
