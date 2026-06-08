package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.utils.convert.ExcelDictConvert;
import lombok.Data;

@Data
public class CustomerExistingStockTemplateVo {
    /**
     * 总部集团编码
     */
    @ExcelProperty(value = "总部集团编码")
    private String hqGrpCode;

    /**
     * 客户归属部门
     */
    @ExcelProperty(value = "客户存增量", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_province_newold_type")
    private String provinceNewoldType;

}
