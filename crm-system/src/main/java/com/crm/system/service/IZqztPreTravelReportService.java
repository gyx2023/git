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
     * 全流程处理行前报告：查询未拜访预约 → 组装数据 → 调用Hiagent → 写入报告记录
     *
     * @param startTime 拜访时间起（格式：yyyy-MM-dd HH:mm:ss）
     * @param endTime   拜访时间止（格式：yyyy-MM-dd HH:mm:ss）
     * @return 处理的记录条数
     */
    int processPreTravelReports(String startTime, String endTime);
}
