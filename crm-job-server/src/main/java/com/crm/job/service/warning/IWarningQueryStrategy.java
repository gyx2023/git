package com.crm.job.service.warning;

import com.crm.system.service.warning.WarningRecord;

import com.crm.system.domain.bo.topp.ToppOrderBo;

import java.util.List;
import java.util.Map;

/**
 * 预警查询策略接口
 * <p>
 * 每个预警类型实现一个，负责查询对应的业务表并转换为统一的 WarningRecord 列表。
 * 建议继承 AbstractWarningQueryStrategy 而非直接实现此接口，以获得统一的日志和参数处理。
 *
 * @author crm
 * @see AbstractWarningQueryStrategy
 */
public interface IWarningQueryStrategy {

    /**
     * 预警类型标识，与 topp_order.order_type 对应
     * 如 "htdq_warn"（合同到期预警）、"qf_warn"（欠费预警）
     */
    String getOrderType();

    /**
     * 查询待处理记录
     *
     * @param targetDate 目标日期（yyyyMMdd），为空时默认当日
     * @return 待处理的预警记录列表
     */
    List<WarningRecord> query(String targetDate);

    // ==================== 可选扩展点 ====================

    /**
     * 该预警使用的流程定义 key
     * <p>
     * 默认单节点通用流程 single_approval；两节点（客户经理 → 稽核）预警重写为 double_approval。
     * 决定 {@code WarningStartService} 启动哪个 BPMN，以及是否需要解析稽核处理人。
     * </p>
     */
    default String getProcessKey() {
        return "single_approval";
    }

    /**
     * 双节点流程（double_approval）第二节点显示名。
     * <p>
     * 默认"稽核人员处理"；
     * 宽带专项营销(custSales) 的第二节点语义为"业务受理"，需覆盖为"业务受理"。
     * </p>
     */
    default String getSecondNodeName() {
        return "稽核人员处理";
    }

    /**
     * 构建前端跳转URL
     * <p>
     * 若不重写，使用默认 URL：/warning/toDeal?orderType=xxx&orderId=xxx
     * </p>
     *
     * @param orderId 工单ID
     * @param extra   预警记录的扩展字段（可用于拼接 URL 参数）
     */
    default String buildDealUrl(Long orderId, Map<String, Object> extra) {
        return "/warning/toDeal?orderType=" + getOrderType() + "&orderId=" + orderId;
    }

    /**
     * 流程启动前的预处理（可选）
     * <p>
     * 在流程启动 + 工单创建之前调用。可用于业务状态更新、外部接口调用等。
     * WarningRecord 在此阶段可修改（如补充 extraVariables）。
     * </p>
     */
    default void beforeStart(WarningRecord record, String targetDate) {
        // 子类按需重写
    }

    /**
     * 流程启动 + 工单创建完成后的后处理（可选）
     * <p>
     * 在流程启动、工单创建、变量设置全部完成后调用。可用于消息通知、关联表更新等。
     * </p>
     */
    default void afterStart(WarningRecord record, ToppOrderBo orderBo) {
        // 子类按需重写
    }

    /**
     * 是否需要全部处理完成后发送通知（微信/短信）
     * <p>
     * 对应旧系统 BaseJob.startTask() / SecondWarningJob.startTaskWideSecond() 中的
     * sendDMessageService.initNoticeMessageByWx() 通知逻辑。
     * </p>
     */
    default boolean isNotificationEnabled() {
        return false;
    }

    /**
     * 是否启用建单重复校验（同一 infoId + orderType 已存在工单则跳过）。
     * <p>
     * 默认 true；部分预警不能查重，如宽带/双线二次预警（wide_second、double_wide_second）：
     * 同一预警记录会随跑批多次生成工单（未续约 exte_flag 持续为 0 时持续提醒），
     * 旧系统 SecondWarningJob 也不做重复校验，需重写返回 false 对齐旧行为。
     * </p>
     */
    default boolean isDuplicateCheckEnabled() {
        return true;
    }

    /**
     * 生成通知消息内容
     *
     * @param count 该客户经理待处理工单数
     * @return 通知文本，如 "温馨提醒：您收到3个宽带二次预警工单，已生成待处理任务，请至创新头条-省分专区-运营平台。"
     */
    default String getNotificationMessage(int count) {
        return "温馨提醒：您收到" + count + "个" + getOrderType() + "工单，已生成待处理任务，请至创新头条-省分专区-运营平台。";
    }
}
