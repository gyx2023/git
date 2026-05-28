package com.crm.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * 支撑工时统计VO
 *
 * @author crm
 */
@Data
public class SupportHoursVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商机ID */
    @ExcelProperty(value = "商机ID", index = 0)
    private Integer oppId;

    /** 商机编号 */
    @ExcelProperty(value = "商机编号", index = 1)
    private String oppCode;

    /** 商机名称 */
    @ExcelProperty(value = "商机名称", index = 2)
    private String oppName;

    /** 客户名称 */
    @ExcelProperty(value = "客户名称", index = 3)
    private String custName;

    /** 预期合同(万元) */
    @ExcelProperty(value = "预期合同(万元)", index = 4)
    private String xpectContractMoney;

    /** 客户行业 */
    @ExcelProperty(value = "客户行业", index = 5)
    private String unicomType;

    /** 客户经理 */
    @ExcelProperty(value = "客户经理", index = 6)
    private String custManagerName;

    /** 商机到达时间 */
    @ExcelProperty(value = "商机到达时间", index = 7)
    private String oppTime;

    /** 主责支撑经理 */
    @ExcelProperty(value = "主责支撑经理", index = 8)
    private String firstManagetName;

    /** 非主责支撑经理 */
    @ExcelProperty(value = "非主责支撑经理", index = 9)
    private String secondManagetName;

    /** 支撑阶段 */
    @ExcelProperty(value = "支撑阶段", index = 10)
    private String taskName;

    /** 支撑阶段编码 */
    @ExcelProperty(value = "支撑阶段编码", index = 11)
    private String supportStage;

    /** 合计小时数 */
    @ExcelProperty(value = "合计小时数", index = 12)
    private Double totalHours;

    /** 月支撑小时数 */
    @ExcelIgnore
    private LinkedHashMap<String, Double> monthHour;

    /** 周支撑小时数 */
    @ExcelIgnore
    private LinkedHashMap<String, Double> weeklyHours;

    /** 支撑经理姓名(个人支撑商机统计用) */
    @ExcelIgnore
    private String supportManagerName;
}
