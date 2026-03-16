package com.crm.system.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.system.domain.bo.ZqztDwdContractsBo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 政企合同数据 Mapper
 *
 * @author tjx
 * @date 2026-03-05
 */
public interface ZqztDwdContractsMapper extends BaseMapper<ZqztDwdContractsBo> {

    int batchInsert(@Param("list") List<ZqztDwdContractsBo> list);

    int deleteByStorageType(@Param("storageType") String storageType);

}
