package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 团购折扣到期预警明细导出视图对象
 * <p>列顺序与旧系统 UserCustWarningAction.exportUserCustWarningList 的导出表头一致：
 * 用户号码|县分|订购客户名称|发展人名称|专属活动ID|专属活动名称|生效时间|失效时间|
 * 续约活动名称|续约时间|续约生效时间|续约失效时间|闭环标识|预警等级
 * </p>
 *
 * @author crm
 */
@Data
@ExcelIgnoreUnannotated
public class GroupBuyWarningDetailExportVo {

    @ExcelProperty(value = "用户号码")
    private String svcNum;

    @ExcelProperty(value = "县分")
    private String countyId;

    @ExcelProperty(value = "订购客户名称")
    private String custName;

    @ExcelProperty(value = "发展人名称")
    private String developerName;

    @ExcelProperty(value = "专属活动ID")
    private String schemeId;

    @ExcelProperty(value = "专属活动名称")
    private String schemeName;

    @ExcelProperty(value = "生效时间")
    private String effTime;

    @ExcelProperty(value = "失效时间")
    private String expTime;

    @ExcelProperty(value = "续约活动名称")
    private String newSchemeName;

    @ExcelProperty(value = "续约时间")
    private String renewalTime;

    @ExcelProperty(value = "续约生效时间")
    private String newEffTime;

    @ExcelProperty(value = "续约失效时间")
    private String newExpTime;

    @ExcelProperty(value = "闭环标识")
    private String cycleFlagText;

    @ExcelProperty(value = "预警等级")
    private String warningLevelText;
}
