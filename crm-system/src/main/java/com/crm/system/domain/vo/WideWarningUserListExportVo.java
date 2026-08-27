package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 宽带到期预警用户清单导出视图对象（对齐老框架 userCustWarningWIDEList.jsp 列）
 *
 * @author crm
 */
@Data
@ExcelIgnoreUnannotated
public class WideWarningUserListExportVo {

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

    @ExcelProperty(value = "年费(元)")
    private String amountPerYear;

    @ExcelProperty(value = "活动生效时间")
    private String effTime;

    @ExcelProperty(value = "活动失效时间")
    private String expTime;

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

    @ExcelProperty(value = "活动周期（月）")
    private String newSchemePeriod;

    @ExcelProperty(value = "年费")
    private String newSchemeFee;

    @ExcelProperty(value = "是否转融合")
    private String transferFlag;

    @ExcelProperty(value = "新资费id")
    private String newSchemeId;

    @ExcelProperty(value = "新资费名称")
    private String newSchemeNameTransfer;

    @ExcelProperty(value = "新资费受理时间")
    private String newCreateTimeTransfer;

    @ExcelProperty(value = "新资费生效时间")
    private String newEffTimeTransfer;

    @ExcelProperty(value = "新资费失效时间")
    private String newExpTimeTransfer;

    @ExcelProperty(value = "用户状态")
    private String userStatus;
}
