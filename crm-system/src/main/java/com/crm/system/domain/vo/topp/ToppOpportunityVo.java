package com.crm.system.domain.vo.topp;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.utils.convert.ExcelDictConvert;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


/**
 * 【请填写功能名称】视图对象 topp_opportunity
 *
 * @author crm
 * @date 2025-12-02
 */
@Data
@ExcelIgnoreUnannotated
public class ToppOpportunityVo {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long id;

    /**
     * 客户ID
     */
    @ExcelProperty(value = "客户ID")
    private Long custId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String custName;

    /**
     * 15位客户编码
     */
    @ExcelProperty(value = "15位客户编码")
    private String custNo;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String custNeed;

    /**
     * 客户联系人
     */
    @ExcelProperty(value = "	客户联系人	")
    private String custContact;

    /**
     * 客户联系人电话
     */
    @ExcelProperty(value = "客户联系人电话")
    private String custContactTel;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String custCapitalSource;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long custManagerId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String custManagerName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String custManagerNo;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String custManagerTel;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String custManagerEmail;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String oppCode;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String oppName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String oppSource;

    /**
     * 项目类型
     */
    @ExcelProperty(value = "项目类型")
    private String projectType;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String oppType;

    /**
     * 包含业务
     */
    @ExcelProperty(value = "包含业务")
    private String oppIncludeType;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String oppPhase;

    /**
     * 商机阶段变化时间
     */
    @ExcelProperty(value = "商机阶段变化时间")
    private Date oppPhaseTime;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long clueId;

    /**
     * 是否是大项目
     */
    @ExcelProperty(value = "是否是大项目")
    private Integer isBigProduct;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Integer isNeedIndependent;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Integer isNeedProject;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Integer isNeedDesigner;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Integer isNeedProv;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Integer isNeedAdvance;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Integer isBidding;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long bidId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Integer isRecPartner;

    /**
     * 是否需要外部资源
     */
    @ExcelProperty(value = "是否需要外部资源")
    private Integer isNeedExternalResources;

    /**
     * 是否具有创新性
     */
    @ExcelProperty(value = "是否具有创新性")
    private Integer isHaveInnovate;

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal bandServiceEarning;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String beloneProductInfo;

    /**
     * 效益比
     */
    @ExcelProperty(value = "效益比")
    private BigDecimal benefitProportion;

    /**
     * 预期总收入
     */
    @ExcelProperty(value = "预期总收入")
    private BigDecimal expectCountMoney;

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal expectAdvance;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String expectAdvanceDuration;

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal expectContractMoney;

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal expectCtEarning;

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal expectMoneyRate;

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal investMoney;

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal lineServiceEarning;

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal moServiceEarning;

    /**
     * IDC收入
     */
    @ExcelProperty(value = "IDC收入")
    private BigDecimal idcEarning;

    /**
     * ICT收入
     */
    @ExcelProperty(value = "ICT收入")
    private BigDecimal ictEarning;

    /**
     * 行业应用收入
     */
    @ExcelProperty(value = "行业应用收入")
    private BigDecimal industryAppEarning;

    /**
     * 组网收入
     */
    @ExcelProperty(value = "组网收入")
    private BigDecimal groupNetEarning;

    /**
     * 大数据收入
     */
    @ExcelProperty(value = "大数据收入")
    private BigDecimal bigDataEarning;

    /**
     * 云计算收入
     */
    @ExcelProperty(value = "云计算收入")
    private BigDecimal cloudComputEarning;

    /**
     * 物联网收入
     */
    @ExcelProperty(value = "物联网收入")
    private BigDecimal netThingEarning;

    /**
     * 集团短彩信
     */
    @ExcelProperty(value = "集团短彩信")
    private BigDecimal groupSmmsEarning;

    /**
     * 后向流量
     */
    @ExcelProperty(value = "后向流量")
    private BigDecimal backwardFlowEarning;

    /**
     * 呼叫中心
     */
    @ExcelProperty(value = "呼叫中心")
    private BigDecimal callCenterEarning;

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal monthIncomeContribution;

    /**
     * 预估合同期（月）
     */
    @ExcelProperty(value = "预估合同期", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "月=")
    private Long contractMonth;

    /**
     * 隐私宝收入
     */
    @ExcelProperty(value = "隐私宝收入")
    private BigDecimal privacyTreasureEarning;

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal otherServiceEarning;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String partnerInfo;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String signMainPart;

    /**
     * 合同期意向
     */
    @ExcelProperty(value = "合同期意向")
    private String contractIntention;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String unicomType;

    /**
     * 行业总监
     */
    @ExcelProperty(value = "行业总监")
    private Long unicomDirector;

    /**
     * 副行业总监
     */
    @ExcelProperty(value = "副行业总监")
    private Long unicomDeputyDirector;

    /**
     * 需要行业总监审批说明原因
     */
    @ExcelProperty(value = "需要行业总监审批说明原因")
    private String explainReason;

    /**
     * 商机审核任务ID
     */
    @ExcelProperty(value = "商机审核任务ID")
    private Long auditTaskId;

    /**
     * 商机状态
     */
    @ExcelProperty(value = "商机状态")
    private String auditStatus;

    /**
     * 审核结果描述
     */
    @ExcelProperty(value = "审核结果描述")
    private String auditResult;

    /**
     * 审核时间
     */
    @ExcelProperty(value = "审核时间")
    private Date auditTime;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String supportPlanProvDescribe;

    /**
     * 支撑任务标号
     */
    @ExcelProperty(value = "支撑任务标号")
    private String supportTaskCode;

    /**
     * 支撑方案描述
     */
    @ExcelProperty(value = "支撑方案描述")
    private String supportPlanDescribe;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Integer isDistribute;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long designer;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long designerOrg;

    /**
     * 确定支撑经理时间
     */
    @ExcelProperty(value = "确定支撑经理时间")
    private Date designerTime;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String designType;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Integer designScore;

    /**
     * 十分制评分
     */
    @ExcelProperty(value = "十分制评分")
    private Integer score;

    /**
     * 商机终结类型
     */
    @ExcelProperty(value = "商机终结类型")
    private String endOppType;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String endOppTypeDetail;

    /**
     * 商机终结描述
     */
    @ExcelProperty(value = "商机终结描述")
    private String endOppTypeMemo;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long creator;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String memo;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String sfzt;

    /**
     * 转实施时间
     */
    @ExcelProperty(value = "转实施时间")
    private Date zssTime;

    /**
     * 合同id
     */
    @ExcelProperty(value = "合同id")
    private Long contractId;

    /**
     * 白名单
     */
    @ExcelProperty(value = "白名单")
    private String whiteList;

    /**
     * 商机状态
     */
    @ExcelProperty(value = "商机状态")
    private String oppoState;

    /**
     * 订阅用户
     */
    @ExcelProperty(value = "订阅用户")
    private String dingyueList;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long designerZid;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String unicomType2;

    /**
     * 创新头条状态记录
     */
    @ExcelProperty(value = "创新头条状态记录")
    private String syncStatus;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date syncTime;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String newOppPhase;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date newOppPhaseTime;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String isBxqj;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long bxqjProjectCount;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String isKhkj;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String isJfkj;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String levelType;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long levelAreaId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long dabiaoCreator;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date dabiaoCreateTime;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date dabiaoUpdateTime;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String dabiaoMemo;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long oppAreaId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long oppOrgId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String bgType;

    /**
     * çœåˆ†ä¸»è¦ä¸šåŠ¡ç±»åž‹
     */
    @ExcelProperty(value = "çœåˆ†ä¸»è¦ä¸šåŠ¡ç±»åž‹")
    private String sfoppoType;

    /**
     * æ˜¯å¦æœ‰æ•ˆ
     */
    @ExcelProperty(value = "æ˜¯å¦æœ‰æ•ˆ")
    private String isValid;

    /**
     * é¢„è®¡ç­¾çº¦æ—¶é—´
     */
    @ExcelProperty(value = "é¢„è®¡ç­¾çº¦æ—¶é—´")
    private Date signTime;

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal clientInvestBudgetAmount;

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal estimatTotalContratAmount;

    /**
     * ä¸»è¦äº§å“/è§£å†³æ–¹æ¡ˆç¼–ç å€¼
     */
    @ExcelProperty(value = "ä¸»è¦äº§å“/è§£å†³æ–¹æ¡ˆç¼–ç å€¼")
    private String productCode;

    /**
     * çœåˆ†ä¸»è¦äº§å“/è§£å†³æ–¹æ¡ˆåç§°
     */
    @ExcelProperty(value = "çœåˆ†ä¸»è¦äº§å“/è§£å†³æ–¹æ¡ˆåç§°")
    private String sfProductName;

    /**
     * å½’å±žæ€»éƒ¨è¡Œä¸š/å®¢æˆ·åˆ†ç±»ç¼–ç å€¼
     */
    @ExcelProperty(value = "å½’å±žæ€»éƒ¨è¡Œä¸š/å®¢æˆ·åˆ†ç±»ç¼–ç å€¼")
    private String headquartersCustomerTypeCode;

    /**
     * å…¶ä»–è¡Œä¸š/å®¢æˆ·åˆ†ç±»
     */
    @ExcelProperty(value = "å…¶ä»–è¡Œä¸š/å®¢æˆ·åˆ†ç±»")
    private String sfCustomerType;

    /**
     * æ–°å…´ict
     */
    @ExcelProperty(value = "æ–°å…´ict")
    private String newIct;

    /**
     * 营销单元
     */
    @ExcelProperty(value = "营销单元")
    private String markunit;

    /**
     * æ˜¯å¦æ˜¯ç®€å•å•†æœº 0å¦ 1æ˜¯
     */
    @ExcelProperty(value = "æ˜¯å¦æ˜¯ç®€å•å•†æœº 0å¦ 1æ˜¯")
    private Long isSimpleOpp;

    /**
     * 集团营销线索ID
     */
    @ExcelProperty(value = "集团营销线索ID ")
    private String groupClueKey;

    /**
     * 楼宇名称
     */
    @ExcelProperty(value = "楼宇名称")
    private String buildingName;

    /**
     * 楼宇编码
     */
    @ExcelProperty(value = "楼宇编码")
    private String buildingCode;

    /**
     * 集团客户拜访json参数
     */
    @ExcelProperty(value = "集团客户拜访json参数")
    private String visitJsonPamram;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String oppDataReform;

    /**
     * 是否五融,0-否，1-是
     */
    @ExcelProperty(value = "是否五融,0-否，1-是")
    private Integer fiveMergeState;

    /**
     * 五融类型多选,逗号分割 融自研产品-mergeProduct,融数-mergeNumber，融云-mergeCloud,融安全-mergeSafe,融5G-merge5G
     */
    @ExcelProperty(value = "五融类型多选,逗号分割 融自研产品-mergeProduct,融数-mergeNumber，融云-mergeCloud,融安全-mergeSafe,融5G-merge5G")
    private String fiveMergeCode;

    /**
     * 五融是否涉及终端（终端类优先保障）,0-否，1-是
     */
    @ExcelProperty(value = "五融是否涉及终端", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "终=端类优先保障")
    private Integer referTerminal;

    /**
     * 支撑阶段,0:未支撑;1:支撑关闭;2:售中交付;3:招投标/直接签约;4:售前支撑;5:待支撑
     */
    @ExcelProperty(value = "支撑阶段,0:未支撑;1:支撑关闭;2:售中交付;3:招投标/直接签约;4:售前支撑;5:待支撑")
    private String supportStage;

    /**
     * 支撑阶段变更时间
     */
    @ExcelProperty(value = "支撑阶段变更时间")
    private Date supportStageTime;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String preDecision;

    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 1-自动关闭、2-发起关闭、3-关联合同、4-关联电子表单、5-关联标讯、6-其他，该字段存在多个状态，当为多个值时用英文逗号分隔，例如：,1,2,3,4;
     */
    @ExcelProperty(value = "商机转化状态")
    private String oppoConversionState;

    private String finalEndOppTypeMemo;
}
