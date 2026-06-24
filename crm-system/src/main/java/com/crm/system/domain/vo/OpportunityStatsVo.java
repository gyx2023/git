package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 商机统计视图对象
 *
 * @author system
 * @date 2026-06-12
 */
@Data
public class OpportunityStatsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 组织名称
     */
    @ExcelProperty(value = "部门", index = 0)
    private String orgName;

    /**
     * 商机数
     */
    @ExcelProperty(value = "商机数", index = 1)
    private Integer opportunityCount;

    /**
     * 时间段新增商机
     */
    @ExcelProperty(value = "时间段新增商机", index = 2)
    private Integer newOpportunityCount;

    /**
     * 签约商机数
     */
    @ExcelProperty(value = "签约商机数", index = 3)
    private Integer signedOpportunityCount;

    /**
     * 时间段新增签约商机数
     */
    @ExcelProperty(value = "时间段新增签约商机数", index = 4)
    private Integer newSignedOpportunityCount;

    /**
     * 支撑商机数
     */
    @ExcelProperty(value = "支撑商机数", index = 5)
    private Integer supportOpportunityCount;

    /**
     * 时间段支撑商机数
     */
    @ExcelProperty(value = "时间段支撑商机数", index = 6)
    private Integer newSupportOpportunityCount;

    /**
     * 签订合同数
     */
    @ExcelProperty(value = "签订合同数", index = 7)
    private Integer contractCount;

    /**
     * 时间段签订合同数
     */
    @ExcelProperty(value = "时间段签订合同数", index = 8)
    private Integer newContractCount;

    /**
     * 签订合同金额（万元）
     */
    @ExcelProperty(value = "签订合同金额(万元)", index = 9)
    private BigDecimal contractAmount;

    /**
     * 时间段签订合同金额（万元）
     */
    @ExcelProperty(value = "时间段签订合同金额(万元)", index = 10)
    private BigDecimal newContractAmount;

    /**
     * 商机预期金额（万元）
     */
    @ExcelProperty(value = "商机金额(万元)", index = 11)
    private BigDecimal expectAmount;
}
