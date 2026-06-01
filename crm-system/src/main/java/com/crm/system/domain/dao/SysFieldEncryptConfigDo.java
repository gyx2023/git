package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.crm.common.core.domain.BaseEntity;

/**
 * 敏感字段加密配置对象 sys_field_encrypt_config
 *
 * @author tjx
 * @date 2026-04-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_field_encrypt_config")
public class SysFieldEncryptConfigDo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
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
     * 加密类型：PHONE(手机号), NAME(姓名), IDCARD(身份证), GENERAL(通用)
     */
    private String encryptType;

    /**
     * 状态：0-待执行，1-执行中，2-已完成，3-失败
     */
    private String status;

    /**
     * 批次大小（每次处理的数据量）
     */
    private Integer batchSize;

    /**
     * 线程数（并发处理的线程数）
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
}
