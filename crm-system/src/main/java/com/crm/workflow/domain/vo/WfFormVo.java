package com.crm.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 流程分类视图对象
 *
 * @author KonBAI
 * @createTime 2022/3/7 22:07
 */
@Data
@ExcelIgnoreUnannotated
public class WfFormVo {

    private static final long serialVersionUID = 1L;

    /**
     * 表单主键
     */
    @ExcelProperty(value = "表单ID")
    private Long formId;

    /**
     * 表单名称
     */
    @ExcelProperty(value = "表单名称")
    private String formName;

    /**
     * 表单内容
     */
    @ExcelProperty(value = "表单内容")
    private String content;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * 表单类型：system-系统表单，custom-自定义URL
     */
    @ExcelProperty(value = "表单类型")
    private String formType;

    /**
     * 表单URL
     */
    @ExcelProperty(value = "表单URL")
    private String formUrl;
}
