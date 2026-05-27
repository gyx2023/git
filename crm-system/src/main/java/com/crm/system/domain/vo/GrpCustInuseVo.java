package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.utils.convert.ExcelDictConvert;
import lombok.Data;


/**
 * 政企实际使用客户类型视图对象 grp_cust_inuse
 *
 * @author crm
 * @date 2026-01-20
 */
@Data
@ExcelIgnoreUnannotated
public class GrpCustInuseVo {

    private static final long serialVersionUID = 1L;

    /**
     * 关系编码（主键）
     */
    @ExcelProperty(value = "关系编码", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "主=键")
    private String inuseId;

    /**
     * 合同编号，关联GRP_CONTRACT_INFO.INUSE_CONTRACT_ID
     */
    @ExcelProperty(value = "合同编号，关联GRP_CONTRACT_INFO.INUSE_CONTRACT_ID")
    private String contractId;

    /**
     * 合同名称
     */
    @ExcelProperty(value = "合同名称")
    private String contractName;

    /**
     * 合同签约的集团ID
     */
    @ExcelProperty(value = "合同签约的集团ID")
    private Integer grpIdA;

    /**
     * 关联客户类型：1000=使用客户（非收入划转）、1001=使用客户（收入划转）、1002=收入归集客户
     */
    @ExcelProperty(value = "关联客户类型：1000=使用客户", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "非=收入划转")
    private Integer relation;

    /**
     * 关联客户集团ID
     */
    @ExcelProperty(value = "关联客户集团ID")
    private Integer grpIdB;

    /**
     * 合同签约的15位集团编码
     */
    @ExcelProperty(value = "合同签约的15位集团编码")
    private String grpCodeA;

    /**
     * 合同签约的集团名称
     */
    @ExcelProperty(value = "合同签约的集团名称")
    private String grpNameA;

    /**
     * 关联客户集团15位编码
     */
    @ExcelProperty(value = "关联客户集团15位编码")
    private String grpCodeB;

    /**
     * 关联客户集团名称
     */
    @ExcelProperty(value = "关联客户集团名称")
    private String grpNameB;

    /**
     * 地市（如Z）
     */
    @ExcelProperty(value = "地市", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "如=Z")
    private String regionId;

    /**
     * 所属业务市县
     */
    @ExcelProperty(value = "所属业务市县")
    private String countyId;

    /**
     * 受理营业厅
     */
    @ExcelProperty(value = "受理营业厅")
    private String officeId;

    /**
     * 受理操作员（如ZQ045281）
     */
    @ExcelProperty(value = "受理操作员", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "如=ZQ045281")
    private String operatorId;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;


}
