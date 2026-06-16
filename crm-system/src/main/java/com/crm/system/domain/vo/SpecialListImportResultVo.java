package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 特殊名单导入结果对象
 *
 * @author crm
 * @date 2026-06-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpecialListImportResultVo extends SpecialListImportVo {

    /**
     * 处理结果（成功/失败）
     */
    @ExcelProperty(index = 6, value = "处理结果")
    private String result;

    /**
     * 错误信息
     */
    @ExcelProperty(index = 7, value = "错误信息")
    @ColumnWidth(50)
    private String errorMsg;

}
