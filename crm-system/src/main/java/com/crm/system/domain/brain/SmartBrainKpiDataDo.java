package com.crm.system.domain.brain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 智慧大脑指标数据表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("smart_brain_kpi_data")
public class SmartBrainKpiDataDo {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 指标加工时间
     */
    @TableField("etl_time")
    private Date etlTime;

    /**
     * 日账期，格式：YYYYMMDD
     */
    @TableField("data_cycle_d")
    private String dataCycleD;

    /**
     * 月账期，格式：YYYYMM
     */
    @TableField("month_id")
    private String monthId;

    /**
     * 年，格式：YYYY
     */
    @TableField("year")
    private String year;

    /**
     * 地市，999-全省，杭州571,000-省本部
     */
    @TableField("area_code")
    private String areaCode;

    /**
     * 区县编码，如：滨江ABJ
     */
    @TableField("county_code")
    private String countyCode;

    /**
     * 网格编码，36开头
     */
    @TableField("zone_code")
    private String zoneCode;

    /**
     * 数据层级，0-省；1-市；2-县；3-网格
     */
    @TableField("data_level")
    private String dataLevel;

    /**
     * 数据频次，Y-年；M-月；D-日
     */
    @TableField("data_requency")
    private String dataRequency;

    /**
     * 指标编码，详见附录2
     */
    @TableField("kpi_id")
    private String kpiId;

    /**
     * 指标数值
     */
    @TableField("kpi_value")
    private String kpiValue;

    /**
     * 环比分子，T账期数值-（T-1）账期数值
     */
    @TableField("month_val")
    private BigDecimal monthVal;

    /**
     * 环比百分比，百分比保留2位小数，如25.89%沉淀25.89
     */
    @TableField("month_ratio")
    private BigDecimal monthRatio;

    /**
     * 同比分子，本年数值-上年同期
     */
    @TableField("year_val")
    private BigDecimal yearVal;

    /**
     * 同比百分比
     */
    @TableField("year_ratio")
    private BigDecimal yearRatio;

    /**
     * 附加指标数值1
     */
    @TableField("kpi_val_add1")
    private String kpiValAdd1;

    /**
     * 附加指标数值2
     */
    @TableField("kpi_val_add2")
    private String kpiValAdd2;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private Date createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private Date updatedTime;
}
