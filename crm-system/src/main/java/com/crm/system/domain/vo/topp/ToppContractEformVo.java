package com.crm.system.domain.vo.topp;

import java.math.BigDecimal;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.utils.convert.ExcelDictConvert;
import lombok.Data;


/**
 * 【请填写功能名称】视图对象 topp_contract_eform
 *
 * @author crm
 * @date 2025-12-18
 */
@Data
@ExcelIgnoreUnannotated
public class ToppContractEformVo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 关联商机
     */
    @ExcelProperty(value = "关联商机")
    private Long projectId;

    /**
     * 电子表单编号
     */
    @ExcelProperty(value = "电子表单编号")
    private String eformNo;

    /**
     * 电子表单名称
     */
    @ExcelProperty(value = "电子表单名称")
    private String eformName;

    /**
     * 客户名称
     */
    @ExcelProperty(value = "客户名称")
    private String custName;

    /**
     * 我方签约主体
     */
    @ExcelProperty(value = "我方签约主体")
    private String ourCompany;

    /**
     * 合同承办人OA
     */
    @ExcelProperty(value = "合同承办人OA")
    private String agentPersonOa;

    /**
     * 合同承办人
     */
    @ExcelProperty(value = "合同承办人")
    private String agentPersonName;

    /**
     * 合同总金额（元）
     */
    @ExcelProperty(value = "合同总金额", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "元=")
    private BigDecimal totalMoney;

    /**
     * 签订日期时间
     */
    @ExcelProperty(value = "签订日期时间")
    private String signTime;

    /**
     * 合同开始日期
     */
    @ExcelProperty(value = "合同开始日期")
    private String startDate;

    /**
     * 合同结束日期
     */
    @ExcelProperty(value = "合同结束日期")
    private String endDate;

    /**
     * 标准文本类型
     */
    @ExcelProperty(value = "标准文本类型")
    private String textType;

    /**
     * 业务类型
     */
    @ExcelProperty(value = "业务类型")
    private String busiType;

    /**
     * 创建人员
     */
    @ExcelProperty(value = "创建人员")
    private Long creator;

    /**
     * 是否有效
     */
    @ExcelProperty(value = "是否有效")
    private Integer isValid;


}
