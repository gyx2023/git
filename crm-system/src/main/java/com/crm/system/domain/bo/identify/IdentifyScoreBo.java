package com.crm.system.domain.bo.identify;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.crm.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】业务对象 identify_score
 *
 * @author crm
 * @date 2026-02-02
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class IdentifyScoreBo extends BaseEntity {

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 对应 identify_score_customer表的id
     */
    @NotNull(message = "对应 identify_score_customer表的id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long customerId;

    /**
     * 总分
     */
    @NotBlank(message = "总分不能为空", groups = { AddGroup.class, EditGroup.class })
    private String totalScore;

    /**
     * 智能体接口返回详细内容
     */
    @NotBlank(message = "智能体接口返回详细内容不能为空", groups = { AddGroup.class, EditGroup.class })
    private String content;

    /**
     * 附件原始名称
     */
    @NotBlank(message = "附件原始名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String name;

    /**
     * 调用接口状态,0待调用，1成功,2失败
     */
    @NotBlank(message = "调用接口状态,0待调用，1成功,2失败不能为空", groups = { AddGroup.class, EditGroup.class })
    private String scoreStatus;

    /**
     * 上传时间
     */
    @NotNull(message = "上传时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date uploadTime;

    /**
     * 上传人员
     */
    @NotNull(message = "上传人员不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long uploader;


}
