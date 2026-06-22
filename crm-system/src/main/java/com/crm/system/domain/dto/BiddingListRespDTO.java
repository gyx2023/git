package com.crm.system.domain.dto;

import lombok.Data;


/**
 * 投标列表响应数据传输对象
 */
@Data
public class BiddingListRespDTO {
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 采购人
     */
    private String buyUserName;
    /**
     * 采购预算
     */
    private Double buyBudgetMoney;
    /**
     * 开标时间
     */
    private String openBidTime;
    /**
     * 客户行业
     */
    private String unicomType;
    /**
     * 竞争情况
     */
    private String competitionCondition;
    /**
     * 投标结果
     */
    private String bidResult;
    /**
     * 投标主体
     */
    private String bidBody;
    /**
     * 项目类型
     */
    private String projectType;
    /**
     * 预计签约金额（万元）
     */
    private Double matchBidMoney;
    /**
     * 预计主营金额（万元）
     */
    private Double expectedMainOperatingAmount;
    /**
     * 毛利率
     */
    private String ictProfit;
    /**
     * 是否框架合同
     */
    private String isFrameworkContract;
    /**
     * 合同期
     */
    private String contractTime;
    /**
     * 商机编码
     */
    private String oppCode;
    /**
     * 商机名称
     */
    private String oppName;
    /**
     * 商机归属部门
     */
    private String oppDeptName;
    /**
     * 客户经理
     */
    private String oppManager;
    /**
     * 主责支撑归属部门
     */
    private String mainSupportDeptName;
    /**
     * 支撑角色
     */
    private String supportRole;
    /**
     * 支撑经理
     */
    private String supportManager;
    /**
     * 支撑团队
     */
    private String supportTeam;
    /**
     * 项目编号（数智业务管理系统，例：C9M24C02WT1086，非商机编号）
     */
    private String projectCode;
    /**
     * 投入工日（取商机上所有支撑工单的工日）
     */
    private String workDays;
    /**
     * 个人贡献度占比
     */
    private String contribution;
    /**
     * 个人贡献度金额（万元）
     */
    private Double personalContributionAmount;
    /**
     * 贡献度情况
     */
    private String contributionSituation;

}
