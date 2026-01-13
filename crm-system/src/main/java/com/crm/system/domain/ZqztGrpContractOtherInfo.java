package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import com.crm.common.core.domain.BaseEntity;

/**
 * 集团合同扩展信息对象 zqzt_grp_contract_other_info
 *
 * @author 万超凡
 * @date 2026-01-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("zqzt_grp_contract_other_info")
public class ZqztGrpContractOtherInfo extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 集团合同序号，对应GRP_CONTRACT_INFO.CONTRACT_ID
     */
    private String contractId;
    /**
     * 信息标识，详细看tab_column_config.module_name='GRP_CONTRACT_INFO'
     */
    private String infoTag;
    /**
     * 信息值
     */
    private String infoVal;

}
