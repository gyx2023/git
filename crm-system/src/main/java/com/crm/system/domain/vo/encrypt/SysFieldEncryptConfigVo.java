package com.crm.system.domain.vo.encrypt;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 敏感字段加密配置视图对象
 *
 * @author tjx
 * @date 2026-04-17
 */
@Data
public class SysFieldEncryptConfigVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 主键字段名（多个用逗号分隔）
     */
    private String primaryKey;

    /**
     * 加密类型
     */
    private String encryptType;

    /**
     * 状态：0-待执行，1-执行中，2-已完成，3-失败
     */
    private String status;

    /**
     * 批次大小
     */
    private Integer batchSize;

    /**
     * 线程数
     */
    private Integer threadCount;

    /**
     * 已处理记录数
     */
    private Long processedCount;

    /**
     * 总记录数
     */
    private Long totalCount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
