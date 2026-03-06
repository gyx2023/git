package com.crm.system.domain.vo.topp;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 老项目商机导出VO
 * 用于Excel导出，包含完整的表头注解
 *
 * @author wcf
 * @date 2026-02-09
 */
@Data
public class OldOpportunityVO {

    @ExcelProperty("商机编号")
    private String oppCode;

    @ExcelProperty("商机名称")
    private String oppName;

    @ExcelProperty("业务类型（主）")
    private String oppType;

    @ExcelProperty("业务类型（次）")
    private String oppIncludeType;

    @ExcelProperty("所属一级行业")
    private String unicomType;

    @ExcelProperty("商机来源")
    private String oppSource;

    @ExcelProperty("预计合同金额")
    private BigDecimal expectContractMoney;

    @ExcelProperty("月收入贡献")
    private BigDecimal monthIncomeContribution;

    @ExcelProperty("预估合同周期")
    private Long contractMonth;

    @ExcelProperty("是否招投标")
    private String isBidding;

    @ExcelProperty("集团客户编号")
    private String custNo;

    @ExcelProperty("集团客户名称")
    private String custName;

    @ExcelProperty("创建时间")
    private Date createTime;

    @ExcelProperty("商机状态")
    private String oppoState;

    @ExcelProperty("商机创建人姓名")
    private String creatorName;

    @ExcelProperty("商机创建人电话")
    private String creatorPhone;

    @ExcelProperty("商机创建人归属地市")
    private String creatorCity;

    @ExcelProperty("商机创建人归属区县")
    private String creatorCounty;

    @ExcelProperty("商机创建人归属部门")
    private String creatorOrg;

    @ExcelProperty("商机客户经理姓名")
    private String custManagerName;

    @ExcelProperty("商机客户经理电话")
    private String custManagerTel;

    @ExcelProperty("商机客户经理归属地市")
    private String custManagerCity;

    @ExcelProperty("商机客户经理归属区县")
    private String custManagerCounty;

    @ExcelProperty("商机客户经理归属部门")
    private String custManagerOrg;

    @ExcelProperty("商机支撑经理姓名")
    private String designerName;

    @ExcelProperty("商机支撑经理电话")
    private String designerPhone;

    @ExcelProperty("商机支撑经理归属地市")
    private String designerCity;

    @ExcelProperty("商机支撑经理归属区县")
    private String designerCounty;

    @ExcelProperty("商机支撑经理归属部门")
    private String designerOrg;

    @ExcelProperty("是否有效")
    private String isValid;

    @ExcelProperty("主要产品/解决方案")
    private String mainProduct;

    @ExcelProperty("项目来源分类")
    private String oppDataReform;

    @ExcelProperty("商机签约概率")
    private String oppWinProb;

    @ExcelProperty("预计签约时间")
    private Date signTime;

    @ExcelProperty("商机增长类型")
    private String oppGrowthType;

    @ExcelProperty("拜访次数")
    private Long visitCnt;

    @ExcelProperty("最近访问时间")
    private Date lastVisitTime;

    @ExcelProperty("商机转化状态")
    private String oppoConversionState;

    @ExcelProperty("合同编号")
    private String contractNos;

    @ExcelProperty("电子表单编号")
    private String eformNos;
}
