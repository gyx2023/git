package com.crm.system.domain.bo;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.crm.common.core.domain.BaseEntity;

/**
 * 集团合同扩展信息业务对象 zqzt_grp_contract_other_info
 *
 * @author 万超凡
 * @date 2026-01-16
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ZqztGrpContractOtherInfoBo extends BaseEntity {

    /**
     * 序号，自增
     */
    @NotNull(message = "序号，自增不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 集团合同序号，对应GRP_CONTRACT_INFO.CONTRACT_ID
     */
    @NotBlank(message = "集团合同序号，对应GRP_CONTRACT_INFO.CONTRACT_ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractId;

    /**
     * 信息标识，详细看tab_column_config.module_name='GRP_CONTRACT_INFO'
     */
    @NotBlank(message = "信息标识，详细看tab_column_config.module_name='GRP_CONTRACT_INFO'不能为空", groups = { AddGroup.class, EditGroup.class })
    private String infoTag;

    /**
     * 信息值
     */
    @NotBlank(message = "信息值不能为空", groups = { AddGroup.class, EditGroup.class })
    private String infoVal;

    /**
     * 填写服务号码
     */
    @NotBlank(message = "填写服务号码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String infoVal2;

    /**
     * 是否有效：1有效，0无效，99迁移失效
     */
    @NotBlank(message = "是否有效：1有效，0无效，99迁移失效不能为空", groups = { AddGroup.class, EditGroup.class })
    private String isValid;
}

