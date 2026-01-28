package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 合同关联用户导入模板VO
 */
public class ContractUserTemplateVo {

    @ExcelProperty(value = "服务号码", index = 0)
    private String svcNum;

    public String getSvcNum() {
        return svcNum;
    }

    public void setSvcNum(String svcNum) {
        this.svcNum = svcNum;
    }
}
