package com.crm.system.domain.vo.identify;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.convert.ExcelDictConvert;
import lombok.Data;
import java.util.Date;



/**
 * 三清单智能评分记录视图对象 identify_score_customer
 *
 * @author crm
 * @date 2026-02-02
 */
@Data
@ExcelIgnoreUnannotated
public class IdentifyScoreCustomerVo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 地市id，上传人员归属的地市
     */
    @ExcelProperty(value = "地市id，上传人员归属的地市")
    private Long areaId;

    /**
     * 县分id，上传人员归属的县分
     */
    @ExcelProperty(value = "县分id，上传人员归属的县分")
    private Long countyId;

    /**
     * 客户编号，本次先为空，后续有需要再沉淀
     */
    @ExcelProperty(value = "客户编号，本次先为空，后续有需要再沉淀")
    private String custId;

    /**
     * 客户名称
     */
    @ExcelProperty(value = "客户名称")
    private String custName;

    /**
     * 附件名称
     */
    @ExcelProperty(value = "附件名称")
    private String fileName;

    /**
     * 附件存放地址编号
     */
    @ExcelProperty(value = "附件存放地址编号")
    private Long fileUrlId;

    /**
     * 评估总分
     */
    @ExcelProperty(value = "评估总分")
    private String totalScore;

    /**
     * 评估详情
     */
    @ExcelProperty(value = "评估详情")
    private String content;

    /**
     * 评估结果附件名称（暂时不用）
     */
    @ExcelProperty(value = "评估结果附件名称", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "暂=时不用")
    private String scoreFileName;

    /**
     * 评估结果附件存放地址编号（暂时不用）
     */
    @ExcelProperty(value = "评估结果附件存放地址编号", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "暂=时不用")
    private Long scoreFileUrlId;

    /**
     * 创建人员ID
     */
    @ExcelProperty(value = "创建人员ID")
    private Long creatorId;

    /**
     * 更新人员ID
     */
    @ExcelProperty(value = "更新人员ID")
    private Long updaterId;


    private String userName;


    private String areaName;

    private String countyName;

    private Date createTime;
}
