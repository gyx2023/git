package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预警处理配置 DO
 *
 * @author crm
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("early_warning_processing_config")
public class EarlyWarningProcessingConfigDo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id_", type = IdType.AUTO)
    private Long id;

    /**
     * 预警类型
     */
    @TableField("warning_type")
    private String warningType;

    /**
     * 数据源（zj_sj: 主库, iob_gov2: 达梦）
     */
    @TableField("data_source")
    private String dataSource;

    /**
     * 预警SQL
     */
    @TableField("warning_sql")
    private String warningSql;

    /**
     * 描述（describe为MySQL保留字，用反引号）
     */
    @TableField("`describe`")
    private String describe;

    /**
     * 预警通知目标（手机号，多个用|分隔）
     */
    @TableField("warning_target")
    private String warningTarget;

    /**
     * 是否有效（1:有效）
     */
    @TableField("is_valid")
    private Integer isValid;
}
