package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import com.crm.common.core.domain.BaseEntity;

/**
 * 集团合同扩展信息日志对象 zqzt_grp_contract_other_info_log
 *
 * @author 万超凡
 * @date 2026-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("zqzt_grp_contract_other_info_log")
public class ZqztGrpContractOtherInfoLog extends BaseEntity {

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

    /**
     * 数据库不查询的字段
     */
    @TableField(exist = false)
    private String createBy;

    @TableField(exist = false)
    private String updateBy;

    @Override
    public void setCreateBy(String createBy) {
        // 不执行任何操作，防止自动填充
    }

    @Override
    public void setUpdateBy(String updateBy) {
        // 不执行任何操作，防止自动填充
    }

}

