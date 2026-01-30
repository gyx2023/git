package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.convert.ExcelDictConvert;
import lombok.Data;
import java.util.Date;



/**
 * 政企主体-实际使用客户（zqzt_grp_cust_inuse）视图对象 zqzt_grp_cust_inuse
 *
 * @author crm
 * @date 2026-01-20
 */
@Data
@ExcelIgnoreUnannotated
public class ZqztGrpCustInuseVo {

    private static final long serialVersionUID = 1L;

    /**
     * 关系编码（主键）
     */
    @ExcelProperty(value = "关系编码", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "主=键")
    private String inuseId;

    /**
     * 合同编号，关联zqzt_grp_contract_info.INUSE_CONTRACT_ID
     */
    @ExcelProperty(value = "合同编号，关联zqzt_grp_contract_info.INUSE_CONTRACT_ID")
    private String contractId;

    /**
     * 合同名称
     */
    @ExcelProperty(value = "合同名称")
    private String contractName;

    /**
     * 合同签约集团ID
     */
    @ExcelProperty(value = "合同签约集团ID")
    private Integer grpIdA;

    /**
     * 客户类型：1000=非收入划转使用客户、1001=收入划转使用客户、1002=收入归集客户
     */
    @ExcelProperty(value = "客户类型：1000=非收入划转使用客户、1001=收入划转使用客户、1002=收入归集客户")
    private Integer relation;

    /**
     * 关联客户集团ID
     */
    @ExcelProperty(value = "关联客户集团ID")
    private Integer grpIdB;

    /**
     * 是否有效：1=有效、0=无效
     */
    @ExcelProperty(value = "是否有效：1=有效、0=无效")
    private String isValid;

    /**
     * 签约集团15位编码
     */
    @ExcelProperty(value = "签约集团15位编码")
    private String grpCodeA;

    /**
     * 签约集团总部编码（关联grp_group_info）
     */
    @ExcelProperty(value = "签约集团总部编码", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "关=联grp_group_info")
    private String hqGrpCodeA;

    /**
     * 签约集团名称
     */
    @ExcelProperty(value = "签约集团名称")
    private String grpNameA;

    /**
     * 关联客户15位编码
     */
    @ExcelProperty(value = "关联客户15位编码")
    private String grpCodeB;

    /**
     * 关联客户总部编码（关联grp_group_info）
     */
    @ExcelProperty(value = "关联客户总部编码", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "关=联grp_group_info")
    private String hqGrpCodeB;

    /**
     * 关联客户集团名称
     */
    @ExcelProperty(value = "关联客户集团名称")
    private String grpNameB;

    /**
     * 地市标识（如Z）
     */
    @ExcelProperty(value = "地市标识", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "如=Z")
    private String regionId;

    /**
     * 所属业务市县编码
     */
    @ExcelProperty(value = "所属业务市县编码")
    private String countyId;

    /**
     * 受理操作员ID
     */
    @ExcelProperty(value = "受理操作员ID")
    private String operatorId;

    /**
     * 备注信息
     */
    @ExcelProperty(value = "备注信息")
    private String remark;


}
