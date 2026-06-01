package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 合同导出 Excel VO
 */
@Data
public class ToppContractExcelVo {
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
     * 集团客户名称
     */
    @ExcelProperty(value = "集团客户名称", index = 2)
    private String custName;

    /**
     * 集团客户编码
     */
    @ExcelProperty(value = "集团客户编码", index = 3)
    private String custNo;

    /**
     * 业务类型（主）
     */
    @ExcelProperty(value = "业务类型（主）", index = 4)
    private String oppType;

    /**
     * 业务类型（全）
     */
    @ExcelProperty(value = "业务类型（全）", index = 5)
    private String oppIncludeType;

    /**
     * 商机所属一级行业
     */
    @ExcelProperty(value = "商机所属一级行业", index = 6)
    private String unicomType;

    /**
     * 商机所属二级行业
     */
    @ExcelProperty(value = "商机所属二级行业", index = 7)
    private String unicomType2;

    /**
     * 商机来源
     */
    @ExcelProperty(value = "商机来源", index = 8)
    private String oppSource;

    /**
     * 商机客户经理姓名
     */
    @ExcelProperty(value = "商机客户经理姓名", index = 9)
    private String custManagerName;

    /**
     * 商机客户经理电话
     */
    @ExcelProperty(value = "商机客户经理电话", index = 10)
    private String custManagerTel;

    /**
     * 商机客户经理归属地市区县
     */
    @ExcelProperty(value = "商机客户经理归属地市区县", index = 11)
    private String managerArea;

    /**
     * 商机客户经理归属部门
     */
    @ExcelProperty(value = "商机客户经理归属部门", index = 12)
    private String managerOrg;

    /**
     * 合同ID
     */
    @ExcelProperty(value = "合同ID", index = 13)
    private Long contractId;

    /**
     * 合同编号
     */
    @ExcelProperty(value = "合同编号", index = 14)
    private String contractNo;

    /**
     * 合同名称
     */
    @ExcelProperty(value = "合同名称", index = 15)
    private String contractName;

    /**
     * 合同种类
     */
    @ExcelProperty(value = "合同种类", index = 16)
    private String contractAddType;

    /**
     * 合同类型
     */
    @ExcelProperty(value = "合同类型", index = 17)
    private String contractType;

    /**
     * 合同总金额
     */
    @ExcelProperty(value = "合同总金额", index = 18)
    private BigDecimal totalMoney;

    /**
     * 合同税率
     */
    @ExcelProperty(value = "合同税率", index = 19)
    private String zzsSl;

    /**
     * 不含增值税合同额
     */
    @ExcelProperty(value = "不含增值税合同额", index = 20)
    private String bhZzsMoney;

    /**
     * 计收类型
     */
    @ExcelProperty(value = "计收类型", index = 21)
    private String incomeType;

    /**
     * 合同签订日期
     */
    @ExcelProperty(value = "合同签订日期", index = 22)
    private Date signDate;

    /**
     * 合同开始履行日期
     */
    @ExcelProperty(value = "合同开始履行日期", index = 23)
    private Date startDate;

    /**
     * 合同履行结束日期
     */
    @ExcelProperty(value = "合同履行结束日期", index = 24)
    private Date endDate;

    /**
     * 合同一级分类
     */
    @ExcelProperty(value = "合同一级分类", index = 25)
    private String contractCateL1Name;

    /**
     * 合同二级分类
     */
    @ExcelProperty(value = "合同二级分类", index = 26)
    private String contractCateL2Name;

    /**
     * 合同三级分类
     */
    @ExcelProperty(value = "合同三级分类", index = 27)
    private String contractCateL3Name;

    /**
     * 签约主体
     */
    @ExcelProperty(value = "签约主体", index = 28)
    private String wfZt;

    /**
     * 对方客户
     */
    @ExcelProperty(value = "对方客户", index = 29)
    private String dfName;

    /**
     * 承办部门
     */
    @ExcelProperty(value = "承办部门", index = 30)
    private String cbDepartment;

    /**
     * 履行部门
     */
    @ExcelProperty(value = "履行部门", index = 31)
    private String lxDepartment;

    /**
     * 合同状态
     */
    @ExcelProperty(value = "合同状态", index = 32)
    private String contState;

    /**
     * 是否有效
     */
    @ExcelProperty(value = "是否有效", index = 33)
    private String isValid;

    /**
     * 合同分类
     */
    @ExcelProperty(value = "合同分类", index = 34)
    private String contractCategory;

    /**
     * 合同说明及签订依据
     */
    @ExcelProperty(value = "合同说明及签订依据", index = 35)
    private String htSmQdyj;

    /**
     * 累计确认收入
     */
    @ExcelProperty(value = "累计确认收入", index = 36)
    private BigDecimal totalRealIncome;

    /**
     * 累计预估收入
     */
    @ExcelProperty(value = "累计预估收入", index = 37)
    private BigDecimal totalPlanIncome;

    /**
     * 当年累计确认收入
     */
    @ExcelProperty(value = "当年累计确认收入", index = 38)
    private BigDecimal yearRealIncome;

    /**
     * 当年累计预估收入
     */
    @ExcelProperty(value = "当年累计预估收入", index = 39)
    private BigDecimal yearPlanIncome;

    /**
     * 上年累计确认收入
     */
    @ExcelProperty(value = "上年累计确认收入", index = 40)
    private BigDecimal lastYearRealIncome;

    /**
     * 上年累计预估收入
     */
    @ExcelProperty(value = "上年累计预估收入", index = 41)
    private BigDecimal lastYearPlanIncome;

    /**
     * 上月预估收入
     */
    @ExcelProperty(value = "上月预估收入", index = 42)
    private BigDecimal lastMonthPlanIncome;

    /**
     * 上月确认收入
     */
    @ExcelProperty(value = "上月确认收入", index = 43)
    private BigDecimal lastMonthRealIncome;

    /**
     * 当月预估收入
     */
    @ExcelProperty(value = "当月预估收入", index = 44)
    private BigDecimal currentMonthPlanIncome;

    /**
     * 服务性收入金额
     */
    @ExcelProperty(value = "服务性收入金额", index = 45)
    private String serviceInMoney;

    /**
     * 项目毛利率/效益比
     */
    @ExcelProperty(value = "项目毛利率/效益比", index = 46)
    private String grossProfitMargin;

    /**
     * 预计本年完成收入
     */
    @ExcelProperty(value = "预计本年完成收入", index = 47)
    private String expectIncome;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间", index = 48)
    private Date createTime;

    /**
     * 合同审核时间
     */
    @ExcelProperty(value = "合同审核时间", index = 49)
    private Date auditTime;
}
