package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 集团客户存增量维护结果VO
 *
 * @author pengjun
 * @date 2025-12-02
 */
@Data
public class GrpStockTemplateResultVo {

    /**
     * 总部集团编码
     */
    @ExcelProperty(value = "总部集团编码")
    private String hqGrpCode;

    /**
     * 集团客户存增量
     */
    @ExcelProperty(value = "集团客户存增量")
    private String provinceNewoldType;

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
