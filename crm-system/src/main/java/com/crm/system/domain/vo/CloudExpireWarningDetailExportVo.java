package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 云业务到期预警明细导出视图对象
 * <p>列顺序与处理页明细表格保持一致（对齐旧框架 userCustWarningCLOUDList.jsp 表头）：
 * 地市县分|业务号码|客户名称|开户时间|发展人名称|云类型|业务名称|订购时间|到期时间|开通时长（月）|
 * 预计年收入（元）|是否续约|（续约）云类型|业务名称|续约时间|到期时间|开通时长（月）|预计年收入（元）
 * </p>
 *
 * @author crm
 */
@Data
@ExcelIgnoreUnannotated
public class CloudExpireWarningDetailExportVo {

    @ExcelProperty(value = "地市")
    private String areaName;

    @ExcelProperty(value = "区县")
    private String countyName;

    @ExcelProperty(value = "业务号码")
    private String serialNumber;

    @ExcelProperty(value = "客户名称")
    private String grpName;

    @ExcelProperty(value = "开户时间")
    private String openDate;

    @ExcelProperty(value = "发展人名称")
    private String developerName;

    @ExcelProperty(value = "云类型")
    private String cloudTag;

    @ExcelProperty(value = "业务名称")
    private String consumeType;

    @ExcelProperty(value = "订购时间")
    private String consumeTime;

    @ExcelProperty(value = "到期时间")
    private String endTime;

    @ExcelProperty(value = "开通时长（月）")
    private String serverDuration;

    @ExcelProperty(value = "预计年收入（元）")
    private String amountPerYear;

    @ExcelProperty(value = "是否续约")
    private String isRenew;

    @ExcelProperty(value = "续约云类型")
    private String newCloudTag;

    @ExcelProperty(value = "续约业务名称")
    private String newConsumeType;

    @ExcelProperty(value = "续约时间")
    private String renewalTime;

    @ExcelProperty(value = "续约到期时间")
    private String newEndTime;

    @ExcelProperty(value = "续约开通时长（月）")
    private String newServerDuration;

    @ExcelProperty(value = "续约预计年收入（元）")
    private String newAmountPerYear;
}
