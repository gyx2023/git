package com.crm.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.domain.dao.FtpTcUserCommonWarningDo;

import java.util.List;

/**
 * 用户级通用预警 Service接口
 * <p>
 * 继承 MyBatis-Plus {@link IService}：按主键查询用自带的 {@code getById}，
 * 条件查询用 {@code lambdaQuery()}（沃TV/特殊活动/云业务三类共用本表）。
 * </p>
 *
 * @author crm
 */
public interface IFtpTcUserCommonWarningService extends IService<FtpTcUserCommonWarningDo> {

    /**
     * 批量插入用户级通用预警数据
     *
     * @param list 数据列表
     */
    void insertBatch(List<FtpTcUserCommonWarningDo> list);

    /**
     * 按主订单流水号查询用户级预警明细（一个客户级订单下多条）
     *
     * @param mainOrderFlowNo 主订单流水号
     * @return 明细列表（按 id_ 升序）
     */
    List<FtpTcUserCommonWarningDo> listByMainOrderFlowNo(String mainOrderFlowNo);

    /**
     * 按子订单流水号查询用户级预警记录（通常一行）
     *
     * @param orderFlowNo 子订单流水号
     * @return 记录，无则 null
     */
    FtpTcUserCommonWarningDo getByOrderFlowNo(String orderFlowNo);
}
