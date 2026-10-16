package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.vo.ZqztOrderInfoObVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
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

    /** 增量扫描 fwdt_orderformat_uc_sc_sl08md（按创建时间窗口 + 地市白名单） */
    List<ZqztOrderInfoObVo> scanSl08md(@Param("start") Date start,
                                       @Param("end") Date end,
                                       @Param("cities") List<String> cities);

    /** 增量扫描 fwdt_orderformat_uc_sc_04049f（按创建时间窗口 + 地市白名单） */
    List<ZqztOrderInfoObVo> scan04049f(@Param("start") Date start,
                                       @Param("end") Date end,
                                       @Param("cities") List<String> cities);

    /**
     * 按工单号取「中台稽核」完成时间（作为台账签约日期，取不到返回 null）
     */
    Date selectAuditDealTime(@Param("orderNo") String orderNo);

    /** 扫描近 N 天内「已撤单 / 已终止」的工单号（两张表各查一次，Java 层合并） */
    List<String> scanCanceledOrderNosSl08md(@Param("since") Date since);

    /** 扫描近 N 天内「已撤单 / 已终止」的工单号（两张表各查一次，Java 层合并） */
    List<String> scanCanceledOrderNos04049f(@Param("since") Date since);
}
