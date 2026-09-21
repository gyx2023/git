package com.crm.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.domain.dao.FtpTcCustCycleWarningDo;

import java.util.List;

/**
 * 客户级闭环预警 Service接口
 * <p>
 * 继承 MyBatis-Plus {@link IService}：按主键查询用自带的 {@code getById}，
 * 条件查询用 {@code lambdaQuery()}。
 * </p>
 *
 * @author crm
 */
public interface IFtpTcCustCycleWarningService extends IService<FtpTcCustCycleWarningDo> {

    /**
     * 批量插入客户级闭环预警数据
     *
     * @param list 数据列表
     */
    void insertBatch(List<FtpTcCustCycleWarningDo> list);

    /**
     * 按主订单流水号查询客户级闭环记录（取最新一条）
     *
     * @param mainOrderFlowNo 主订单流水号
     * @return 记录，无则 null
     */
    FtpTcCustCycleWarningDo getLatestByMainOrderFlowNo(String mainOrderFlowNo);
}
