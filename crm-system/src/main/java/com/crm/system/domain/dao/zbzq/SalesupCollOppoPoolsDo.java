package com.crm.system.domain.dao.zbzq;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.crm.common.core.domain.BaseEntity;

/**
 * VIEW对象 salesup_coll_oppo_pools
 *
 * @author crm
 * @date 2025-12-16
 */
@Data
@TableName("salesup_coll_oppo_pools")
public class SalesupCollOppoPoolsDo {

    private static final long serialVersionUID=1L;

    /**
     *
     */
    private Long id;
    /**
     *
     */
    private Long oppoId;
    /**
     *
     */
    private Long oppoOrderId;
    /**
     *
     */
    private Long creator;
    /**
     *
     */
    private String creatorLogin;
    /**
     *
     */
    private String oppoNumber;
    /**
     *
     */
    private String oppoName;
    /**
     *
     */
    private String userName;
    /**
     *
     */
    private String prov;
    /**
     *
     */
    private String oppoCustname;
    /**
     *
     */
    private Long org3Id;
    /**
     *
     */
    private String org3Name;
    /**
     *
     */
    private Long org2Id;
    /**
     *
     */
    private String org2Name;
    /**
     *
     */
    private Long orgId;
    /**
     *
     */
    private String orgName;
    /**
     *
     */
    private Date oppoCreatetime;
    /**
     *
     */
    private String oppoState;
    /**
     *
     */
    private String colorBrand;
    /**
     *
     */
    private String currentStepCode;
    /**
     *
     */
    private String currentStep;
    /**
     *
     */
    private Date currentStepArriveTime;
    /**
     *
     */
    private String oppoIndustry;
    /**
     *
     */
    private String oppoClue;
    /**
     *
     */
    private String busiType;
    /**
     *
     */
    private String taskState;
    /**
     *
     */
    private String processCode;
    /**
     *
     */
    private String processName;
    /**
     *
     */
    private String sourceName;
    /**
     *
     */
    private String busiTypeName;
    /**
     *
     */
    private String primaryBusiTypeCode;
    /**
     *
     */
    private String primaryBusiTypeName;
    /**
     *
     */
    private String industryName;
    /**
     *
     */
    private String creatorName;
    /**
     *
     */
    private String creatorOrgName;
    /**
     *
     */
    private String creatorOrg2Name;
    /**
     *
     */
    private String creatorOrg3Name;
    /**
     *
     */
    private String clientInvestBudgetAmount;
    /**
     *
     */
    private BigDecimal estimatTotalContratAmount;
    /**
     *
     */
    private BigDecimal estimatedIncome;
    /**
     *
     */
    private Date signTime;
    /**
     *
     */
    private String sfProductName;
    /**
     *
     */
    private String isCustom;
    /**
     *
     */
    private String custName;
    /**
     *
     */
    private String custNumber;
    /**
     *
     */
    private String industry;
    /**
     *
     */
    private String industry2;
    /**
     *
     */
    private String industry2Name;
    /**
     *
     */
    private String industry3;
    /**
     *
     */
    private String industry3Name;
    /**
     *
     */
    private String custContact;
    /**
     *
     */
    private String contactTelephone;
    /**
     *
     */
    private String nationalCust;
    /**
     *
     */
    private String custReq;
    /**
     *
     */
    private String suppEva;
    /**
     *
     */
    private String middleEva;
    /**
     *
     */
    private String signDate;
    /**
     *
     */
    private String contractId;
    /**
     *
     */
    private String signName;
    /**
     *
     */
    private String contractIncome;
    /**
     *
     */
    private String effectDate;
    /**
     *
     */
    private String endDate;
    /**
     *
     */
    private String contactExtend;
    /**
     *
     */
    private String lanuchState;
    /**
     *
     */
    private String userId;
    /**
     *
     */
    private String userLogin;
    /**
     *
     */
    private String headquartersIndustry;
    /**
     *
     */
    private String demandExpressDegree;
    /**
     *
     */
    private String jobSatisfaction;
    /**
     *
     */
    private String biddingResults;
    /**
     *
     */
    private String bidTime;
    /**
     *
     */
    private String supptName;
    /**
     *
     */
    private String supptOrgId;
    /**
     *
     */
    private String supptOrg2Id;
    /**
     *
     */
    private Integer contributionIsSaved;
    /**
     *
     */
    private Integer contribution;
    /**
     *
     */
    private Date demandResponseTime;
    /**
     *
     */
    private Date providedFirstPlanTime;
    /**
     *
     */
    private Date planExchangeTime;
    /**
     *
     */
    private String suppServiceTimeSatisDegree;
    /**
     *
     */
    private String masterBusiType;
    /**
     *
     */
    private Date oppoSaveTime;
    /**
     *
     */
    private String isImmatureOppo;
    /**
     *
     */
    private Long oneLevelIndustryId;
    /**
     *
     */
    private Long twoLevelIndustryId;
    /**
     *
     */
    private Long tradeClusterId;
    /**
     *
     */
    private String oneLevelIndustryName;
    /**
     *
     */
    private String twoLevelIndustryName;
    /**
     *
     */
    private String tradeClusterName;
    /**
     *
     */
    private Long provinceIndustryId;
    /**
     *
     */
    private String provinceIndustryName;
    /**
     *
     */
    private String natureCustId;
    /**
     *
     */
    private Integer isClearBudget;
    /**
     *
     */
    private String maturationLevel;
    /**
     *
     */
    private String importantLevel;
    /**
     *
     */
    private String natureCustName;
    /**
     *
     */
    private String leaderAttention;
    /**
     *
     */
    private Integer fiveMergeState;
    /**
     *
     */
    private String fiveMergeCode;
    /**
     *
     */
    private String attributionOrganization;
    /**
     *
     */
    private String systemSource;
    /**
     *
     */
    private String referTerminal;

}
