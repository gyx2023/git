package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 集团客户拓展类型维护结果VO
 *
 * @author lingma
 * @date 2025-12-02
 */
@Data
public class GrpExpandTypeTemplateResultVo {

    /**
     * 总部集团编码
     */
    @ExcelProperty(value = "总部集团编码")
    private String hqGrpCode;

    /**
     * 集团客户拓展类型
     */
    @ExcelProperty(value = "集团客户拓展类型")
    private String expandTypeName;

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