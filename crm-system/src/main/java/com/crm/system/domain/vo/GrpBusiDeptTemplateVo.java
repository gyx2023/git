package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
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
    @ExcelProperty(value = "客户归属部门")
    private String busiDeptName;

}