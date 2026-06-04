package com.crm.askdata.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.crm.common.core.domain.BaseEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 指标元数据实体
 * <p>
 * 关键点：
 * 1. version 字段支持乐观锁
 * 2. allowedValues 防止 AI 参数幻觉
 * 3. securityLevel 控制访问权限
 *
 * @author crm
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meta_metric")
public class MetaMetric extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 指标代码 (唯一)
     */
    @TableField("metric_code")
    private String metricCode;

    /**
     * 指标名称
     */
    @TableField("metric_name")
    private String metricName;

    /**
     * 计算表达式
     */
    private String expression;

    /**
     * 业务分类：sales/contract/customer/finance 等
     */
    private String category;

    /**
     * 数据类型：money/percent/count/ratio/date
     */
    @TableField("data_type")
    private String dataType;

    /**
     * 安全等级：1-公开 2-内部 3-保密
     */
    @TableField("security_level")
    private Integer securityLevel;

    /**
     * 单位
     */
    private String unit;

    /**
     * 数据来源表名
     */
    @TableField("source_table")
    private String sourceTable;

    /**
     * 数据源名称（对应 spring.datasource.dynamic.datasource 中的配置 key）
     */
    @TableField("source_datasource")
    private String sourceDatasource;

    /**
     * 实体字段→物理字段映射（JSON 格式）
     * 如 {"time":"order_month", "region":"region_code"}
     */
    @TableField("condition_mapping")
    private String conditionMapping;

    /**
     * 枚举值列表 (JSON 格式，防止 AI 幻觉)
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
            return java.util.Collections.emptyList();
        }
        try {
            return new ObjectMapper().readValue(allowedValuesJson,
                    new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return java.util.Collections.emptyList();
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

    /**
     * 校验值是否在枚举范围内
     * 陷阱防护：工具参数幻觉
     */
    public boolean validateValue(String value) {
        List<String> allowed = getAllowedValues();
        if (allowed == null || allowed.isEmpty()) {
            return true; // 无枚举限制
        }
        return allowed.contains(value);
    }
}
