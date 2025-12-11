package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.convert.ExcelDictConvert;
import lombok.Data;

/**
 * 客户归属部门模板VO
 *
 * @author lingma
 * @date 2025-12-01
 */
@Data
public class GrpBusiDeptTemplateVo {

    /**
     * 总部集团编码
     */
    @ExcelProperty(value = "总部集团编码")
    private String hqGrpCode;

    /**
     * 客户归属部门
     */
    @ExcelProperty(value = "客户归属部门", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_busi_dept_type")
    private String busiDeptName;

}