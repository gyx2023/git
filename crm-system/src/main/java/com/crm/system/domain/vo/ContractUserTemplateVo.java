package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 合同关联用户导入模板VO
 */
@Data
public class ContractUserTemplateVo {

    @ExcelProperty(value = "服务号码", index = 0)
    private String svcNum;
}
