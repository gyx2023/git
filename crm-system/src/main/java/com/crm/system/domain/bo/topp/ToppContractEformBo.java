package com.crm.system.domain.bo.topp;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.crm.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】业务对象 topp_contract_eform
 *
 * @author crm
 * @date 2025-12-16
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ToppContractEformBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 关联商机
     */
    @NotNull(message = "关联商机不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long projectId;

    /**
     * 电子表单编号
     */
    @NotBlank(message = "电子表单编号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String eformNo;

    /**
     * 电子表单名称
     */
    @NotBlank(message = "电子表单名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String eformName;

    /**
     * 客户名称
     */
    @NotBlank(message = "客户名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custName;

    /**
     * 我方签约主体
     */
    @NotBlank(message = "我方签约主体不能为空", groups = { AddGroup.class, EditGroup.class })
    private String ourCompany;

    /**
     * 合同承办人OA
     */
    @NotBlank(message = "合同承办人OA不能为空", groups = { AddGroup.class, EditGroup.class })
    private String agentPersonOa;

    /**
     * 合同承办人
     */
    @NotBlank(message = "合同承办人不能为空", groups = { AddGroup.class, EditGroup.class })
    private String agentPersonName;

    /**
     * 合同总金额（元）
     */
    @NotNull(message = "合同总金额（元）不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal totalMoney;

    /**
     * 签订日期时间
     */
    @NotNull(message = "签订日期时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date signTime;

    /**
     * 合同开始日期
     */
    @NotNull(message = "合同开始日期不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date startDate;

    /**
     * 合同结束日期
     */
    @NotNull(message = "合同结束日期不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date endDate;

    /**
     * 标准文本类型
     */
    @NotBlank(message = "标准文本类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String textType;

    /**
     * 业务类型
     */
    @NotBlank(message = "业务类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String busiType;

    /**
     * 创建人员
     */
    @NotNull(message = "创建人员不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long creator;

    /**
     * 是否有效
     */
    @NotNull(message = "是否有效不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer isValid;


}
