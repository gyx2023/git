package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 特殊名单导入模板对象
 *
 * @author crm
 * @date 2026-06-16
 */
@Data
public class SpecialListImportVo {

    /**
     * 场景编码
     */
    @ExcelProperty(index = 0, value = "场景名称")
    private String sceneCode;

    /**
     * 目标值
     */
    @ExcelProperty(index = 1, value = "特殊名单值")
    private String targetValue;

    /**
     * 生效时间
     */
    @ExcelProperty(index = 2, value = "生效时间")
    private Date effectiveTime;

    /**
     * 失效时间
     */
    @ExcelProperty(index = 3, value = "失效时间")
    private Date expireTime;

    /**
     * 原因
     */
    @ExcelProperty(index = 4, value = "备注")
    private String reason;

}
