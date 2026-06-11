package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.utils.convert.ExcelDictConvert;
import lombok.Data;


/**
 * 合同和关联客户ID视图对象 zqzt_grp_cust_inuse
 *
 * @author crm
 * @date 2026-01-19
 */
@Data
@ExcelIgnoreUnannotated
public class ZqztGrpCustInuseVo {

    private static final long serialVersionUID = 1L;

    /**
     * 关系编码ZQ1904150001
     */
    @ExcelProperty(value = "关系编码ZQ1904150001")
    private String inuseId;

    /**
     * 合同编号，对应GRP_CONTRACT_INFO.INUSE_CONTRACT_ID
     */
    @ExcelProperty(value = "合同编号，对应GRP_CONTRACT_INFO.INUSE_CONTRACT_ID")
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
    private Long grpIdA;

    /**
     * 合同签约的15位集团编码
     */
    @ExcelProperty(value = "合同签约的15位集团编码")
    private String grpCodeA;

    /**
     * 合同签约的总部集团编码
     */
    @ExcelProperty(value = "合同签约的总部集团编码")
    private String hqGrpCodeA;

    /**
     * 合同签约的集团名称
     */
    @ExcelProperty(value = "合同签约的集团名称")
    private String grpNameA;

    /**
     * 关联客户类型：1000：使用客户（非收入划转）1001：使用客户（收入划转）1002：收入归集客户
     */
    @ExcelProperty(value = "关联客户类型：1000：使用客户", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "非=收入划转")
    private Long relation;

    /**
     * 关联客户集团ID
     */
    @ExcelProperty(value = "关联客户集团ID")
    private Long grpIdB;

    /**
     * 关联客户集团15位编码
     */
    @ExcelProperty(value = "关联客户集团15位编码")
    private String grpCodeB;

    /**
     * 关联客户的总部集团编码
     */
    @ExcelProperty(value = "关联客户的总部集团编码")
    private String hqGrpCodeB;

    /**
     * 关联客户集团名称
     */
    @ExcelProperty(value = "关联客户集团名称")
    private String grpNameB;

    /**
     * 地市
     */
    @ExcelProperty(value = "地市")
    private String regionId;

    /**
     * 所属业务市县
     */
    @ExcelProperty(value = "所属业务市县")
    private String countyId;

    /**
     * 受理操作员
     */
    @ExcelProperty(value = "受理操作员")
    private String operatorId;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * 是否有效：1有效，0无效
     */
    @ExcelProperty(value = "是否有效：1有效，0无效")
    private String isValid;


}
