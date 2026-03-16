package com.crm.system.domain.bo;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.crm.common.core.domain.BaseEntity;

/**
 * 政企实际使用客户类型用户清单业务对象 grp_cust_inuse_userlist
 *
 * @author crm
 * @date 2026-01-20
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class GrpCustInuseUserlistBo extends BaseEntity {

    /**
     * 关系编号
     */
    @NotBlank(message = "关系编号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String inuseId;

    /**
     * 用户编号
     */
    @NotNull(message = "用户编号不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long SUBSCRBID;

    /**
     * 业务号码
     */
    @NotBlank(message = "业务号码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String svcNum;

    /**
     * 地市
     */
    @NotBlank(message = "地市不能为空", groups = { AddGroup.class, EditGroup.class })
    private String regionId;

    /**
     * 所属业务市县
     */
    @NotBlank(message = "所属业务市县不能为空", groups = { AddGroup.class, EditGroup.class })
    private String countyId;

    /**
     * 受理营业厅
     */
    @NotBlank(message = "受理营业厅不能为空", groups = { AddGroup.class, EditGroup.class })
    private String officeId;

    /**
     * 受理操作员
     */
    @NotBlank(message = "受理操作员不能为空", groups = { AddGroup.class, EditGroup.class })
    private String operatorId;

    /**
     * 备注
     */
    @NotBlank(message = "备注不能为空", groups = { AddGroup.class, EditGroup.class })
    private String REMARK;


}
