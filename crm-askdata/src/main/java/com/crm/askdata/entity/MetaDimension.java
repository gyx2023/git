package com.crm.askdata.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.crm.common.core.domain.BaseEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.List;

/**
 * 维度元数据实体
 * <p>
 * 维度是可分组的分类字段，如时间、区域、部门等
 *
 * @author crm
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meta_dimension")
public class MetaDimension extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 维度代码 (唯一)
     */
    @TableField("dim_code")
    private String dimCode;

    /**
     * 维度名称
     */
    @TableField("dim_name")
    private String dimName;

    /**
     * 物理字段名
     */
    @TableField("physical_column")
    private String physicalColumn;

    /**
     * 维度类型：time/region/dept
     */
    @TableField("dim_type")
    private String dimType;

    /**
     * 枚举值列表 (JSON 格式)
     */
    @TableField("allowed_values")
    private String allowedValuesJson;

    /**
     * 版本号 (乐观锁)
     */
    @Version
    private Integer version;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;

    /**
     * 获取枚举值列表 (自动解析 JSON)
     */
    public List<String> getAllowedValues() {
        if (allowedValuesJson == null || allowedValuesJson.trim().isEmpty()
                || "[]".equals(allowedValuesJson.trim())) {
            return Collections.emptyList();
        }
        try {
            return new ObjectMapper().readValue(allowedValuesJson,
                    new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 设置枚举值列表 (自动序列化 JSON)
     */
    public void setAllowedValues(List<String> values) {
        try {
            this.allowedValuesJson = new ObjectMapper().writeValueAsString(values);
        } catch (Exception e) {
            this.allowedValuesJson = "[]";
        }
    }
}
