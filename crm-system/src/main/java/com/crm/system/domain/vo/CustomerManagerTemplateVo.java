package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 批量分配客户经理Excel模板VO
 *
 * @author lingma
 * @date 2025-11-28
 */
@Data
public class CustomerManagerTemplateVo {

    /**
     * 总部集团编码
     */
    @ExcelProperty(value = "总部集团编码")
    private String hqGrpCode;

    /**
     * 省级客户经理
     */
    @ExcelProperty(value = "省级客户经理")
    private String provManagerName;

    /**
     * 省级客户经理OA
     */
    @ExcelProperty(value = "省级客户经理OA")
    private String provManagerOauser;

    /**
     * 市级客户经理
     */
    @ExcelProperty(value = "市级客户经理")
    private String cityManagerName;

    /**
     * 市级客户经理OA
     */
    @ExcelProperty(value = "市级客户经理OA")
    private String cityManagerOauser;

    /**
     * 区县级客户经理
     */
    @ExcelProperty(value = "区县级客户经理")
    private String countyManagerName;

    /**
     * 区县级客户经理OA
     */
    @ExcelProperty(value = "区县级客户经理OA")
    private String countyManagerOauser;

    /**
     * 处理结果
     */
    @ExcelProperty(value = "处理结果")
    private String processResult;

}