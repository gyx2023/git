package com.crm.system.domain.dao.rpa;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * RPA KPI 统计结果对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rpa_kpi_statistics_result")
public class RpaKpiStatisticsResultDo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String taskSource;
    private String kpiType;
    private String busiTypeLevel;
    private String busiTypeTypePar;
    private String kpiName;
    private String busiOrder;
    private String isDisplay;
    private String busiDisplayDistrict;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+8")
    private Date etlTime;

    private String dataCycleD;
    private String monthId;
    private String YEAR;
    private String areaCode;
    private String countyCode;
    private String zoneCode;
    private String dataLevel;
    private String dataRequency;
    private String kpiId;
    private String kpiValue;
    private BigDecimal monthVal;
    private BigDecimal monthRatio;
    private BigDecimal yearVal;
    private BigDecimal yearRatio;
    private String kpiValName1;
    private BigDecimal kpiValAdd1;
    private String kpiValName2;
    private BigDecimal kpiValAdd2;
    private String kpiDesc;
    private String 系统编码;
}
