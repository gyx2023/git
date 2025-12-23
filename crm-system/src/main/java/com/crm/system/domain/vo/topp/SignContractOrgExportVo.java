package com.crm.system.domain.vo.topp;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户对象导出VO
 *
 * @author Lion Li
 */

@Data
@NoArgsConstructor
public class SignContractOrgExportVo implements Serializable {
    private static final long serialVersionUID = 1L;
    @ExcelProperty(value = "地市")
    private String deptName;
    @ExcelProperty(value = "数量")
    private Integer countQy;
    @ExcelProperty(value = "100万以下")
    private Integer ybwQy;
    @ExcelProperty(value = "100-1000")
    private Integer ybyqQy;
    @ExcelProperty(value = "1000-5000")
    private Integer ybwqQy;
    @ExcelProperty(value = "5000以上")
    private Integer wqQy;
    @ExcelProperty(value = "预计签约金额(万元）")
    private Double moneyQy;
}
