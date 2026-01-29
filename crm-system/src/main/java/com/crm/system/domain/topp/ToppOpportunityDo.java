package com.crm.system.domain.topp;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.core.domain.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 【请填写功能名称】对象 topp_opportunity
 *
 * @author crm
 * @date 2025-12-02
 */
@Data
@TableName("topp_opportunity")
public class ToppOpportunityDo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId(value = "id_")
    private Long id;
    /**
     * 客户ID
     */
    private Long custId;
    /**
     *
     */
    private String custName;
    /**
     * 15位客户编码
     */
    private String custNo;
    /**
     *
     */
    private String custNeed;
    /**
     * 客户联系人
     */
    private String custContact;
    /**
     * 客户联系人电话
     */
    private String custContactTel;
    /**
     *
     */
    private String custCapitalSource;
    /**
     *
     */
    private Long custManagerId;
    /**
     *
     */
    private String custManagerName;
    /**
     *
     */
    private String custManagerNo;
    /**
     *
     */
    private String custManagerTel;
    /**
     *
     */
    private String custManagerEmail;
    /**
     *
     */
    private String oppCode;
    /**
     *
     */
    private String oppName;
    /**
     *
     */
    private String oppSource;
    /**
     * 项目类型
     */
    private String projectType;
    /**
     *
     */
    private String oppType;
    /**
     * 包含业务
     */
    private String oppIncludeType;
    /**
     *
     */
    private String oppPhase;
    /**
     * 商机阶段变化时间
     */
    private Date oppPhaseTime;
    /**
     *
     */
    private Long clueId;
    /**
     * 是否是大项目
     */
    private Integer isBigProduct;
    /**
     *
     */
    private Integer isNeedIndependent;
    /**
     *
     */
    private Integer isNeedProject;
    /**
     *
     */
    private Integer isNeedDesigner;
    /**
     *
     */
    private Integer isNeedProv;
    /**
     *
     */
    private Integer isNeedAdvance;
    /**
     *
     */
    private Integer isBidding;
    /**
     *
     */
    private Long bidId;
    /**
     *
     */
    @TableField("is_rec_partner_")
    private Integer isRecPartner;
    /**
     * 是否需要外部资源
     */
    private Integer isNeedExternalResources;
    /**
     * 是否具有创新性
     */
    private Integer isHaveInnovate;
    /**
     *
     */
    private BigDecimal bandServiceEarning;
    /**
     *
     */
    private String beloneProductInfo;
    /**
     * 效益比
     */
    private BigDecimal benefitProportion;
    /**
     * 预期总收入
     */
    private BigDecimal expectCountMoney;
    /**
     *
     */
    private BigDecimal expectAdvance;
    /**
     *
     */
    private String expectAdvanceDuration;
    /**
     *
     */
    private BigDecimal expectContractMoney;
    /**
     *
     */
    private BigDecimal expectCtEarning;
    /**
     *
     */
    private BigDecimal expectMoneyRate;
    /**
     *
     */
    private BigDecimal investMoney;
    /**
     *
     */
    private BigDecimal lineServiceEarning;
    /**
     *
     */
    private BigDecimal moServiceEarning;
    /**
     * IDC收入
     */
    private BigDecimal idcEarning;
    /**
     * ICT收入
     */
    private BigDecimal ictEarning;
    /**
     * 行业应用收入
     */
    private BigDecimal industryAppEarning;
    /**
     * 组网收入
     */
    private BigDecimal groupNetEarning;
    /**
     * 大数据收入
     */
    private BigDecimal bigDataEarning;
    /**
     * 云计算收入
     */
    private BigDecimal cloudComputEarning;
    /**
     * 物联网收入
     */
    private BigDecimal netThingEarning;
    /**
     * 集团短彩信
     */
    private BigDecimal groupSmmsEarning;
    /**
     * 后向流量
     */
    private BigDecimal backwardFlowEarning;
    /**
     * 呼叫中心
     */
    private BigDecimal callCenterEarning;
    /**
     *
     */
    private BigDecimal monthIncomeContribution;
    /**
     * 预估合同期（月）
     */
    private Long contractMonth;
    /**
     * 隐私宝收入
     */
    private BigDecimal privacyTreasureEarning;
    /**
     *
     */
    private BigDecimal otherServiceEarning;
    /**
     *
     */
    private String partnerInfo;
    /**
     *
     */
    private String signMainPart;
    /**
     * 合同期意向
     */
    private String contractIntention;
    /**
     *
     */
    private String unicomType;
    /**
     * 行业总监
     */
    private Long unicomDirector;
    /**
     * 副行业总监
     */
    private Long unicomDeputyDirector;
    /**
     * 需要行业总监审批说明原因
     */
    private String explainReason;
    /**
     * 商机审核任务ID
     */
    private Long auditTaskId;
    /**
     * 商机状态
     */
    private String auditStatus;
    /**
     * 审核结果描述
     */
    private String auditResult;
    /**
     * 审核时间
     */
    private Date auditTime;
    /**
     *
     */
    private String supportPlanProvDescribe;
    /**
     * 支撑任务标号
     */
    private String supportTaskCode;
    /**
     * 支撑方案描述
     */
    private String supportPlanDescribe;
    /**
     *
     */
    private Integer isDistribute;
    /**
     *
     */
    private Long designer;
    /**
     *
     */
    private Long designerOrg;
    /**
     * 确定支撑经理时间
     */
    private Date designerTime;
    /**
     *
     */
    private String designType;
    /**
     *
     */
    private Integer designScore;
    /**
     * 十分制评分
     */
    @TableField("score_")
    private Integer score;
    /**
     * 商机终结类型
     */
    private String endOppType;
    /**
     *
     */
    private String endOppTypeDetail;
    /**
     * 商机终结描述
     */
    private String endOppTypeMemo;

    /**
     *
     */
    private Long creator;
    /**
     *
     */
    @TableField("memo_")
    private String memo;
    /**
     *
     */
    @TableField("sfzt_")
    private String sfzt;
    /**
     * 转实施时间
     */
    private Date zssTime;
    /**
     * 合同id
     */
    private Long contractId;
    /**
     * 白名单
     */
    private String whiteList;
    /**
     * 商机状态
     */
    private String oppoState;
    /**
     * 订阅用户
     */
    private String dingyueList;
    /**
     *
     */
    private Long designerZid;
    /**
     *
     */
    private String unicomType2;
    /**
     * 创新头条状态记录
     */
    private String syncStatus;
    /**
     *
     */
    private Date syncTime;
    /**
     *
     */
    private String newOppPhase;
    /**
     *
     */
    private Date newOppPhaseTime;
    /**
     *
     */
    private String isBxqj;
    /**
     *
     */
    private Long bxqjProjectCount;
    /**
     *
     */
    private String isKhkj;
    /**
     *
     */
    private String isJfkj;
    /**
     *
     */
    private String levelType;
    /**
     *
     */
    private Long levelAreaId;
    /**
     *
     */
    private Long dabiaoCreator;
    /**
     *
     */
    private Date dabiaoCreateTime;
    /**
     *
     */
    private Date dabiaoUpdateTime;
    /**
     *
     */
    private String dabiaoMemo;
    /**
     *
     */
    private Long oppAreaId;
    /**
     *
     */
    private Long oppOrgId;
    /**
     *
     */
    private String bgType;
    /**
     * çœåˆ†ä¸»è¦ä¸šåŠ¡ç±»åž‹
     */
    private String sfoppoType;
    /**
     * æ˜¯å¦æœ‰æ•ˆ
     */
    private String isValid;
    /**
     * é¢„è®¡ç­¾çº¦æ—¶é—´
     */
    private Date signTime;
    /**
     *
     */
    private BigDecimal clientInvestBudgetAmount;
    /**
     *
     */
    private BigDecimal estimatTotalContratAmount;
    /**
     * ä¸»è¦äº§å“/è§£å†³æ–¹æ¡ˆç¼–ç å€¼
     */
    private String productCode;
    /**
     * çœåˆ†ä¸»è¦äº§å“/è§£å†³æ–¹æ¡ˆåç§°
     */
    private String sfProductName;
    /**
     * å½’å±žæ€»éƒ¨è¡Œä¸š/å®¢æˆ·åˆ†ç±»ç¼–ç å€¼
     */
    private String headquartersCustomerTypeCode;
    /**
     * å…¶ä»–è¡Œä¸š/å®¢æˆ·åˆ†ç±»
     */
    private String sfCustomerType;
    /**
     * æ–°å…´ict
     */
    private String newIct;
    /**
     * 营销单元
     */
    private String markunit;
    /**
     * æ˜¯å¦æ˜¯ç®€å•å•†æœº 0å¦ 1æ˜¯
     */
    private Long isSimpleOpp;
    /**
     * 集团营销线索ID
     */
    private String groupClueKey;
    /**
     * 楼宇名称
     */
    private String buildingName;
    /**
     * 楼宇编码
     */
    private String buildingCode;
    /**
     * 集团客户拜访json参数
     */
    private String visitJsonPamram;
    /**
     *
     */
    private String oppDataReform;
    /**
     * 是否五融,0-否，1-是
     */
    private Integer fiveMergeState;
    /**
     * 五融类型多选,逗号分割 融自研产品-mergeProduct,融数-mergeNumber，融云-mergeCloud,融安全-mergeSafe,融5G-merge5G
     */
    private String fiveMergeCode;
    /**
     * 五融是否涉及终端（终端类优先保障）,0-否，1-是
     */
    private Integer referTerminal;
    /**
     * 支撑阶段,0:未支撑;1:支撑关闭;2:售中交付;3:招投标/直接签约;4:售前支撑;5:待支撑
     */
    private String supportStage;
    /**
     * 支撑阶段变更时间
     */
    private Date supportStageTime;
    /**
     *
     */
    private String preDecision;

    /**
     * 访问次数
     */
    private Integer visitCnt;

    /**
     * 最后访问时间
     */
    private Date lastVisitTime;

    /**
     * 创建者
     */
    @TableField(exist = false)
    private String createBy;

    /**
     * 更新者
     */
    @TableField(exist = false)
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(exist = false)
    private Date updateTime;

    /**
     * 1-自动关闭、2-发起关闭、3-关联合同、4-关联电子表单、5-关联标讯、6-其他，该字段存在多个状态，当为多个值时用英文逗号分隔，例如：,1,2,3,4;
     */
    private String oppoConversionState;
}
