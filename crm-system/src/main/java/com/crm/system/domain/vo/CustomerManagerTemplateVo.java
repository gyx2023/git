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
     * 实体客户经理OA账号
     */
    @ExcelProperty(value = "实体客户经理OA账号")
    private String custManagerOauser;
}
