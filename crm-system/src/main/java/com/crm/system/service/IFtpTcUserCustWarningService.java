package com.crm.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.domain.dao.FtpTcUserCustWarningDo;

import java.util.List;

/**
 * 客户级通用预警 Service接口
 * <p>
 * 继承 MyBatis-Plus {@link IService}：按主键查询用自带的 {@code getById}，
 * 条件查询用 {@code lambdaQuery()}（沃TV/特殊活动/云业务三类共用本表）。
 * </p>
 *
 * @author crm
 */
public interface IFtpTcUserCustWarningService extends IService<FtpTcUserCustWarningDo> {

    /**
     * 批量插入客户级通用预警数据
     *
     * @param list 数据列表
     */
    void insertBatch(List<FtpTcUserCustWarningDo> list);

    /**
     * 按主订单流水号查询客户级预警记录（同一主订单流水通常一行）
     *
     * @param mainOrderFlowNo 主订单流水号
     * @return 记录，无则 null
     */
    FtpTcUserCustWarningDo getByMainOrderFlowNo(String mainOrderFlowNo);

    /**
     * 按主键 id 取 MAIN_ORDER_FLOW_NO（只查一列，供详情类服务由 info_id 反查主订单流水号）
     *
     * @param id 客户级预警主键 id_（工单 topp_order.info_id）
     * @return 主订单流水号；记录不存在或该列为空时返回 null
     */
    String getMainOrderFlowNoById(Long id);

    /**
     * 按主键 id + 预警类型查询客户级预警行（指派策略按 detail_id 反查原始预警行用）
     *
     * @param id          客户级预警主键 id_（maintenance_detail.detail_id）
     * @param warningType 预警类型编码，如 C00001(云业务)/C00024(沃TV)/000032(特殊活动)
     * @return 记录，无则 null
     */
    FtpTcUserCustWarningDo getByIdAndWarningType(Long id, String warningType);

    /**
     * 按预警类型 + 预警创建日期（yyyyMMdd）查询待建单的客户级预警行列表。
     * <p>
     * 供各预警 Job 策略（云业务/沃TV/特殊活动/宽带装机地址等）扫描当日新增预警行，按 id_ 升序。
     * 日期条件沿用原 Mapper 写法：{@code DATE_FORMAT(create_date,'%Y%m%d') = yyyyMMdd}。
     * </p>
     *
     * @param warningType 预警类型编码，如 C00001(云业务)/C00023(宽带装机地址)/000032(特殊活动)/C00024(沃TV)
     * @param yyyyMMdd    目标日期，格式 yyyyMMdd
     * @return 预警行列表（按 id_ 升序），无则空列表
     */
    List<FtpTcUserCustWarningDo> listByWarningTypeAndCreateDate(String warningType, String yyyyMMdd);
}
