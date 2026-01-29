package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 合同关联用户导入结果VO
 */
@Data
public class ContractUserTemplateResultVo {

    @ExcelProperty(value = "服务号码", index = 0)
    private String svcNum;

    @ExcelProperty(value = "处理结果", index = 1)
    private String processResult;

    @ExcelProperty(value = "说明", index = 2)
    private String errorMessage;

}
