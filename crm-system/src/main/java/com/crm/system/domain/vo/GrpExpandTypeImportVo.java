package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 集团客户拓展类型导入VO
 * 用于处理Excel导入时的数据，避免在异步任务中使用需要Web上下文的转换器
 *
 * @author lingma
 * @date 2025-12-04
 */
@Data
public class GrpExpandTypeImportVo {

    /**
     * 总部集团编码
     */
    @ExcelProperty(value = "总部集团编码")
    private String hqGrpCode;

    /**
     * 集团客户拓展类型
     * 接收Excel中的标签值，在业务逻辑中转换为字典值
     */
    @ExcelProperty(value = "集团客户拓展类型")
    private String expandTypeName;

}