package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 双线到期预警明细导出视图对象
 * <p>列顺序与处理页明细表格保持一致（对齐旧框架双线明细列）：
 * 业务号码|用户姓名|联系人|联系电话|发展人名称|活动名称|业务大类|业务类型|接入方式|
 * 活动生效时间|活动失效时间|年费(元)|速率(MB)|安装地址|是否续约|续约活动名称|续约时间|
 * 续约生效时间|续约失效时间|活动周期(月)|活动年费|是否转融合|用户状态
 * </p>
 *
 * @author crm
 */
@Data
@ExcelIgnoreUnannotated
public class DoubleWideExpireWarningDetailExportVo {

    @ExcelProperty(value = "业务号码")
    private String svcNum;

    @ExcelProperty(value = "用户姓名")
    private String custName;

    @ExcelProperty(value = "联系人")
    private String contactMan;

    @ExcelProperty(value = "联系电话")
    private String contactPhone;

    @ExcelProperty(value = "发展人名称")
    private String developerName;

    @ExcelProperty(value = "活动名称")
    private String schemeName;

    @ExcelProperty(value = "业务大类")
    private String lvlName;

    @ExcelProperty(value = "业务类型")
    private String serviceName;

    @ExcelProperty(value = "接入方式")
    private String connType;

    @ExcelProperty(value = "活动生效时间")
    private String effTime;

    @ExcelProperty(value = "活动失效时间")
    private String expTime;

    @ExcelProperty(value = "年费(元)")
    private String amountPerYear;

    @ExcelProperty(value = "速率(MB)")
    private String rate;

    @ExcelProperty(value = "安装地址")
    private String userAddr;

    @ExcelProperty(value = "是否续约")
    private String exteFlag;

    @ExcelProperty(value = "续约活动名称")
    private String newSchemeName;

    @ExcelProperty(value = "续约时间")
    private String newCreateTime;

    @ExcelProperty(value = "续约生效时间")
    private String newEffTime;

    @ExcelProperty(value = "续约失效时间")
    private String newExpTime;

    @ExcelProperty(value = "活动周期(月)")
    private String newSchemePeriod;

    @ExcelProperty(value = "活动年费")
    private String newSchemeFee;

    @ExcelProperty(value = "是否转融合")
    private String relationTypeName;

    @ExcelProperty(value = "用户状态")
    private String userStatus;
}
