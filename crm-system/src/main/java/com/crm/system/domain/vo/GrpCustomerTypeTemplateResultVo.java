package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 *
 * @author pengjun
 * @date 2025-12-02
 */
@Data
public class GrpCustomerTypeTemplateResultVo {

    /**
     * 总部集团编码
     */
    @ExcelProperty(value = "总部集团编码")
    private String hqGrpCode;

    /**
     * 集团客户类别
     */
    @ExcelProperty(value = "集团客户类别")
    private String grpCustType;

    /**
     * 此条处理结果
     */
    @ExcelProperty(value = "此条处理结果")
    private String processResult;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息")
    private String errorMessage;
}
