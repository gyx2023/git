package com.crm.system.domain.vo;

import java.util.Date;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;


/**
 * 系统操作记录视图对象 zqzt_oper_record
 *
 * @author crm
 * @date 2026-04-02
 */
@Data
@ExcelIgnoreUnannotated
public class ZqztOperRecordVo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增生成
     */
    @ExcelProperty(value = "主键ID，自增生成")
    private Long id;

    /**
     * 模块编码：商机/集团等大模块
     */
    @ExcelProperty(value = "模块编码：商机/集团等大模块")
    private String moduleCode;

    /**
     * 操作点编码：模块内具体操作
     */
    @ExcelProperty(value = "操作点编码：模块内具体操作")
    private String operCode;

    /**
     * 关联业务ID
     */
    @ExcelProperty(value = "关联业务ID")
    private String businessId;

    /**
     * 请求参数(JSON格式)
     */
    @ExcelProperty(value = "请求参数(JSON格式)")
    private String requestParams;

    /**
     * 扩展信息(JSON格式，非入参非返回值)
     */
    @ExcelProperty(value = "扩展信息(JSON格式，非入参非返回值)")
    private String extraInfo;

    /**
     * 操作状态：0-成功，1-失败
     */
    @ExcelProperty(value = "操作状态：0-成功，1-失败")
    private Integer status;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息")
    private String errorMsg;

    /**
     * 操作人ID
     */
    @ExcelProperty(value = "操作人ID")
    private Long operUserId;

    /**
     * 操作人姓名
     */
    @ExcelProperty(value = "操作人姓名")
    private String operUserName;

    /**
     * 操作时间
     */
    @ExcelProperty(value = "操作时间")
    private Date operTime;

    /**
     * 操作人IP
     */
    @ExcelProperty(value = "操作人IP")
    private String operIp;


}
