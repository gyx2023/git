package com.crm.statistics.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 百科点击量统计VO
 */
@Data
public class BaikeClickStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 地市 */
    @ExcelProperty(value = "地市", index = 0)
    private String areaName;

    /** 知识点 */
    @ExcelProperty(value = "知识点", index = 1)
    private String baikeName;

    /** 点击量 */
    @ExcelProperty(value = "点击量", index = 2)
    private Long clickCount;
}
