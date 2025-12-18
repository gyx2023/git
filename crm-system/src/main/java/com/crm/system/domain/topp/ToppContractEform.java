package com.crm.system.domain.topp;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import java.math.BigDecimal;
import com.crm.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】对象 topp_contract_eform
 *
 * @author crm
 * @date 2025-12-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("topp_contract_eform")
public class ToppContractEform extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 主键
     */
    @TableId(value = "id_")
    private Long id;
    /**
     * 关联商机
     */
    private Long projectId;
    /**
     * 电子表单编号
     */
    private String eformNo;
    /**
     * 电子表单名称
     */
    private String eformName;
    /**
     * 客户名称
     */
    private String custName;
    /**
     * 我方签约主体
     */
    private String ourCompany;
    /**
     * 合同承办人OA
     */
    private String agentPersonOa;
    /**
     * 合同承办人
     */
    private String agentPersonName;
    /**
     * 合同总金额（元）
     */
    private BigDecimal totalMoney;
    /**
     * 签订日期时间
     */
    private String signTime;
    /**
     * 合同开始日期
     */
    private String startDate;
    /**
     * 合同结束日期
     */
    private String endDate;
    /**
     * 标准文本类型
     */
    private String textType;
    /**
     * 业务类型
     */
    private String busiType;
    /**
     * 创建人员
     */
    private Long creator;
    /**
     * 是否有效
     */
    private Integer isValid;



    @TableField(exist = false)
    private String createBy;
    @TableField(exist = false)
    private Date createTime;
    @TableField(exist = false)
    private String updateBy;
    @TableField(exist = false)
    private Date updateTime;
}
