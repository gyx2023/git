package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;


/**
 * 【请填写功能名称】视图对象 sys_area
 *
 * @author crm
 * @date 2025-11-24
 */
@Data
@ExcelIgnoreUnannotated
public class SysAreaVo {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long id;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String name;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String type;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long parent;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String pinyin;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String latlng;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String remark;

    /**
     * bss编码
     */
    @ExcelProperty(value = "bss编码")
    private String bssCode;

    /**
     * bss父级编码
     */
    @ExcelProperty(value = "bss父级编码")
    private String bssCodeParent;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long order;

    /**
     * 政企专区区县配置 1-市级中台调度 2-区县中台
     */
    @ExcelProperty(value = "政企专区区县配置 1-市级中台调度 2-区县中台")
    private Integer zqType;

    /**
     * 映射集团区域代码
     */
    @ExcelProperty(value = "映射集团区域代码")
    private String zbArea;

    /**
     * 集团 县市编码
     */
    @ExcelProperty(value = "集团 县市编码")
    private String jtCode;

    /**
     * 市ID
     */
    private int empAreaId;

    /**
     * 区县ID
     */
    private int empCountyId;


}
