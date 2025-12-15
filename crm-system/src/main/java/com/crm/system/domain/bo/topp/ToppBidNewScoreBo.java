package com.crm.system.domain.bo.topp;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.crm.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】业务对象 topp_bid_new_score
 *
 * @author crm
 * @date 2025-12-02
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ToppBidNewScoreBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 新招投标id
     */
    @NotNull(message = "新招投标id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long toppBidNewId;

    /**
     * 商机编码
     */
    @NotBlank(message = "商机编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String opportunityCode;

    /**
     * 支撑经理姓名
     */
    @NotBlank(message = "支撑经理姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String name;

    /**
     * 角色
     */
    @NotBlank(message = "角色不能为空", groups = { AddGroup.class, EditGroup.class })
    private String role;

    /**
     * 是否录入支撑工单
     */
    @NotBlank(message = "是否录入支撑工单不能为空", groups = { AddGroup.class, EditGroup.class })
    private String isNotSupport;

    /**
     * 贡献度
     */
    @NotBlank(message = "贡献度不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contribution;

    /**
     * 创建人员
     */
    @NotBlank(message = "创建人员不能为空", groups = { AddGroup.class, EditGroup.class })
    private String creator;

    /**
     * 状态0:保存,1:审核中，2:结束
     */
    @NotBlank(message = "状态0:保存,1:审核中，2:结束不能为空", groups = { AddGroup.class, EditGroup.class })
    private String status;


}
