package com.crm.system.domain.bo.topp;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.crm.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】业务对象 topp_opportunity
 *
 * @author crm
 * @date 2025-12-02
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ToppOpportunityBo extends BaseEntity {

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 客户ID
     */
    @NotNull(message = "客户ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long custId;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custName;

    /**
     * 15位客户编码
     */
    @NotBlank(message = "15位客户编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custNo;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custNeed;

    /**
     * 	客户联系人
     */
    @NotBlank(message = "	客户联系人	不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custContact;

    /**
     * 客户联系人电话
     */
    @NotBlank(message = "客户联系人电话不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custContactTel;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custCapitalSource;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long custManagerId;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custManagerName;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custManagerNo;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custManagerTel;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custManagerEmail;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String oppCode;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String oppName;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String oppSource;

    /**
     * 项目类型
     */
    @NotBlank(message = "项目类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String projectType;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String oppType;

    /**
     * 包含业务
     */
    @NotBlank(message = "包含业务不能为空", groups = { AddGroup.class, EditGroup.class })
    private String oppIncludeType;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String oppPhase;

    /**
     * 商机阶段变化时间
     */
    @NotNull(message = "商机阶段变化时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date oppPhaseTime;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long clueId;

    /**
     * 是否是大项目
     */
    @NotNull(message = "是否是大项目不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer isBigProduct;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer isNeedIndependent;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer isNeedProject;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer isNeedDesigner;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer isNeedProv;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer isNeedAdvance;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer isBidding;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long bidId;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer isRecPartner;

    /**
     * 是否需要外部资源
     */
    @NotNull(message = "是否需要外部资源不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer isNeedExternalResources;

    /**
     * 是否具有创新性
     */
    @NotNull(message = "是否具有创新性不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer isHaveInnovate;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal bandServiceEarning;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String beloneProductInfo;

    /**
     * 效益比
     */
    @NotNull(message = "效益比不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal benefitProportion;

    /**
     * 预期总收入
     */
    @NotNull(message = "预期总收入不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal expectCountMoney;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal expectAdvance;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String expectAdvanceDuration;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal expectContractMoney;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal expectCtEarning;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal expectMoneyRate;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal investMoney;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal lineServiceEarning;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal moServiceEarning;

    /**
     * IDC收入
     */
    @NotNull(message = "IDC收入不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal idcEarning;

    /**
     * ICT收入
     */
    @NotNull(message = "ICT收入不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal ictEarning;

    /**
     * 行业应用收入
     */
    @NotNull(message = "行业应用收入不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal industryAppEarning;

    /**
     * 组网收入
     */
    @NotNull(message = "组网收入不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal groupNetEarning;

    /**
     * 大数据收入
     */
    @NotNull(message = "大数据收入不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal bigDataEarning;

    /**
     * 云计算收入
     */
    @NotNull(message = "云计算收入不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal cloudComputEarning;

    /**
     * 物联网收入
     */
    @NotNull(message = "物联网收入不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal netThingEarning;

    /**
     * 集团短彩信
     */
    @NotNull(message = "集团短彩信不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal groupSmmsEarning;

    /**
     * 后向流量
     */
    @NotNull(message = "后向流量不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal backwardFlowEarning;

    /**
     * 呼叫中心
     */
    @NotNull(message = "呼叫中心不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal callCenterEarning;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal monthIncomeContribution;

    /**
     * 预估合同期（月）
     */
    @NotNull(message = "预估合同期（月）不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long contractMonth;

    /**
     * 隐私宝收入
     */
    @NotNull(message = "隐私宝收入不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal privacyTreasureEarning;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal otherServiceEarning;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String partnerInfo;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String signMainPart;

    /**
     * 合同期意向
     */
    @NotBlank(message = "合同期意向不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractIntention;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String unicomType;

    /**
     * 行业总监
     */
    @NotNull(message = "行业总监不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long unicomDirector;

    /**
     * 副行业总监
     */
    @NotNull(message = "副行业总监不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long unicomDeputyDirector;

    /**
     * 需要行业总监审批说明原因
     */
    @NotBlank(message = "需要行业总监审批说明原因不能为空", groups = { AddGroup.class, EditGroup.class })
    private String explainReason;

    /**
     * 商机审核任务ID
     */
    @NotNull(message = "商机审核任务ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long auditTaskId;

    /**
     * 商机状态
     */
    @NotBlank(message = "商机状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private String auditStatus;

    /**
     * 审核结果描述
     */
    @NotBlank(message = "审核结果描述不能为空", groups = { AddGroup.class, EditGroup.class })
    private String auditResult;

    /**
     * 审核时间
     */
    @NotNull(message = "审核时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date auditTime;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String supportPlanProvDescribe;

    /**
     * 支撑任务标号
     */
    @NotBlank(message = "支撑任务标号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String supportTaskCode;

    /**
     * 支撑方案描述
     */
    @NotBlank(message = "支撑方案描述不能为空", groups = { AddGroup.class, EditGroup.class })
    private String supportPlanDescribe;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer isDistribute;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long designer;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long designerOrg;

    /**
     * 确定支撑经理时间
     */
    @NotNull(message = "确定支撑经理时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date designerTime;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String designType;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer designScore;

    /**
     * 十分制评分
     */
    @NotNull(message = "十分制评分不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer score;

    /**
     * 商机终结类型
     */
    @NotBlank(message = "商机终结类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String endOppType;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String endOppTypeDetail;

    /**
     * 商机终结描述
     */
    @NotBlank(message = "商机终结描述不能为空", groups = { AddGroup.class, EditGroup.class })
    private String endOppTypeMemo;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long creator;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String memo;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String sfzt;

    /**
     * 转实施时间
     */
    @NotNull(message = "转实施时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date zssTime;

    /**
     * 合同id
     */
    @NotNull(message = "合同id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long contractId;

    /**
     * 白名单
     */
    @NotBlank(message = "白名单不能为空", groups = { AddGroup.class, EditGroup.class })
    private String whiteList;

    /**
     * 商机状态
     */
    @NotBlank(message = "商机状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private String oppoState;

    /**
     * 订阅用户
     */
    @NotBlank(message = "订阅用户不能为空", groups = { AddGroup.class, EditGroup.class })
    private String dingyueList;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long designerZid;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String unicomType2;

    /**
     * 创新头条状态记录
     */
    @NotBlank(message = "创新头条状态记录不能为空", groups = { AddGroup.class, EditGroup.class })
    private String syncStatus;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date syncTime;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String newOppPhase;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date newOppPhaseTime;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String isBxqj;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long bxqjProjectCount;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String isKhkj;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String isJfkj;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String levelType;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long levelAreaId;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long dabiaoCreator;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date dabiaoCreateTime;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date dabiaoUpdateTime;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String dabiaoMemo;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long oppAreaId;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long oppOrgId;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bgType;

    /**
     * çœåˆ†ä¸»è¦ä¸šåŠ¡ç±»åž‹
     */
    @NotBlank(message = "çœåˆ†ä¸»è¦ä¸šåŠ¡ç±»åž‹不能为空", groups = { AddGroup.class, EditGroup.class })
    private String sfoppoType;

    /**
     * æ˜¯å¦æœ‰æ•ˆ
     */
    @NotBlank(message = "æ˜¯å¦æœ‰æ•ˆ不能为空", groups = { AddGroup.class, EditGroup.class })
    private String isValid;

    /**
     * é¢„è®¡ç­¾çº¦æ—¶é—´
     */
    @NotNull(message = "é¢„è®¡ç­¾çº¦æ—¶é—´不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date signTime;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal clientInvestBudgetAmount;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal estimatTotalContratAmount;

    /**
     * ä¸»è¦äº§å“/è§£å†³æ–¹æ¡ˆç¼–ç å€¼
     */
    @NotBlank(message = "ä¸»è¦äº§å“/è§£å†³æ–¹æ¡ˆç¼–ç å€¼不能为空", groups = { AddGroup.class, EditGroup.class })
    private String productCode;

    /**
     * çœåˆ†ä¸»è¦äº§å“/è§£å†³æ–¹æ¡ˆåç§°
     */
    @NotBlank(message = "çœåˆ†ä¸»è¦äº§å“/è§£å†³æ–¹æ¡ˆåç§°不能为空", groups = { AddGroup.class, EditGroup.class })
    private String sfProductName;

    /**
     * å½’å±žæ€»éƒ¨è¡Œä¸š/å®¢æˆ·åˆ†ç±»ç¼–ç å€¼
     */
    @NotBlank(message = "å½’å±žæ€»éƒ¨è¡Œä¸š/å®¢æˆ·åˆ†ç±»ç¼–ç å€¼不能为空", groups = { AddGroup.class, EditGroup.class })
    private String headquartersCustomerTypeCode;

    /**
     * å…¶ä»–è¡Œä¸š/å®¢æˆ·åˆ†ç±»
     */
    @NotBlank(message = "å…¶ä»–è¡Œä¸š/å®¢æˆ·åˆ†ç±»不能为空", groups = { AddGroup.class, EditGroup.class })
    private String sfCustomerType;

    /**
     * æ–°å…´ict
     */
    @NotBlank(message = "æ–°å…´ict不能为空", groups = { AddGroup.class, EditGroup.class })
    private String newIct;

    /**
     * 营销单元
     */
    @NotBlank(message = "营销单元不能为空", groups = { AddGroup.class, EditGroup.class })
    private String markunit;

    /**
     * æ˜¯å¦æ˜¯ç®€å•å•†æœº 0å¦ 1æ˜¯
     */
    @NotNull(message = "æ˜¯å¦æ˜¯ç®€å•å•†æœº 0å¦ 1æ˜¯不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long isSimpleOpp;

    /**
     * 集团营销线索ID
     */
    @NotBlank(message = "集团营销线索ID 不能为空", groups = { AddGroup.class, EditGroup.class })
    private String groupClueKey;

    /**
     * 楼宇名称
     */
    @NotBlank(message = "楼宇名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String buildingName;

    /**
     * 楼宇编码
     */
    @NotBlank(message = "楼宇编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String buildingCode;

    /**
     * 集团客户拜访json参数
     */
    @NotBlank(message = "集团客户拜访json参数不能为空", groups = { AddGroup.class, EditGroup.class })
    private String visitJsonPamram;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String oppDataReform;

    /**
     * 是否五融,0-否，1-是
     */
    @NotNull(message = "是否五融,0-否，1-是不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer fiveMergeState;

    /**
     * 五融类型多选,逗号分割 融自研产品-mergeProduct,融数-mergeNumber，融云-mergeCloud,融安全-mergeSafe,融5G-merge5G
     */
    @NotBlank(message = "五融类型多选,逗号分割 融自研产品-mergeProduct,融数-mergeNumber，融云-mergeCloud,融安全-mergeSafe,融5G-merge5G不能为空", groups = { AddGroup.class, EditGroup.class })
    private String fiveMergeCode;

    /**
     * 五融是否涉及终端（终端类优先保障）,0-否，1-是
     */
    @NotNull(message = "五融是否涉及终端（终端类优先保障）,0-否，1-是不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer referTerminal;

    /**
     * 支撑阶段,0:未支撑;1:支撑关闭;2:售中交付;3:招投标/直接签约;4:售前支撑;5:待支撑
     */
    @NotBlank(message = "支撑阶段,0:未支撑;1:支撑关闭;2:售中交付;3:招投标/直接签约;4:售前支撑;5:待支撑不能为空", groups = { AddGroup.class, EditGroup.class })
    private String supportStage;

    /**
     * 支撑阶段变更时间
     */
    @NotNull(message = "支撑阶段变更时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date supportStageTime;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String preDecision;

    /**
     * 1-自动关闭、2-发起关闭、3-关联合同、4-关联电子表单、5-关联标讯、6-其他，该字段存在多个状态，当为多个值时用英文逗号分隔，例如：,1,2,3,4;
     */
    private String oppoConversionState;

}
