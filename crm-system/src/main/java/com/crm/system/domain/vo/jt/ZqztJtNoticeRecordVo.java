package com.crm.system.domain.vo.jt;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.convert.ExcelDictConvert;
import lombok.Data;
import java.util.Date;



/**
 * 集团任务前置通知接口记录表视图对象 zqzt_jt_notice_record
 *
 * @author crm
 * @date 2025-12-31
 */
@Data
@ExcelIgnoreUnannotated
public class ZqztJtNoticeRecordVo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（自增，唯一标识每条记录）
     */
    @ExcelProperty(value = "主键ID", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "自=增，唯一标识每条记录")
    private Long id;

    /**
     * 流程实例ID
     */
    @ExcelProperty(value = "流程实例ID")
    private String orderId;

    /**
     * 订单创建者OA工号
     */
    @ExcelProperty(value = "订单创建者OA工号")
    private String orderOperatorId;

    /**
     * 订单创建者OA名称
     */
    @ExcelProperty(value = "订单创建者OA名称")
    private String orderOperatorName;

    /**
     * 订单创建者省份编码
     */
    @ExcelProperty(value = "订单创建者省份编码")
    private String orderProvinceCode;

    /**
     * 订单创建者地市编码
     */
    @ExcelProperty(value = "订单创建者地市编码")
    private String orderCityCode;

    /**
     * 流程状态（3=结束，1=在途）
     */
    @ExcelProperty(value = "流程状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "3==结束，1=在途")
    private String orderStatus;

    /**
     * 任务ID（流程启动/完工等场景使用，与任务无关时为空）
     */
    @ExcelProperty(value = "任务ID", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "流=程启动/完工等场景使用，与任务无关时为空")
    private String worksheetId;

    /**
     * 环节编码
     */
    @ExcelProperty(value = "环节编码")
    private String taskCode;

    /**
     * 任务名称
     */
    @ExcelProperty(value = "任务名称")
    private String taskName;

    /**
     * 流程编码（流程唯一标识）
     */
    @ExcelProperty(value = "流程编码", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "流=程唯一标识")
    private String bizCode;

    /**
     * 处理人工号（系统自动任务时可为空）
     */
    @ExcelProperty(value = "处理人工号", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "系=统自动任务时可为空")
    private String dealUserCode;

    /**
     * 处理人名称
     */
    @ExcelProperty(value = "处理人名称")
    private String dealUserName;

    /**
     * 任务处理状态编码
     */
    @ExcelProperty(value = "任务处理状态编码")
    private String dealResultCode;

    /**
     * 入参报文体
     */
    @ExcelProperty(value = "入参报文体")
    private String body;

    /**
     * 表单报文结构（含处理意见和附件）
     */
    @ExcelProperty(value = "表单报文结构", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "含=处理意见和附件")
    private String formTemplate;

    /**
     * 返回报文体
     */
    @ExcelProperty(value = "返回报文体")
    private String resBody;


}
