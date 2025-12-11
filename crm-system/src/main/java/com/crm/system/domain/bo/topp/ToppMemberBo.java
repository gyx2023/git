package com.crm.system.domain.bo.topp;

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
 * 【请填写功能名称】业务对象 topp_member
 *
 * @author crm
 * @date 2025-12-02
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ToppMemberBo extends BaseEntity {

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 	项目ID
     */
    @NotNull(message = "	项目ID	不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long projectId;

    /**
     * 	团队角色
     */
    @NotBlank(message = "	团队角色	不能为空", groups = { AddGroup.class, EditGroup.class })
    private String memberRole;

    /**
     * 	成员id
     */
    @NotNull(message = "	成员id	不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long memberId;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long memberArea;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long memberOrg;

    /**
     * 	姓名
     */
    @NotBlank(message = "	姓名	不能为空", groups = { AddGroup.class, EditGroup.class })
    private String name;

    /**
     * 	职责说明
     */
    @NotBlank(message = "	职责说明	不能为空", groups = { AddGroup.class, EditGroup.class })
    private String roleDesc;

    /**
     * 	创建人员
     */
    @NotNull(message = "	创建人员	不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long creator;

    /**
     * 	备注说明
     */
    @NotBlank(message = "	备注说明不能为空", groups = { AddGroup.class, EditGroup.class })
    private String memo;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String score;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String scoreExplain;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date scoreTime;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contri;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String teamLeaderFlag;


}
