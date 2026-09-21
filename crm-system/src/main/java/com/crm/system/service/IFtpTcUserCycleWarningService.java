package com.crm.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.domain.dao.FtpTcUserCycleWarningDo;

import java.util.List;

/**
 * 用户级闭环预警 Service接口
 * <p>
 * 继承 MyBatis-Plus {@link IService}：按主键查询用自带的 {@code getById}，
 * 条件查询用 {@code lambdaQuery()}。
 * </p>
 *
 * @author crm
 */
public interface IFtpTcUserCycleWarningService extends IService<FtpTcUserCycleWarningDo> {

    /**
     * 批量插入用户级闭环预警数据
     *
     * @param list 数据列表
     */
    void insertBatch(List<FtpTcUserCycleWarningDo> list);

    /**
     * 按子订单流水号查询用户级闭环记录（取最新一条）
     *
     * @param orderFlowNo 子订单流水号
     * @return 记录，无则 null
     */
    FtpTcUserCycleWarningDo getLatestByOrderFlowNo(String orderFlowNo);
}
