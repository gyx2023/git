package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 特殊名单视图对象 zqzt_sys_speciallist
 *
 * @author crm
 * @date 2026-06-16
 */
@Data
@ExcelIgnoreUnannotated
public class SpecialListVo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Integer id;

    /**
     * 场景编码
     */
    @ExcelProperty(value = "场景编码")
    private String sceneCode;

    /**
     * 目标类型
     */
    @ExcelProperty(value = "目标类型")
    private String targetType;

    /**
     * 目标值
     */
    @ExcelProperty(value = "目标值")
    private String targetValue;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态")
    private String status;

    /**
     * 生效时间
     */
    @ExcelProperty(value = "生效时间")
    private Date effectiveTime;

    /**
     * 失效时间
     */
    @ExcelProperty(value = "失效时间")
    private Date expireTime;

    /**
     * 原因
     */
    @ExcelProperty(value = "原因")
    private String reason;

    /**
     * 创建人姓名
     */
    @ExcelProperty(value = "创建人")
    private String createByName;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新人姓名
     */
    @ExcelProperty(value = "更新人")
    private String updateByName;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private Date updateTime;

}
