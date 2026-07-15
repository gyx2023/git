package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 宽带装机地址预警明细导出视图对象（对齐旧处理页"导出"列）
 *
 * @author crm
 */
@Data
@ExcelIgnoreUnannotated
public class KdAddrWarningDetailExportVo {

    @ExcelProperty(value = "稽核类型")
    private String checkType;

    @ExcelProperty(value = "地市")
    private String areaName;

    @ExcelProperty(value = "县区")
    private String countyName;

    @ExcelProperty(value = "行业")
    private String industry;

    @ExcelProperty(value = "号码")
    private String svcNum;

    @ExcelProperty(value = "客户名称")
    private String custName;

    @ExcelProperty(value = "用户地址")
    private String userAddr;

    @ExcelProperty(value = "入网时间")
    private String openDate;

    @ExcelProperty(value = "用户状态")
    private String userStatus;

    @ExcelProperty(value = "状态变更时间")
    private String statusChgTime;

    @ExcelProperty(value = "产品ID")
    private String prodId;

    @ExcelProperty(value = "产品名称")
    private String prodName;

    @ExcelProperty(value = "上行速率（MB）")
    private String upRate;

    @ExcelProperty(value = "下行速率（MB）")
    private String downRate;

    @ExcelProperty(value = "渠道编码")
    private String chnlCode;

    @ExcelProperty(value = "渠道名称")
    private String chnlName;

    @ExcelProperty(value = "发展人ID")
    private String developerId;

    @ExcelProperty(value = "发展人姓名")
    private String developerName;

    @ExcelProperty(value = "同房间同址量")
    private String sameRoomCount;

    @ExcelProperty(value = "反馈情况")
    private String feedback;
}
