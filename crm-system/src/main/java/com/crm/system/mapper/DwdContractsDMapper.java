package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.DwdContractsDDo;
import com.crm.system.domain.vo.DwdContractsDVo;
import com.crm.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * DWD_CONTRACTS_D 表 Mapper 接口
 * 使用@DS("slave_2")注解确保查询达梦数据库
 *
 * @author 万超凡
 * @date 2026-01-19
 */
@Mapper
@DS("slave_2")  // 指定使用达梦数据库
public interface DwdContractsDMapper extends BaseMapperPlus<DwdContractsDMapper, DwdContractsDDo, DwdContractsDVo> {

    /**
     * 根据流水号查询单条数据
     *
     * @param arrairNo 业务流水号
     * @return 合同数据
     */
    DwdContractsDDo selectByArrairNo(@Param("arrairNo") String arrairNo);
}
