package com.crm.system.domain.dao.topp;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.crm.common.core.domain.BaseEntity;

import java.math.BigDecimal;

/**
 * 合同分项收入明细对象 topp_contract_include_type_money
 * 老系统对照：ContractIncludeTypeMoney
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("topp_contract_include_type_money")
public class ToppContractIncludeTypeMoneyDo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 合同ID */
    private Long contractId;

    /** 分项收入类型编码 */
    private String includeType;

    /** 金额（单位：元，老系统保存时除以10000从元转万元） */
    private BigDecimal value;
}
