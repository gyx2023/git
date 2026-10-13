package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 台账导出 视图对象
 */
@Data
public class ZqztLedgerInfoExportVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty("服务大厅工单号")
    private String orderNo;

    @ExcelProperty("签约时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date signDate;

    @ExcelProperty("服务大厅创建时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date orderCreateTime;

    @ExcelProperty("地市")
    private String cityName;

    @ExcelProperty("区县")
    private String districtName;

    @ExcelProperty("街道")
    private String streetName;

    @ExcelProperty("企业名称")
    private String companyName;

    @ExcelProperty("社会统一信用代码")
    private String creditCode;

    @ExcelProperty("签约路数")
    private Integer signRoutes;

    @ExcelProperty("联通客户经理")
    private String unicomManager;

    @ExcelProperty("联通客户经理联系方式")
    private String unicomManagerPhone;

    @ExcelProperty("全流程状态")
    private String status;

    @ExcelProperty("摄像头竣工时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date cameraFinishDate;
}
