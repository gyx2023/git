package com.crm.system.domain.bo.identify;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.crm.common.core.domain.BaseEntity;

/**
 * 三清单智能评分记录业务对象 identify_score_customer
 *
 * @author crm
 * @date 2026-02-02
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class IdentifyScoreCustomerBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 地市id，上传人员归属的地市
     */
    @NotNull(message = "地市id，上传人员归属的地市不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long areaId;

    /**
     * 县分id，上传人员归属的县分
     */
    @NotNull(message = "县分id，上传人员归属的县分不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long countyId;

    /**
     * 客户编号，本次先为空，后续有需要再沉淀
     */
    @NotBlank(message = "客户编号，本次先为空，后续有需要再沉淀不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custId;

    /**
     * 客户名称
     */
    @NotBlank(message = "客户名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custName;

    /**
     * 附件名称
     */
    @NotBlank(message = "附件名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String fileName;

    /**
     * 附件存放地址编号
     */
    @NotNull(message = "附件存放地址编号不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long fileUrlId;

    /**
     * 评估总分
     */
    @NotBlank(message = "评估总分不能为空", groups = { AddGroup.class, EditGroup.class })
    private String totalScore;

    /**
     * 评估详情
     */
    @NotBlank(message = "评估详情不能为空", groups = { AddGroup.class, EditGroup.class })
    private String content;

    /**
     * 评估结果附件名称（暂时不用）
     */
    @NotBlank(message = "评估结果附件名称（暂时不用）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String scoreFileName;

    /**
     * 评估结果附件存放地址编号（暂时不用）
     */
    @NotNull(message = "评估结果附件存放地址编号（暂时不用）不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long scoreFileUrlId;

    /**
     * 创建人员ID
     */
    @NotNull(message = "创建人员ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long creatorId;

    /**
     * 更新人员ID
     */
    @NotNull(message = "更新人员ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long updaterId;

    private String createTimeBegin;
    private String createTimeEnd;

    private String minScore;

    private String maxScore;
}
