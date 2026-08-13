package com.crm.job.utils.export;

import lombok.Data;

/**
 * 导出汇总信息
 * 在 finishWriteFileInternal 步骤传递给子类，用于记录导出结果
 */
@Data
public class SummaryHolder {

    /** 总处理行数 */
    private long totalRows;

    /** 成功行数 */
    private long successRows;

    /** 失败行数 */
    private long failRows;

    /** 错误信息 */
    private String errorMsg;
}
