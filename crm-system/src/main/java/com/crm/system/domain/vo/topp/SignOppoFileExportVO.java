package com.crm.system.domain.vo.topp;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class SignOppoFileExportVO {
    @ExcelProperty("商机编码")
    private String oppCode;
    @ExcelProperty("商机名称")
    private String oppName;
    @ExcelProperty("客户名称")
    private String custName;
    @ExcelProperty("客户编号")
    private String custNo;
    @ExcelProperty("商机来源")
    private String oppSource;
    @ExcelProperty("预计合同金额")
    private String expectContractMoney;
    @ExcelProperty("客户行业")
    private String unicomType;
    @ExcelProperty("地市区县")
    private String areaCounty;
    @ExcelProperty("客户经理")
    private String customerManager;
    @ExcelProperty("所属组织")
    private String orgName;
    @ExcelProperty("手机号")
    private String mobilePhone;
    @ExcelProperty("支撑经理")
    private String supportManager;
    @ExcelProperty("主业务")
    private String mainBusiness;
    @ExcelProperty("包含业务")
    private String includeBusiness;
    @ExcelProperty("主要产品")
    private String mainProduct;
    @ExcelProperty("拓展属性")
    private String developProperty;
    @ExcelProperty("创建时间")
    private String createTime;
    @ExcelProperty("状态")
    private String oppStatus;
    @ExcelProperty("环节")
    private String phase;
    @ExcelProperty("打标人")
    private String marker;
    @ExcelProperty("环节")
    private String levelArea;
    @ExcelProperty("打标人组织")
    private String markerOrg;
    @ExcelProperty("打标时间")
    private String markTime;
    @ExcelProperty("审核时间")
    private String auditTime;
    @ExcelProperty("预计签约时间")
    private String signTime;
    @ExcelProperty("是否有效")
    private String isValid;
}
