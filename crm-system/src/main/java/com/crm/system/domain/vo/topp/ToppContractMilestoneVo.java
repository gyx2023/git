package com.crm.system.domain.vo.topp;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 合同里程碑视图对象
 */
@Data
@ExcelIgnoreUnannotated
public class ToppContractMilestoneVo {

    private Long id;
    private Long contractId;
    private Long opportunityId;
    private Boolean valid;

    private BigDecimal sbAmount;
    private Date sbSfMonth;
    private BigDecimal sbSfAmount;
    private Date sbDhMonth;
    private BigDecimal sbDhAmount;
    private Date sbYsMonth;
    private BigDecimal sbYsAmount;
    private Date sbQtMonth;
    private BigDecimal sbQtAmount;

    private BigDecimal fwAmount;
    private Date fwSfMonth;
    private BigDecimal fwSfAmount;
    private Date fwCyMonth;
    private BigDecimal fwCyAmount;
    private Date fwZyMonth;
    private BigDecimal fwZyAmount;
    private Date fwQtMonth;
    private BigDecimal fwQtAmount;

    private BigDecimal zqAmount;
    private Integer zqPeriodType;
    private Date zqJsStartMonth;
    private Date zqJsEndMonth;
    private BigDecimal zqAverageAmount;

    private String incomeType;
    private Date createTime;
    private Date auditTime;
    private String reason;
    private String remark;
    private Boolean sbFlag;
    private Boolean fwFlag;
}
