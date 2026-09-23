package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 特殊活动到期预警明细导出视图对象
 * <p>列顺序与处理页预警明细表格展示顺序保持一致：
 * 地市|区县|业务号码|用户名称|发展人名称|活动ID|活动名称|业务类型|产品类型|
 * 活动生效时间|活动失效时间|是否续约|续约活动名称|续约时间|续约活动生效时间|续约活动失效时间
 * </p>
 *
 * @author crm
 */
@Data
@ExcelIgnoreUnannotated
public class SpecialActivityWarningDetailExportVo {

    @ExcelProperty(value = "地市")
    private String areaName;

    @ExcelProperty(value = "区县")
    private String countyName;

    @ExcelProperty(value = "业务号码")
    private String svcNum;

    @ExcelProperty(value = "用户名称")
    private String custName;

    @ExcelProperty(value = "发展人名称")
    private String developerName;

    @ExcelProperty(value = "活动ID")
    private String schemeId;

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

    @ExcelProperty(value = "是否续约")
    private String isXy;

    @ExcelProperty(value = "续约活动名称")
    private String newSchemeName;

    @ExcelProperty(value = "续约时间")
    private String renewalTime;

    @ExcelProperty(value = "续约活动生效时间")
    private String newEffTime;

    @ExcelProperty(value = "续约活动失效时间")
    private String newExpTime;
}
