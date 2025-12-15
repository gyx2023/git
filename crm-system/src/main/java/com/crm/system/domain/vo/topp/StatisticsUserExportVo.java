package com.crm.system.domain.vo.topp;

import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.convert.ExcelDictConvert;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

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
    private String countTb;
    @ExcelProperty(value = "（投标商机）100万以下")
    private String ybwTb;
    @ExcelProperty(value = "（投标商机）100-1000")
    private String ybyqTb;
    @ExcelProperty(value = "（投标商机）1000-5000")
    private String ybwqTb;
    @ExcelProperty(value = "（投标商机）5000以上")
    private String wqTb;
    @ExcelProperty(value = "（投标商机）预算金额(万元）")
    private String moneyTb;
    @ExcelProperty(value = "（中标商机）数量")
    private String countZb;
    @ExcelProperty(value = "（中标商机）100万以下")
    private String ybwZb;
    @ExcelProperty(value = "（中标商机）100-1000")
    private String ybyqZb;
    @ExcelProperty(value = "（中标商机）1000-5000")
    private String ybwqZb;
    @ExcelProperty(value = "（中标商机）5000以上")
    private String wqZb;
    @ExcelProperty(value = "（中标商机）项目金额（万元）")
    private String moneyZb;
    @ExcelProperty(value = "（中标商机）中标贡献度金额")
    private String gxedZb;


}
