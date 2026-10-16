package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 用户红名单到期预警(000030) 明细导出视图对象
 * <p>
 * 列顺序与旧系统 UserCustWarningAction 000030 导出 title 一致（20 列）。
 * </p>
 *
 * @author crm
 */
@Data
@ExcelIgnoreUnannotated
public class HmdExpireWarningDetailExportVo {

    @ExcelProperty(value = "地市县分")
    private String areaName;

    @ExcelProperty(value = "发展人名称")
    private String developerName;

    @ExcelProperty(value = "用户号码")
    private String svcNum;

    @ExcelProperty(value = "用户姓名")
    private String custName;

    @ExcelProperty(value = "入网时间")
    private String openDate;

    @ExcelProperty(value = "产品名称")
    private String prodName;

    @ExcelProperty(value = "业务类型")
    private String hqSvcType;

    @ExcelProperty(value = "用户状态")
    private String userStatus;

    @ExcelProperty(value = "入网证件类型")
    private String certType;

    @ExcelProperty(value = "是否关键人")
    private String isGrpKey;

    @ExcelProperty(value = "红名单生效时间")
    private String redEffTime;

    @ExcelProperty(value = "红名单失效时间")
    private String redExpTime;

    @ExcelProperty(value = "是否续约")
    private String isRenew;

    @ExcelProperty(value = "续约时间")
    private String renewCreateDate;

    @ExcelProperty(value = "续约生效时间")
    private String renewEffDate;

    @ExcelProperty(value = "续约失效时间")
    private String renewExpDate;

    @ExcelProperty(value = "闭环标识")
    private String cycleFlag;

    @ExcelProperty(value = "预警等级")
    private String warningLevel;

    @ExcelProperty(value = "逾期欠费合计")
    private String overdueFee;

    @ExcelProperty(value = "最早欠费账期")
    private String arrearsCycle;
}
