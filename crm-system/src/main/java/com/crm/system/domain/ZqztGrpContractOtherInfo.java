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
 * @date 2026-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("zqzt_grp_contract_other_info")
public class ZqztGrpContractOtherInfo extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 序号，自增
     */
    @TableId(value = "id")
    private Long id;
    /**
     * 集团合同序号，对应GRP_CONTRACT_INFO.CONTRACT_ID
     */
    private String contractId;
    /**
     * 信息标识，详细看tab_column_config.module_name='GRP_CONTRACT_INFO'
     */
    private String infoTag;
    /**
     * 合同业务类型
     */
    private String grpContractBusiType;
    /**
     * 合同相关用户
     */
    private String grpContractUserList;
    /**
     * 信息值
     */
    private String infoVal;
    /**
     * 填写服务号码
     */
    private String infoVal2;
    /**
     * 是否有效：1有效，0无效，99迁移失效
     */
    private String isValid;

}

