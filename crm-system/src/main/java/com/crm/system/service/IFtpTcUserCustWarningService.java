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
}
