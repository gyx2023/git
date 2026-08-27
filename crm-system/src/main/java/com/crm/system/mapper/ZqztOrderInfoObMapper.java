package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.vo.ZqztOrderInfoObVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 总部OB工单源表 Mapper（slave_1 / zq_user_36）
 * 同一工单可能出现在 sl08md 或 04049f 中，各查一张表，Java 合并
 */
@Mapper
@DS("slave_1")
public interface ZqztOrderInfoObMapper {

    /** 查询 fwdt_orderformat_uc_sc_sl08md */
    List<ZqztOrderInfoObVo> selectFromSl08md(@Param("orderNo") String orderNo);

    /** 查询 fwdt_orderformat_uc_sc_04049f */
    List<ZqztOrderInfoObVo> selectFrom04049f(@Param("orderNo") String orderNo);
}
