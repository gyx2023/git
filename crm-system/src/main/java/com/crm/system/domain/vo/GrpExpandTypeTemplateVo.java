package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.utils.convert.ExcelDictConvert;
import lombok.Data;

/**
 * 集团客户拓展类型维护模板VO
 *
 * @author lingma
 * @date 2025-12-02
 */
@Data
public class GrpExpandTypeTemplateVo {

    /**
     * 总部集团编码
     */
    @ExcelProperty(value = "总部集团编码")
    private String hqGrpCode;

    /**
     * 集团客户拓展类型
     */
    @ExcelProperty(value = "集团客户拓展类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_expand_type")
    private String expandTypeName;

}
