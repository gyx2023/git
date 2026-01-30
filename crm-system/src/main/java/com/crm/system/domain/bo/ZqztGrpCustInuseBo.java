package com.crm.system.domain.bo;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.crm.common.core.domain.BaseEntity;

/**
 * 政企主体-实际使用客户（zqzt_grp_cust_inuse）业务对象 zqzt_grp_cust_inuse
 *
 * @author crm
 * @date 2026-01-20
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ZqztGrpCustInuseBo extends BaseEntity {

    /**
     * 关系编码（主键）
     */
    @NotBlank(message = "关系编码（主键）不能为空", groups = { EditGroup.class })
    private String inuseId;

    /**
     * 合同编号，关联zqzt_grp_contract_info.INUSE_CONTRACT_ID
     */
    @NotBlank(message = "合同编号，关联zqzt_grp_contract_info.INUSE_CONTRACT_ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractId;

    /**
     * 合同名称
     */
    @NotBlank(message = "合同名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractName;

    /**
     * 合同签约集团ID
     */
    @NotNull(message = "合同签约集团ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer grpIdA;

    /**
     * 客户类型：1000=非收入划转使用客户、1001=收入划转使用客户、1002=收入归集客户
     */
    @NotNull(message = "客户类型：1000=非收入划转使用客户、1001=收入划转使用客户、1002=收入归集客户不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer relation;

    /**
     * 关联客户集团ID
     */
    @NotNull(message = "关联客户集团ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer grpIdB;

    /**
     * 是否有效：1=有效、0=无效
     */
    @NotBlank(message = "是否有效：1=有效、0=无效不能为空", groups = { AddGroup.class, EditGroup.class })
    private String isValid;

    /**
     * 签约集团15位编码
     */
    @NotBlank(message = "签约集团15位编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String grpCodeA;

    /**
     * 签约集团总部编码（关联grp_group_info）
     */
    @NotBlank(message = "签约集团总部编码（关联grp_group_info）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String hqGrpCodeA;

    /**
     * 签约集团名称
     */
    @NotBlank(message = "签约集团名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String grpNameA;

    /**
     * 关联客户15位编码
     */
    @NotBlank(message = "关联客户15位编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String grpCodeB;

    /**
     * 关联客户总部编码（关联grp_group_info）
     */
    @NotBlank(message = "关联客户总部编码（关联grp_group_info）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String hqGrpCodeB;

    /**
     * 关联客户集团名称
     */
    @NotBlank(message = "关联客户集团名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String grpNameB;

    /**
     * 地市标识（如Z）
     */
    @NotBlank(message = "地市标识（如Z）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String regionId;

    /**
     * 所属业务市县编码
     */
    @NotBlank(message = "所属业务市县编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String countyId;

    /**
     * 受理操作员ID
     */
    @NotBlank(message = "受理操作员ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private String operatorId;

    /**
     * 备注信息
     */
    @NotBlank(message = "备注信息不能为空", groups = { AddGroup.class, EditGroup.class })
    private String remark;

    /**
     * 是否有效：1有效，0无效
     */
    @NotBlank(message = "是否有效：1有效，0无效不能为空", groups = { AddGroup.class, EditGroup.class })
    private String isValid;

}
