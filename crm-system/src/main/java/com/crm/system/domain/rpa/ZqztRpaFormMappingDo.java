package com.crm.system.domain.rpa;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import com.crm.common.core.domain.BaseEntity;

/**
 * rpa单映射对象 zqzt_rpa_form_mapping
 *
 * @author crm
 * @date 2026-01-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("zqzt_rpa_form_mapping")
public class ZqztRpaFormMappingDo extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;
    /**
     * 流程编码
     */
    private String bizCode;
    /**
     * 环节编码
     */
    private String taskCode;
    /**
     * 表单key值
     */
    private String formKey;
    /**
     * 表单名称
     */
    private String formName;


    private String isComponent;

    private String componentKey;

    private String componentTemplate;
    /**
     * 表单对应的取值key
     */
    private String sourceKey;
    /**
     * 表单对应取值名称
     */
    private String sourceName;
    /**
     * 取值类型:1-表单，2-文件
     */
    private String sourceType;

    private String conditionsShow;

    private String conditionsValue;


    /**
     * 是否必填，0-否，1-是
     */
    private String isRequired;

    private String defaultValue;

    private String valueType;

    /**
     * 脚本编码
     */
    private String scriptCode;

    /**
     * 脚本是否启用，0-否，1-是
     */
    private String scriptEnabled;

    /**
     * 脚本执行阶段：ROW-行内执行，POST_ALL-全量后执行
     */
    private String scriptStage;

}
