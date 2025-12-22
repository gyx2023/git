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
public class SignContractUserExportVo implements Serializable {
    private static final long serialVersionUID = 1L;
    @ExcelProperty(value = "姓名")
    private String userName;
    @ExcelProperty(value = "数量")
    private String countQy;
    @ExcelProperty(value = "100万以下")
    private String ybwQy;
    @ExcelProperty(value = "100-1000")
    private String ybyqQy;
    @ExcelProperty(value = "1000-5000")
    private String ybwqQy;
    @ExcelProperty(value = "5000以上")
    private String wqQy;
    @ExcelProperty(value = "预计签约金额(万元）")
    private String moneyQy;
    @ExcelProperty(value = "个人贡献度金额")
    private String gxedMoney;
}
