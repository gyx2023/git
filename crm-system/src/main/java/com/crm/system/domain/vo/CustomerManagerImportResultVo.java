package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 批量分配客户经理导入结果VO
 *
 * @author lingma
 * @date 2025-11-28
 */
@Data
public class CustomerManagerImportResultVo {

    /**
     * 总部集团编码
     */
    @ExcelProperty(value = "总部集团编码")
    private String hqGrpCode;


    /**
     * 实体客户经理OA账号
     */
    @ExcelProperty(value = "实体客户经理OA账号")
    private String custManagerOauser;


    /**
     * 处理结果
     */
    @ExcelProperty(value = "处理结果")
    private String processResult;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息")
    private String errorMessage;
}
