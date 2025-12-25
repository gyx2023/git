package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.convert.ExcelDictConvert;
import lombok.Data;
import java.util.Date;



/**
 * 统一编码视图对象 sys_code
 *
 * @author crm
 * @date 2025-12-15
 */
@Data
@ExcelIgnoreUnannotated
public class SysCodeVo {

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
    private String code;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Long order;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private String remark;

    /**
     * 是否是系统编码  1：是  0：否 可编辑
     */
    @ExcelProperty(value = "是否是系统编码  1：是  0：否 可编辑")
    private String isSystem;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Long settingInt1;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Long settingInt2;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private String settingStr1;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private String settingStr2;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private String settingStr3;


}
