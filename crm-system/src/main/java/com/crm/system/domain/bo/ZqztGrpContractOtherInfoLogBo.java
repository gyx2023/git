package com.crm.system.domain.bo;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.crm.common.core.domain.BaseEntity;

/**
 * 集团合同扩展信息日志业务对象 zqzt_grp_contract_other_info_log
 *
 * @author 万超凡
 * @date 2026-01-15
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ZqztGrpContractOtherInfoLogBo extends BaseEntity {

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


}
