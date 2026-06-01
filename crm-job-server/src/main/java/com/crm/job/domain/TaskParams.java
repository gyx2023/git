package com.crm.job.domain;

import lombok.Data;

import java.util.Date;
import java.util.List;

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
     * 账期日期（格式：yyyyMMdd 或 yyyyMM），用于动态拼接文件路径
     * 示例：
     * - 有账期路径：/usr/local/20250101/xxx.txt
     * - 无账期路径：/usr/local/xxx.txt
     */
    private String targetDate;

    /**
     * 账期日期（格式：yyyyMMdd 或 yyyyMM），用于拼接文件名后缀
     * 示例：
     * - 拼接后文件名：xxx_20250101.txt
     * - 无日期文件名：xxx.txt
     */
    private String suffixDate;

    /**
     * 批处理大小
     */
    private int batchSize;

    /**
     * 文件类型后缀
     * 示例：txt
     */
    private String extension;

    /**
     * 文件名核心前缀列表
     * 示例：DWD_CONTRACTS_JC_D
     */
    private List<String> fileNameList;

    /**
     * 文件完整路径列表
     * 示例：/usr/local/remoteFile/DWD_CONTRACTS_JC_D_20260305.txt
     */
    private List<String> filePathList;

    /**
     * 文件路径：是否包含账期目录
     * 示例：/usr/local/20250101/xxx.txt
     */
    private boolean hasPathDate;

    /**
     * 文件名称：是否包含账期
     * 示例：/usr/local/20250101/xxx_20250101.txt
     */
    private boolean hasSuffixDate;

    /**
     * 文件账期偏移天数（执行任务日期 - 文件账期日期 = 偏移量）
     * 示例：
     * - 执行任务日期：2025-01-03
     * - 文件账期日期：20250101（对应路径/文件：/usr/local/20250101/xxx_20250101.txt）
     * - 偏移量计算：2025-01-03 - 20250101 = 2 → offsetDay = 2（即t-2）
     */
    private Integer offsetDay;

    /**
     * 文件账期
     */
    private Date fileEtlCycle;

    /**
     * 文件根目录
     */
    private String dataRootPath;
}
