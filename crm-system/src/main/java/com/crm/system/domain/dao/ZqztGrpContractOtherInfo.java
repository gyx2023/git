package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
}

