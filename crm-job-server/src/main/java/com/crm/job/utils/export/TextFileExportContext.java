package com.crm.job.utils.export;

import lombok.Data;

import java.io.File;

/**
 * 文件导出上下文
 * 承载导出过程中的文件元数据和状态信息
 */
@Data
public class TextFileExportContext {

    /** 文件名（不含路径） */
    private String fileName;

    /** 文件后缀，如 ".txt", ".xls" */
    private String fileType;

    /** 导出日期 yyyyMMdd */
    private String exportDate;

    /** 本地文件完整路径 */
    private String filePath;

    /** 本地文件引用 */
    private File localFile;

    /** 累计写入行数 */
    private long totalRows;

    /** 导出状态 */
    private boolean success;
}
