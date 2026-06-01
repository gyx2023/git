package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.utils.convert.ExcelDictConvert;
import lombok.Data;

@Data
public class CustomerTypeTemplateVo {
    /**
     * 总部集团编码
     */
    @ExcelProperty(value = "总部集团编码")
    private String hqGrpCode;

    /**
     * 客户归属部门
     */
    @ExcelProperty(value = "集团客户类别", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_cust_type")
    private String grpCustType;

}
