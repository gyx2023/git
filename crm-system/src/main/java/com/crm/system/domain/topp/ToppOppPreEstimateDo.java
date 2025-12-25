package com.crm.system.domain.topp;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.crm.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 商机前评估信息对象 topp_opp_pre_estimate
 *
 * @author crm
 * @date 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("topp_opp_pre_estimate")
public class ToppOppPreEstimateDo extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 主键
     */
    @TableId(value = "id_")
    private Long id;
    /**
     * 关联商机
     */
    private Long oppoId;
    /**
     * 前评支撑人
     */
    private Long oppDesigner;
    /**
     * 项目编号
     */
    private String projectNumber;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 前评估审批结果(1：通过，2：标前终止)
     */
    private Long evaluationResult;
    /**
     * 是否五融(1：是，0：否)
     */
    private Integer fiveFusion;
    /**
     * 五融类型逗号分隔(融自研产品，融数，融安全，融云，融5G)
     */
    private String fiveFusionType;
    /**
     * 自然客户ID
     */
    private String natureCustId;
    /**
     * 自然客户名称
     */
    private String natureCustName;
    /**
     * 创建者
     */
    @TableField(exist = false)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(exist = false)
    private Date createTime;

    /**
     * 更新者
     */
    @TableField(exist = false)
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(exist = false)
    private Date updateTime;

}
