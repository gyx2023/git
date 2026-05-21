package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 合同收入数据导出 Excel VO
 * 对应 ContractIncludeMoneyPutJob 中的7个查询字段
 */
@Data
public class ContractIncomeExcelVo {

    /**
     * 1. 合同编号
     * 对应SQL：c.contract_no
     */
    @ExcelProperty(value = "合同编号", index = 0)
    private String contractNo;

    /**
     * 2. 新兴ICT项目类计收金额（元）
     * 对应SQL：c.ict_plan_income
     */
    @ExcelProperty(value = "新兴ICT项目类计收金额（元）", index = 1)
    private BigDecimal ictPlanIncome;

    /**
     * 3. 标准类产品预计入网时间
     * 对应SQL：c.norm_date
     */
    @ExcelProperty(value = "标准类产品预计入网时间", index = 2)
    private Date normDate;

    /**
     * 4. 标准类产品计收金额（元）
     * 对应SQL：ROUND(c.norm_plan_income / 10000, 6) AS norm_plan_income
     */
    @ExcelProperty(value = "标准类产品计收金额（元）", index = 3)
    private BigDecimal normPlanIncome;

    /**
     * 5. 计收周期
     * 对应SQL：c.plan_cycle
     */
    @ExcelProperty(value = "计收周期", index = 4)
    private String planCycle;

    /**
     * 6. 预计本年完成收入
     * 对应SQL：ROUND(c.expect_income, 6) AS expect_income
     */
    @ExcelProperty(value = "预计本年完成收入", index = 5)
    private BigDecimal expectIncome;

    /**
     * 7. 合同分项收入
     * 对应SQL：include_type.include_type_value
     */
    @ExcelProperty(value = "合同分项收入", index = 6)
    private String includeTypeValue;
}
