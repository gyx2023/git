package com.crm.system.domain.bo.encrypt;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 敏感字段加密配置业务对象
 *
 * @author tjx
 * @date 2026-04-17
 */
@Data
public class SysFieldEncryptConfigBo {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 表名
     */
    @NotBlank(message = "表名不能为空")
    private String tableName;

    /**
     * 字段名
     */
    @NotBlank(message = "字段名不能为空")
    private String fieldName;

    /**
     * 主键字段名（多个用逗号分隔），为空则自动检测
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
     * 批次大小（默认1000）
     */
    private Integer batchSize;

    /**
     * 线程数（默认4，最大8）
     */
    private Integer threadCount;

    /**
     * 备注
     */
    private String remark;
}
