package com.crm.system.domain.cust;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
import com.crm.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】对象 zqzt_cust_maint_info
 *
 * @author crm
 * @date 2026-02-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("zqzt_cust_maint_info")
public class ZqztCustMaintInfo extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     *
     */
    @TableField("STAT_CYCLE")
    private String statCycle;
    /**
     *
     */
    @TableField("ETL_TIME")
    private Date etlTime;
    /**
     * 操作类型
     */
    @TableField("OPER_TYPE")
    private String operType;
    /**
     * 用户ID（唯一索引）
     */
    @TableId(value = "USER_ID")
    private String userId;
    /**
     * 业务号码
     */
    @TableField("SVC_NUM")
    private String svcNum;

    /**
     * 二级类型编码
     */
    @TableField("TYPE_LVL2")
    private String typeLvl2;

    /**
     * 二级类型名称
     */
    @TableField("TYPE_LVL2_NAME")
    private String typeLvl2Name;
    /**
     * 客户组ID
     */
    @TableField("CUST_GRP_ID")
    private String custGrpId;

    /**
     * 客户组名称
     */
    @TableField("CUST_GRP_NAME")
    private String custGrpName;
    /**
     * 客户组类型
     */
    @TableField("CUST_GRP_TYPE")
    private String custGrpType;
    /**
     * 客户ID
     */
    @TableField("CUST_ID")
    private String custId;

    /**
     * 客户名称
     */
    @TableField("CUST_NAME")
    private String custName;
    /**
     * 状态
     */
    @TableField("STATUS")
    private String STATUS;

}
