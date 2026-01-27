package com.crm.system.domain.topp;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import com.crm.common.core.domain.BaseEntity;

/**
 * 商机标讯信息对象 topp_bid_suboppo
 *
 * @author crm
 * @date 2026-01-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("topp_bid_suboppo")
public class ToppBidSuboppoDo extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 关联商机ID
     */
    private Long projectId;
    /**
     * 商机编码
     */
    private String oppoCode;
    /**
     * 标讯ID
     */
    private String infoId;
    /**
     * 标讯任务ID
     */
    private String bidTaskManagerId;
    /**
     * 商机挂牌类型: GREEN绿牌/NONE未挂牌/RED红牌/YELLOW黄牌
     */
    private String colorBrand;
    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 预算金额
     */

    private String budgetAmount;


    @TableField(exist = false)
    private String createBy;

    @TableField(exist = false)
    private String updateBy;
}
