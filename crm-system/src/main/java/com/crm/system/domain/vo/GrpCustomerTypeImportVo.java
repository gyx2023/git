package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import lombok.Data;

/**
 * 用于处理Excel导入时的数据，避免在异步任务中使用需要Web上下文的转换器
 *
 * @author pengjun
 * @date 2025-12-04
 */
@Data
public class GrpCustomerTypeImportVo {

    /**
     * 总部集团编码
     */
    @ExcelProperty(value = "总部集团编码")
    private String hqGrpCode;

    /**
     * 客户归属部门
     * 接收Excel中的标签值，在业务逻辑中转换为字典值
     */
    @ExcelProperty(value = "集团客户类别")
    private String grpCustType;
}
