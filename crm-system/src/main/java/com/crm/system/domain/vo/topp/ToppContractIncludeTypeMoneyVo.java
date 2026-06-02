package com.crm.system.domain.vo.topp;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 合同分项收入明细视图对象
 */
@Data
@ExcelIgnoreUnannotated
public class ToppContractIncludeTypeMoneyVo {

    private Long id;
    private Long contractId;
    private String includeType;
    private BigDecimal value;
}
