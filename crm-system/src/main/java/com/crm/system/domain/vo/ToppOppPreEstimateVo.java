package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.convert.ExcelDictConvert;
import lombok.Data;
import java.util.Date;



/**
 * 商机前评估信息视图对象 topp_opp_pre_estimate
 *
 * @author crm
 * @date 2025-12-16
 */
@Data
@ExcelIgnoreUnannotated
public class ToppOppPreEstimateVo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 关联商机
     */
    @ExcelProperty(value = "关联商机")
    private Long oppoId;

    /**
     * 前评支撑人
     */
    @ExcelProperty(value = "前评支撑人")
    private Long oppDesigner;

    /**
     * 项目编号
     */
    @ExcelProperty(value = "项目编号")
    private String projectNumber;

    /**
     * 项目名称
     */
    @ExcelProperty(value = "项目名称")
    private String projectName;

    /**
     * 前评估审批结果(1：通过，2：标前终止)
     */
    @ExcelProperty(value = "前评估审批结果(1：通过，2：标前终止)")
    private Long evaluationResult;

    /**
     * 是否五融(1：是，0：否)
     */
    @ExcelProperty(value = "是否五融(1：是，0：否)")
    private Integer fiveFusion;

    /**
     * 五融类型逗号分隔(融自研产品，融数，融安全，融云，融5G)
     */
    @ExcelProperty(value = "五融类型逗号分隔(融自研产品，融数，融安全，融云，融5G)")
    private String fiveFusionType;

    /**
     * 自然客户ID
     */
    @ExcelProperty(value = "自然客户ID")
    private String natureCustId;

    /**
     * 自然客户名称
     */
    @ExcelProperty(value = "自然客户名称")
    private String natureCustName;


}
