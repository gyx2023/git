package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 沃TV到期预警明细导出视图对象
 * <p>列顺序与处理页明细表格保持一致：
 * 地市|区县|业务号码|用户名称|联系人|联系人电话|发展人名称|用户状态|业务类型|产品类型|
 * 产品名称|活动名称|活动生效时间|活动失效时间|是否续约|续费活动ID|续费活动名称|续约时间|
 * 续约活动生效时间|续约活动失效时间
 * </p>
 *
 * @author crm
 */
@Data
@ExcelIgnoreUnannotated
public class WotvExpireWarningDetailExportVo {

    @ExcelProperty(value = "地市")
    private String areaName;

    @ExcelProperty(value = "区县")
    private String countyName;

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

    @ExcelProperty(value = "用户状态")
    private String userStatus;

    @ExcelProperty(value = "业务类型")
    private String serviceName;

    @ExcelProperty(value = "产品类型")
    private String prodType;

    @ExcelProperty(value = "产品名称")
    private String prodName;

    @ExcelProperty(value = "活动名称")
    private String schemeName;

    @ExcelProperty(value = "活动生效时间")
    private String effTime;

    @ExcelProperty(value = "活动失效时间")
    private String expTime;

    @ExcelProperty(value = "是否续约")
    private String isXy;

    @ExcelProperty(value = "续费活动ID")
    private String newSchemeId;

    @ExcelProperty(value = "续费活动名称")
    private String discntName;

    @ExcelProperty(value = "续约时间")
    private String renewalTime;

    @ExcelProperty(value = "续约活动生效时间")
    private String newEffTime;

    @ExcelProperty(value = "续约活动失效时间")
    private String newExpTime;
}
