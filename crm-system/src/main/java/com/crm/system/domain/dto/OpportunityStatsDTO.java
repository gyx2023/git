package com.crm.system.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 商机统计响应数据传输对象
 *
 * @author system
 * @date 2026-06-12
 */
@Data
public class OpportunityStatsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 组织ID
     */
    private Long orgId;

    /**
     * 组织名称
     */
    private String orgName;

    /**
     * 商机数
     */
    private Integer opportunityCount;

    /**
     * 时间段新增商机
     */
    private Integer newOpportunityCount;

    /**
     * 签约商机数
     */
    private Integer signedOpportunityCount;

    /**
     * 时间段新增签约商机数
     */
    private Integer newSignedOpportunityCount;

    /**
     * 支撑商机数
     */
    private Integer supportOpportunityCount;

    /**
     * 时间段支撑商机数
     */
    private Integer newSupportOpportunityCount;

    /**
     * 签订合同数
     */
    private Integer contractCount;

    /**
     * 时间段签订合同数
     */
    private Integer newContractCount;

    /**
     * 签订合同金额（万元）
     */
    private BigDecimal contractAmount;

    /**
     * 时间段签订合同金额（万元）
     */
    private BigDecimal newContractAmount;

    /**
     * 商机预期金额（万元）
     */
    private BigDecimal expectAmount;

    public BigDecimal getExpectAmount() {
        return formatBigDecimal(expectAmount);
    }

    /**
     * 格式化BigDecimal，避免科学计数法显示
     * - null返回0.00
     * - 极小值(<0.005)返回0.00
     * - 其他保留2位小数
     */
    private BigDecimal formatBigDecimal(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(0, RoundingMode.HALF_UP);
        }

        // 处理极小值（如0E-10），统一转为0
        if (value.compareTo(new BigDecimal("0.005")) < 0 && value.compareTo(BigDecimal.ZERO) >= 0) {
            return BigDecimal.ZERO.setScale(0, RoundingMode.HALF_UP);
        }

        // 保留2位小数，四舍五入
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
