package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 合同关联用户导入结果VO
 */
public class ContractUserTemplateResultVo {

    @ExcelProperty(value = "服务号码", index = 0)
    private String svcNum;

    @ExcelProperty(value = "处理结果", index = 1)
    private String processResult;

    @ExcelProperty(value = "说明", index = 2)
    private String errorMessage;

    public String getSvcNum() {
        return svcNum;
    }

    public void setSvcNum(String svcNum) {
        this.svcNum = svcNum;
    }

    public String getProcessResult() {
        return processResult;
    }

    public void setProcessResult(String processResult) {
        this.processResult = processResult;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
