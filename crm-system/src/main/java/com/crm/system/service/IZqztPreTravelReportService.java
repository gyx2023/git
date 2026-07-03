package com.crm.system.service;

public interface IZqztPreTravelReportService {

    /**
     * 根据T+2日期范围查询未拜访的预约记录，生成行前报告待生成记录
     *
     * @param startTime 拜访时间起（格式：yyyy-MM-dd HH:mm:ss）
     * @param endTime   拜访时间止（格式：yyyy-MM-dd HH:mm:ss）
     * @return 生成的记录条数
     */
    int createPendingReports(String startTime, String endTime);

    /**
     * 全流程处理行前报告：扫描指定时间范围内创建且未拜访的预约 → 剔重 → 组装数据 → 调用Hiagent → 写入报告记录
     *
     * @param startTime 创建时间起（格式：yyyy-MM-dd HH:mm:ss）
     * @param endTime   创建时间止（格式：yyyy-MM-dd HH:mm:ss）
     * @return 处理的记录条数
     */
    int processPreTravelReports(String startTime, String endTime);

    /**
     * 重处理失败的行前报告（定时任务调用）
     * 范围：data_source=job、task_status=3、retry_count < 配置的最大重试次数
     * 先校验预约是否已拜访，已拜访则取消；否则重新调用Hiagent接口
     *
     * @return 处理的记录条数
     */
    int retryFailedReports();
}
