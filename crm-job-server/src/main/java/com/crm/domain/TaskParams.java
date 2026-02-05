package com.crm.domain;

import lombok.Data;

import java.util.Date;

@Data
public class TaskParams {
    /**
     * SFTP配置类型
     */
    private String sftpConfigType;
    /**
     * 日期类型 D日 M月
     */
    private String dateType;
    /**
     * 目标日期 yyyyMMdd 或 yyyyMM
     */
    private String targetDate;
    /**
     * 批处理大小
     */
    private int batchSize;
    /**
     * 文件名
     */
    private String fileName;
}
