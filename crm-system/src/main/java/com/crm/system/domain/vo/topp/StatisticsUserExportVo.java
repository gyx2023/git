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
public class StatisticsUserExportVo implements Serializable {
    private static final long serialVersionUID = 1L;
    @ExcelProperty(value = "姓名")
    private String userName;
    @ExcelProperty(value = "（投标商机）数量")
    private Integer countTb;
    @ExcelProperty(value = "（投标商机）100万以下")
    private Integer ybwTb;
    @ExcelProperty(value = "（投标商机）100-1000")
    private Integer ybyqTb;
    @ExcelProperty(value = "（投标商机）1000-5000")
    private Integer ybwqTb;
    @ExcelProperty(value = "（投标商机）5000以上")
    private Integer wqTb;
    @ExcelProperty(value = "（投标商机）预算金额(万元）")
    private Double moneyTb;
    @ExcelProperty(value = "（中标商机）数量")
    private Integer countZb;
    @ExcelProperty(value = "（中标商机）100万以下")
    private Integer ybwZb;
    @ExcelProperty(value = "（中标商机）100-1000")
    private Integer ybyqZb;
    @ExcelProperty(value = "（中标商机）1000-5000")
    private Integer ybwqZb;
    @ExcelProperty(value = "（中标商机）5000以上")
    private Integer wqZb;
    @ExcelProperty(value = "（中标商机）项目金额（万元）")
    private Double moneyZb;
    @ExcelProperty(value = "（中标商机）中标贡献度金额")
    private Double gxedZb;


}
