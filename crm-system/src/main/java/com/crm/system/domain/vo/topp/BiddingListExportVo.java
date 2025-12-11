package com.crm.system.domain.vo.topp;

import com.crm.common.annotation.ExcelDictFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.alibaba.excel.annotation.ExcelProperty;

@Data
@NoArgsConstructor
public class BiddingListExportVo {
        /**
         * 项目名称
         */
        @ExcelProperty("项目名称")
        private String projectName;
        /**
         * 采购人
         */
        @ExcelProperty("采购人")
        private String buyUserName;
        /**
         * 采购预算
         */
        @ExcelProperty("采购预算")
        private String buyBudgetMoney;
        /**
         * 开标时间
         */
        @ExcelProperty("开标时间")
        private String openBidTime;
        /**
         * 客户行业
         */
        @ExcelProperty("客户行业")
        @ExcelDictFormat(dictType = "unicom_type")
        private String unicomType;
        /**
         * 竞争情况
         */
        @ExcelProperty("竞争情况")
        @ExcelDictFormat(dictType = "competition_condition")
        private String competitionCondition;
        /**
         * 投标结果
         */
        @ExcelProperty("投标结果")
        @ExcelDictFormat(dictType = "bid_result")
        private String bidResult;
        /**
         * 投标主体
         */
        @ExcelProperty("投标主体")
        @ExcelDictFormat(dictType = "bid_body")
        private String bidBody;
        /**
         * 项目类型
         */
        @ExcelProperty("项目类型")
        @ExcelDictFormat(dictType = "project_type")
        private String projectType;
        /**
         * 预计签约金额（万元）
         */
        @ExcelProperty("预计签约金额（万元）")
        private String signMoney;
        /**
         * 预计主营金额（万元）
         */
        @ExcelProperty("预计主营金额（万元）")
        private String expectedMainOperatingAmount;
        /**
         * 毛利率
         */
        @ExcelProperty("毛利率")
        private String ictProfit;
        /**
         * 是否框架合同
         */
        @ExcelProperty("是否框架合同")
        @ExcelDictFormat(dictType = "is_framework_contract")
        private String isFrameworkContract;
        /**
         * 合同期
         */
        @ExcelProperty("合同期")
        private String contractTime;
        /**
         * 商机编码
         */
        @ExcelProperty("商机编码")
        private String oppCode;
        /**
         * 商机名称
         */
        @ExcelProperty("商机名称")
        private String oppName;
        /**
         * 商机归属部门
         */
        @ExcelProperty("商机归属部门")
        private String oppDeptName;
        /**
         * 客户经理
         */
        @ExcelProperty("客户经理")
        private String oppManager;
        /**
         * 主责支撑归属部门
         */
        @ExcelProperty("主责支撑归属部门")
        private String mainSupportDeptName;
        /**
         * 支撑角色
         */
        @ExcelProperty("支撑角色")
        private String supportRole;
        /**
         * 支撑经理
         */
        @ExcelProperty("支撑经理")
        private String supportManager;
        /**
         * 支撑团队
         */
        @ExcelProperty("支撑团队")
        private String supportTeam;
        /**
         * 项目编号（数智业务管理系统，例：C9M24C02WT1086，非商机编号）
         */
        @ExcelProperty("项目编号")
        private String projectCode;
        /**
         * 投入工日（取商机上所有支撑工单的工日）
         */
        @ExcelProperty("投入工日")
        private String workDays;
        /**
         * 个人贡献度占比
         */
        @ExcelProperty("个人贡献度占比")
        private String contribution;
        /**
         * 个人贡献度金额（万元）
         */
        @ExcelProperty("个人贡献度金额（万元）")
        private String personalContributionAmount;
        /**
         * 贡献度情况
         */
        @ExcelProperty("贡献度情况")
        private String contributionSituation;

}
