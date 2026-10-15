package com.crm.system.service;

import java.util.List;
import java.util.Map;

/**
 * 红名单到期预警(000030) 领导候选人 Service 接口
 * <p>
 * 复刻老框架 FtpTcUserCustWarningService.getLeaderList() 语义：客户经理"不延期"送领导时，
 * 返回该客户经理可选的领导名单（总经理→副总经理→地市接口人兜底，DWD 人员维度）。
 * </p>
 *
 * @author crm
 */
public interface IHmdExpireLeaderService {

    /**
     * 查询指定红名单工单当前/原客户经理可选的领导候选人列表
     *
     * @param orderId 红名单到期预警工单ID（topp_order.id_）
     * @return [{userId, userName}]；工单/经理缺失、领导未配置或全部 OA 未映射到本地 sys_user 时返回空列表
     */
    List<Map<String, Object>> listLeaders(Long orderId);
}
