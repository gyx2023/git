//package com.crm.system.domain.bo;
//
//import com.crm.common.core.validate.AddGroup;
//import com.crm.common.core.validate.EditGroup;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import javax.validation.constraints.*;
//
//import java.util.Date;
//
//import com.crm.common.core.domain.BaseEntity;
//
///**
// * 政企实际使用客户类型业务对象 grp_cust_inuse
// *
// * @author crm
// * @date 2026-01-20
// */
//
//@Data
//@EqualsAndHashCode(callSuper = true)
//public class GrpCustInuseBo extends BaseEntity {
//
//    /**
//     * 关系编码（主键）
//     */
//    @NotBlank(message = "关系编码（主键）不能为空", groups = { EditGroup.class })
//    private String inuseId;
//
//    /**
//     * 合同编号，关联GRP_CONTRACT_INFO.INUSE_CONTRACT_ID
//     */
//    @NotBlank(message = "合同编号，关联GRP_CONTRACT_INFO.INUSE_CONTRACT_ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String contractId;
//
//    /**
//     * 合同名称
//     */
//    @NotBlank(message = "合同名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String contractName;
//
//    /**
//     * 合同签约的集团ID
//     */
//    @NotNull(message = "合同签约的集团ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private Integer grpIdA;
//
//    /**
//     * 关联客户类型：1000=使用客户（非收入划转）、1001=使用客户（收入划转）、1002=收入归集客户
//     */
//    @NotNull(message = "关联客户类型：1000=使用客户（非收入划转）、1001=使用客户（收入划转）、1002=收入归集客户不能为空", groups = { AddGroup.class, EditGroup.class })
//    private Integer relation;
//
//    /**
//     * 关联客户集团ID
//     */
//    @NotNull(message = "关联客户集团ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private Integer grpIdB;
//
//    /**
//     * 合同签约的15位集团编码
//     */
//    @NotBlank(message = "合同签约的15位集团编码不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String grpCodeA;
//
//    /**
//     * 合同签约的集团名称
//     */
//    @NotBlank(message = "合同签约的集团名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String grpNameA;
//
//    /**
//     * 关联客户集团15位编码
//     */
//    @NotBlank(message = "关联客户集团15位编码不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String grpCodeB;
//
//    /**
//     * 关联客户集团名称
//     */
//    @NotBlank(message = "关联客户集团名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String grpNameB;
//
//    /**
//     * 地市（如Z）
//     */
//    @NotBlank(message = "地市（如Z）不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String regionId;
//
//    /**
//     * 所属业务市县
//     */
//    @NotBlank(message = "所属业务市县不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String countyId;
//
//    /**
//     * 受理营业厅
//     */
//    @NotBlank(message = "受理营业厅不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String officeId;
//
//    /**
//     * 受理操作员（如ZQ045281）
//     */
//    @NotBlank(message = "受理操作员（如ZQ045281）不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String operatorId;
//
//    /**
//     * 备注
//     */
//    @NotBlank(message = "备注不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String remark;
//
//
//}
