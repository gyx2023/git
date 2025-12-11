package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.convert.ExcelDictConvert;
import lombok.Data;
import java.util.Date;



/**
 * 行业大类-亚类-细类分类视图对象 sys_category
 *
 * @author crm
 * @date 2025-11-24
 */
@Data
@ExcelIgnoreUnannotated
public class SysCategoryVo {

    private static final long serialVersionUID = 1L;

    /**
     * 行业唯一编码，层级联动
     */
    @ExcelProperty(value = "行业唯一编码，层级联动")
    private String id;

    /**
     * 行业名称
     */
    @ExcelProperty(value = "行业名称")
    private String name;

    /**
     * 层级类型（大类、亚类、细类）
     */
    @ExcelProperty(value = "层级类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "大=类、亚类、细类")
    private String type;

    /**
     * 父级编码，关联上级ID
     */
    @ExcelProperty(value = "父级编码，关联上级ID")
    private String parent;

    /**
     * 行业名称拼音，用于检索
     */
    @ExcelProperty(value = "行业名称拼音，用于检索")
    private String pinyin;


}
