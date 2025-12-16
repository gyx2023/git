package com.crm.system.domain.bo;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.crm.common.core.domain.BaseEntity;

/**
 * 商机前评估信息业务对象 topp_opp_pre_estimate
 *
 * @author crm
 * @date 2025-12-16
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ToppOppPreEstimateBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 关联商机
     */
    @NotNull(message = "关联商机不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long oppoId;

    /**
     * 前评支撑人
     */
    @NotNull(message = "前评支撑人不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long oppDesigner;

    /**
     * 项目编号
     */
    @NotBlank(message = "项目编号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String projectNumber;

    /**
     * 项目名称
     */
    @NotBlank(message = "项目名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String projectName;

    /**
     * 前评估审批结果(1：通过，2：标前终止)
     */
    @NotNull(message = "前评估审批结果(1：通过，2：标前终止)不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long evaluationResult;

    /**
     * 是否五融(1：是，0：否)
     */
    @NotNull(message = "是否五融(1：是，0：否)不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer fiveFusion;

    /**
     * 五融类型逗号分隔(融自研产品，融数，融安全，融云，融5G)
     */
    @NotBlank(message = "五融类型逗号分隔(融自研产品，融数，融安全，融云，融5G)不能为空", groups = { AddGroup.class, EditGroup.class })
    private String fiveFusionType;

    /**
     * 自然客户ID
     */
    @NotBlank(message = "自然客户ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private String natureCustId;

    /**
     * 自然客户名称
     */
    @NotBlank(message = "自然客户名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String natureCustName;


}
