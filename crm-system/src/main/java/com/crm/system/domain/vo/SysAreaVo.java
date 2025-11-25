package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.convert.ExcelDictConvert;
import lombok.Data;
import java.util.Date;



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
    @ExcelProperty(value = "ID")
    private Long id;

    /**
     * 名称
     */
    @ExcelProperty(value = "名称")
    private String name;

    /**
     * 类型
     */
    @ExcelProperty(value = "类型")
    private String type;

    /**
     * 父级ID
     */
    @ExcelProperty(value = "父级ID")
    private Long parent;

    /**
     * 拼音
     */
    @ExcelProperty(value = "拼音")
    private String pinyin;

    /**
     * 经纬度
     */
    @ExcelProperty(value = "经纬度")
    private String latlng;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
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
     * 排序
     */
    @ExcelProperty(value = "排序")
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
    private Long zbArea;

    /**
     * 集团 县市编码
     */
    @ExcelProperty(value = "集团 县市编码")
    private String jtCode;


}