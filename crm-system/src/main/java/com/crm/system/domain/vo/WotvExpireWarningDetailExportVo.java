package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 沃TV到期预警明细导出视图对象
 *
 * @author crm
 */
@Data
@ExcelIgnoreUnannotated
public class WotvExpireWarningDetailExportVo {

    @ExcelProperty(value = "业务号码")
    private String svcNum;

    @ExcelProperty(value = "用户名称")
    private String custName;

    @ExcelProperty(value = "联系人")
    private String contactMan;

    @ExcelProperty(value = "联系人电话")
    private String contactPhone;

    @ExcelProperty(value = "发展人名称")
    private String developerName;

    @ExcelProperty(value = "活动名称")
    private String schemeName;

    @ExcelProperty(value = "业务类型")
    private String serviceName;

    @ExcelProperty(value = "产品类型")
    private String prodType;

    @ExcelProperty(value = "活动生效时间")
    private String effTime;

    @ExcelProperty(value = "活动失效时间")
    private String expTime;

    @ExcelProperty(value = "续费活动ID")
    private String newSchemeId;

    @ExcelProperty(value = "续约时间")
    private String renewalTime;

    @ExcelProperty(value = "续约活动生效时间")
    private String newEffTime;

    @ExcelProperty(value = "续约活动失效时间")
    private String newExpTime;
}
