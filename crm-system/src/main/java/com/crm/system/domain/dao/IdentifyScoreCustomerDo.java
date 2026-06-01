package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.crm.common.core.domain.BaseEntity;

/**
 * 三清单智能评分记录对象 identify_score_customer
 *
 * @author crm
 * @date 2026-02-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("identify_score_customer")
public class IdentifyScoreCustomerDo extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 主键ID
     */
    @TableId(value = "id_", type = IdType.AUTO)
    private Long id;
    /**
     * 地市id，上传人员归属的地市
     */
    private Long areaId;
    /**
     * 县分id，上传人员归属的县分
     */
    private Long countyId;
    /**
     * 客户编号，本次先为空，后续有需要再沉淀
     */
    private String custId;
    /**
     * 客户名称
     */
    private String custName;
    /**
     * 附件名称
     */
    private String fileName;
    /**
     * 附件存放地址编号
     */
    private Long fileUrlId;
    /**
     * 评估总分
     */
    private String totalScore;
    /**
     * 评估详情
     */
    private String content;
    /**
     * 评估结果附件名称（暂时不用）
     */
    private String scoreFileName;
    /**
     * 评估结果附件存放地址编号（暂时不用）
     */
    private Long scoreFileUrlId;
    /**
     * 创建人员ID
     */
    private Long creatorId;
    /**
     * 更新人员ID
     */
    private Long updaterId;

    /**
     * 更新者
     */
    @TableField(exist = false)
    private String createBy;

    /**
     * 更新者
     */
    @TableField(exist = false)
    private String updateBy;

}
