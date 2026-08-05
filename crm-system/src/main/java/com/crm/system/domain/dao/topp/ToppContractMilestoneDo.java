package com.crm.system.domain.dao.topp;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 合同里程碑对象 topp_contract_milestone
 */
@Data
@TableName("topp_contract_milestone")
public class ToppContractMilestoneDo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 合同ID */
    private Long contractId;

    /** 商机ID */
    private Long opportunityId;

    /** 是否有效 */
    private Boolean valid;

    /** 设备里程碑金额 */
    private BigDecimal sbAmount;

    /** 设备首发月份 */
    private Date sbSfMonth;

    /** 设备首发金额 */
    private BigDecimal sbSfAmount;

    /** 设备到货月份 */
    private Date sbDhMonth;

    /** 设备到货金额 */
    private BigDecimal sbDhAmount;

    /** 设备验收月份 */
    private Date sbYsMonth;

    /** 设备验收金额 */
    private BigDecimal sbYsAmount;

    /** 设备其他月份 */
    private Date sbQtMonth;

    /** 设备其他金额 */
    private BigDecimal sbQtAmount;

    /** 服务里程碑金额 */
    private BigDecimal fwAmount;

    /** 服务首发月份 */
    private Date fwSfMonth;

    /** 服务首发金额 */
    private BigDecimal fwSfAmount;

    /** 服务初验月份 */
    private Date fwCyMonth;

    /** 服务初验金额 */
    private BigDecimal fwCyAmount;

    /** 服务终验月份 */
    private Date fwZyMonth;

    /** 服务终验金额 */
    private BigDecimal fwZyAmount;

    /** 服务其他月份 */
    private Date fwQtMonth;

    /** 服务其他金额 */
    private BigDecimal fwQtAmount;

    /** 账期金额 */
    private BigDecimal zqAmount;

    /** 账期类型 */
    private Integer zqPeriodType;

    /** 账期结算开始月份 */
    private Date zqJsStartMonth;

    /** 账期结算结束月份 */
    private Date zqJsEndMonth;

    /** 账期平均金额 */
    private BigDecimal zqAverageAmount;

    /** 收入类型 */
    private String incomeType;

    /** 创建时间 */
    private Date createTime;

    /** 审核时间 */
    private Date auditTime;

    /** 原因 */
    private String reason;

    /** 备注 */
    private String remark;

    /** 设备标志 */
    private Boolean sbFlag;

    /** 服务标志 */
    private Boolean fwFlag;
}
