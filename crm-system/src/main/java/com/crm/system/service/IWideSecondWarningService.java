package com.crm.system.service;

import org.flowable.task.api.Task;

import java.util.List;
import java.util.Map;

/**
 * 宽带二次预警 Service 接口
 *
 * @author crm
 */
public interface IWideSecondWarningService {

    /**
     * 分页查询用户明细列表
     *
     * @param orderId    工单ID
     * @param pageNum    页码（从1开始）
     * @param pageSize   每页大小
     * @param targetDate 到期日期（yyyyMMdd），由调用方从流程变量 wide_second-date 解析后传入
     */
    Map<String, Object> pageRecords(Long orderId, int pageNum, int pageSize, String targetDate);

    /**
     * 查询全部用户明细（导出用，不过滤 exte_flag）
     *
     * @param orderId    工单ID
     * @param targetDate 到期日期（yyyyMMdd）
     */
    List<Map<String, Object>> queryExportRecords(Long orderId, String targetDate);

    /**
     * 查询未续约用户明细（页面展示用，过滤 exte_flag=0）
     *
     * @param orderId    工单ID
     * @param targetDate 到期日期（yyyyMMdd）
     */
    List<Map<String, Object>> queryAllRecords(Long orderId, String targetDate);

    /**
     * 统计未续约用户数量（过滤 exte_flag=0）
     *
     * @param orderId    工单ID
     * @param targetDate 到期日期（yyyyMMdd）
     */
    int countRecords(Long orderId, String targetDate);

    /**
     * 获取跟踪情况下拉选项
     */
    Map<String, String> getFollowUpOptions();


    /**
     * 完成（通过）—— 对应老框架 dealWideSecondTask taskFlag=1。
     * 调用方负责补全流程编排：preserveDisplayVars → 本方法 → isProcessRunning/syncOrder。
     */
    Map<String, Object> completeOrder(Long orderId, Task task, String comment, String followUp, String otherReason);

    /**
     * 跟踪中 —— 对应老框架 dealWideSecondTask taskFlag=3。
     */
    Map<String, Object> trackOrder(Long orderId, Task task, String comment);
}
