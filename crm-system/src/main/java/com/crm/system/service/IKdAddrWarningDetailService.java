package com.crm.system.service;

import java.util.List;
import java.util.Map;

/**
 * 宽带装机地址预警明细 Service
 *
 * @author crm
 */
public interface IKdAddrWarningDetailService {

    /**
     * 根据工单ID查询预警明细记录列表
     * <p>
     * 流程：topp_order.infoId → ftp_tc_user_cust_warning.main_order_flow_no → ftp_tc_user_common_warning 明细
     * </p>
     *
     * @param orderId 工单ID（topp_order.id）
     * @return 明细记录列表，每条包含解析后的字段
     */
    List<Map<String, Object>> queryRecords(Long orderId);

    /**
     * 保存单条记录的反馈
     *
     * @param recordId       明细记录ID（ftp_tc_user_common_warning.id_）
     * @param feedbackFlag   反馈标识：0-正常，1-不正常
     * @param feedbackRemark 反馈备注
     */
    void saveFeedback(Long recordId, Integer feedbackFlag, String feedbackRemark);

    /**
     * 批量保存反馈
     *
     * @param feedbacks 反馈列表，每项包含 recordId, feedbackFlag, feedbackRemark
     */
    void batchSaveFeedback(List<Map<String, Object>> feedbacks);

    /**
     * 检查工单下是否所有明细记录都已反馈
     *
     * @param orderId 工单ID
     * @return true-全部已反馈，false-存在未反馈记录
     */
    boolean isAllFeedbackDone(Long orderId);
}
