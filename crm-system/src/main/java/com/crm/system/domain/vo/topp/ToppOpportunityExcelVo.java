package com.crm.system.domain.vo.topp;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商机导出Excel专用VO
 *
 * @author system
 * @date 2026-05-19
 */
@Data
@ExcelIgnoreUnannotated
public class ToppOpportunityExcelVo{
    /**
     * 商机编号
     */
    @ExcelProperty(value = "商机编号", index = 0)
    private String oppCode;

    /**
     * 商机名称
     */
    @ExcelProperty(value = "商机名称", index = 1)
    private String oppName;

    /**
     * 业务类型（主）- 原始编码，需字典转换
     */
    @ExcelProperty(value = "业务类型（主）", index = 2)
    private String oppType;

    /**
     * 业务类型（次）- 原始字符串，需解析和字典转换
     */
    @ExcelProperty(value = "业务类型（次）", index = 3)
    private String oppIncludeType;

    /**
     * 所属一级行业 - 原始编码，需字典转换
     */
    @ExcelProperty(value = "所属一级行业", index = 4)
    private String unicomType;

    /**
     * 所属二级行业 - 原始编码，需字典转换
     */
    @ExcelProperty(value = "所属二级行业", index = 5)
    private String unicomType2;

    /**
     * 客户拓展属性 - 原始编码，需转换(01/02/03)
     */
    @ExcelProperty(value = "客户拓展属性", index = 6)
    private String developFlag;

    /**
     * 商机来源 - 原始编码，需字典转换
     */
    @ExcelProperty(value = "商机来源", index = 7)
    private String oppSource;

    /**
     * 预计合同金额
     */
    @ExcelProperty(value = "预计合同金额", index = 8)
    private BigDecimal expectContractMoney;

    /**
     * 月收入贡献
     */
    @ExcelProperty(value = "月收入贡献", index = 9)
    private BigDecimal monthIncomeContribution;

    /**
     * 预估合同周期（月）
     */
    @ExcelProperty(value = "预估合同周期", index = 10)
    private Integer contractMonth;

    /**
     * 是否招投标 - 原始Short值，需字典转换
     */
    @ExcelProperty(value = "是否招投标", index = 11)
    private String isBidding;

    /**
     * 是否需要外部资源 - 原始Short值，需转换(1/0)
     */
    @ExcelProperty(value = "是否需要外部资源", index = 12)
    private String isNeedExternalResources;

    /**
     * 是否具有创新性 - 原始Short值，需转换(1/0)
     */
    @ExcelProperty(value = "是否具有创新性", index = 13)
    private String isHaveInnovate;

    /**
     * 集团客户编号
     */
    @ExcelProperty(value = "集团客户编号", index = 14)
    private String custNo;

    /**
     * 集团客户名称
     */
    @ExcelProperty(value = "集团客户名称", index = 15)
    private String custName;

    /**
     * 集团客户一级行业 - 从setting_str1获取，需字典转换
     */
    @ExcelProperty(value = "集团客户一级行业", index = 16)
    private String customerUnicomType;

    /**
     * 集团客户二级行业 - 从department_code获取，需字典转换
     */
    @ExcelProperty(value = "集团客户二级行业", index = 17)
    private String departmentCode;

    /**
     * 创建时间 - 原始Date，需格式化
     */
    @ExcelProperty(value = "创建时间", index = 18)
    private String createTime;

    /**
     * 商机状态 - 原始编码，需字典转换
     */
    @ExcelProperty(value = "商机状态", index = 19)
    private String oppoState;

    /**
     * 商机创建人姓名
     */
    @ExcelProperty(value = "商机创建人姓名", index = 20)
    private String creatorNameCh;

    /**
     * 商机创建人电话
     */
    @ExcelProperty(value = "商机创建人电话", index = 21)
    private String creatorPhone;

    /**
     * 商机创建人归属地市
     */
    @ExcelProperty(value = "商机创建人归属地市", index = 22)
    private String creatorCity;

    /**
     * 商机创建人归属区县
     */
    @ExcelProperty(value = "商机创建人归属区县", index = 23)
    private String creatorDistrict;

    /**
     * 商机创建人归属部门
     */
    @ExcelProperty(value = "商机创建人归属部门", index = 24)
    private String creatorDept;

    /**
     * 商机客户经理姓名
     */
    @ExcelProperty(value = "商机客户经理姓名", index = 25)
    private String custManagerNameCh;

    /**
     * 商机客户经理电话
     */
    @ExcelProperty(value = "商机客户经理电话", index = 26)
    private String custManagerPhone;

    /**
     * 商机客户经理归属地市
     */
    @ExcelProperty(value = "商机客户经理归属地市", index = 27)
    private String custManagerCity;

    /**
     * 商机客户经理归属区县
     */
    @ExcelProperty(value = "商机客户经理归属区县", index = 28)
    private String custManagerDistrict;

    /**
     * 商机客户经理归属部门
     */
    @ExcelProperty(value = "商机客户经理归属部门", index = 29)
    private String custManagerDept;

    /**
     * 商机支撑经理姓名
     */
    @ExcelProperty(value = "商机支撑经理姓名", index = 30)
    private String designerNameCh;

    /**
     * 商机支撑经理电话
     */
    @ExcelProperty(value = "商机支撑经理电话", index = 31)
    private String designerPhone;

    /**
     * 商机支撑经理归属地市
     */
    @ExcelProperty(value = "商机支撑经理归属地市", index = 32)
    private String designerCity;

    /**
     * 商机支撑经理归属区县
     */
    @ExcelProperty(value = "商机支撑经理归属区县", index = 33)
    private String designerDistrict;

    /**
     * 商机支撑经理归属部门
     */
    @ExcelProperty(value = "商机支撑经理归属部门", index = 34)
    private String designerDept;

    /**
     * 线索id
     */
    @ExcelProperty(value = "线索id", index = 35)
    private Integer clueId;

    /**
     * 投标主体
     */
    @ExcelProperty(value = "投标主体", index = 36)
    private String bidBody;

    /**
     * 投标状态 - 原始编码，需字典转换
     */
    @ExcelProperty(value = "投标状态", index = 37)
    private String bidResult;

    /**
     * 投标时间 - 原始Date，需格式化
     */
    @ExcelProperty(value = "投标时间", index = 38)
    private String bidResultTime;

    /**
     * 标的金额
     */
    @ExcelProperty(value = "标的金额", index = 39)
    private BigDecimal bidMoney;

    /**
     * 中标金额
     */
    @ExcelProperty(value = "中标金额", index = 40)
    private BigDecimal matchBidMoney;

    /**
     * 投标消息公布时间 - 原始Date，需格式化
     */
    @ExcelProperty(value = "投标消息公布时间", index = 41)
    private String bidCreateTime;

    /**
     * 合同数量
     */
    @ExcelProperty(value = "合同数量", index = 42)
    private Integer contractCount;

    /**
     * 合同合计金额(万元)
     */
    @ExcelProperty(value = "合同合计金额(万元)", index = 43)
    private BigDecimal contractTotalMoney;

    /**
     * 合同签订时间 - 原始Date，需格式化
     */
    @ExcelProperty(value = "合同签订时间", index = 44)
    private String contractSignDate;

    /**
     * 合同履行期 - 原始Date，需格式化
     */
    @ExcelProperty(value = "合同履行期", index = 45)
    private String contractStartDate;

    /**
     * 合同履行开始时间 - 原始Date，需格式化
     */
    @ExcelProperty(value = "合同履行开始时间", index = 46)
    private String contractPeriodStart;

    /**
     * 合同履行结束时间 - 原始Date，需格式化
     */
    @ExcelProperty(value = "合同履行结束时间", index = 47)
    private String contractEndDate;

    /**
     * 是否有效 - 原始Boolean，需转换
     */
    @ExcelProperty(value = "是否有效", index = 48)
    private String isValid;

    /**
     * 客户需求简介
     */
    @ExcelProperty(value = "客户需求简介", index = 49)
    private String custNeed;

    /**
     * 预计签约时间 - 原始Date，需格式化
     */
    @ExcelProperty(value = "预计签约时间", index = 50)
    private String signTime;

    /**
     * 商机行业/BU打标 - 原始编码，需字典转换
     */
    @ExcelProperty(value = "商机行业/BU打标", index = 51)
    private String bgType;
}
