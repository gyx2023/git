//package com.crm.system.domain.zbzq;
//
//import com.baomidou.mybatisplus.annotation.TableName;
//import lombok.Data;
//
//import java.io.Serializable;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
///**
// * VIEW对象 salesup_coll_oppo_sign
// *
// * @author crm
// * @date 2025-12-16
// */
//@Data
//@TableName("salesup_bak_tbl_bid_task")
//public class SalesupBakTblBidTaskDo implements Serializable {
//    private static final long serialVersionUID=1L;
//
//    private Long id;
//
//    /**
//     * 任务名称
//     */
//    private String name;
//
//    /**
//     * 关联信息ID
//     */
//    private String infoId;
//
//    /**
//     * 客户ID
//     */
//    private String custId;
//
//    /**
//     * 客户名称
//     */
//    private String customerName;
//
//    /**
//     * 采购人
//     */
//    private String purchaser;
//
//    /**
//     * 项目地址（旧字段，建议优先使用projectAddress）
//     */
//    private String projectAddr;
//
//    /**
//     * 联系人（支持多联系人，长度2048）
//     */
//    private String contactPerson;
//
//    /**
//     * 联系电话
//     */
//    private String contactPhone;
//
//    /**
//     * 采购需求（大文本）
//     */
//    private String purchaseReq;
//
//    /**
//     * 省份名称
//     */
//    private String provName;
//
//    /**
//     * 省份编码
//     */
//    private String prov;
//
//    /**
//     * 城市名称
//     */
//    private String cityName;
//
//    /**
//     * 城市编码
//     */
//    private String city;
//
//    /**
//     * 区县名称
//     */
//    private String countyName;
//
//    /**
//     * 区县编码
//     */
//    private String county;
//
//    /**
//     * 预算金额（精度24位，小数2位）
//     */
//    private BigDecimal budgetAmount;
//
//    /**
//     * 发布日期
//     */
//    private LocalDate releaseDate;
//
//    /**
//     * 标记方式（1位字符，如0/1标识）
//     */
//    private String markingMode;
//
//    /**
//     * 任务发布方式（1位字符）
//     */
//    private String taskReleaseMode;
//
//    /**
//     * 中标方
//     */
//    private String winbidder;
//
//    /**
//     * 投标金额
//     */
//    private BigDecimal bidAmount;
//
//    /**
//     * 业务IDs（多个ID拼接）
//     */
//    private String businessIds;
//
//    /**
//     * 业务名称
//     */
//    private String businessName;
//
//    /**
//     * 行业ID
//     */
//    private String industryId;
//
//    /**
//     * 行业名称
//     */
//    private String industryName;
//
//    /**
//     * 行业一级分类ID
//     */
//    private String industryG1Id;
//
//    /**
//     * 行业二级分类ID
//     */
//    private String industryG2Id;
//
//    /**
//     * 总部行业编码
//     */
//    private String headquartersIndustryCode;
//
//    /**
//     * 任务发布时间
//     */
//    private LocalDateTime taskReleaseTime;
//
//    /**
//     * 投标开始时间
//     */
//    private LocalDateTime bidStartTime;
//
//    /**
//     * 投标结束时间
//     */
//    private LocalDateTime bidEndTime;
//
//    /**
//     * 推送时间
//     */
//    private LocalDateTime pushTime;
//
//    /**
//     * 投标状态（长度10，如"未投标"/"已中标"等）
//     */
//    private String bidStatus;
//
//    /**
//     * 发布状态（1位字符，0/1标识）
//     */
//    private String releaseStatus;
//
//    /**
//     * 客户经理（多个拼接）
//     */
//    private String accountManagers;
//
//    /**
//     * 优质投标标识（1位字符）
//     */
//    private String goodBid;
//
//    /**
//     * 创建人ID
//     */
//    private String creator;
//
//    /**
//     * 创建时间
//     */
//    private LocalDateTime createTime;
//
//    /**
//     * 更新人ID
//     */
//    private String updater;
//
//    /**
//     * 更新时间
//     */
//    private LocalDateTime updateTime;
//
//    /**
//     * 任务状态（长度20）
//     */
//    private String taskStatus;
//
//    /**
//     * 名册ID（长度2048，多ID拼接）
//     */
//    private String rosterId;
//
//    /**
//     * 项目地址（新字段）
//     */
//    private String projectAddress;
//
//    /**
//     * 项目名称
//     */
//    private String projectName;
//
//    /**
//     * 级别
//     */
//    private String levels;
//
//    /**
//     * 是否核心客户
//     */
//    private String whetherCoreCustomer;
//
//    /**
//     * 名册客户名称（长度2048）
//     */
//    private String rosterCustomerName;
//
//    /**
//     * 其他共享标识
//     */
//    private String sharedOther;
//
//    /**
//     * 全资子公司标识
//     */
//    private String whollyOwnedSubsidiary;
//
//    /**
//     * 行业二级分类名称
//     */
//    private String industryG2;
//
//    /**
//     * 总部行业名称
//     */
//    private String headquartersIndustryName;
//
//    /**
//     * 来源链接（大文本）
//     */
//    private String sourceLink;
//
//    /**
//     * 行业所属省份ID
//     */
//    private String industryProvId;
//
//    /**
//     * 行业所属省份名称
//     */
//    private String industryProvName;
//
//    /**
//     * 城市编码（新）
//     */
//    private String cityCode;
//
//    /**
//     * 区县编码（新）
//     */
//    private String countyCode;
//
//    /**
//     * 弃标原因编码
//     */
//    private String abandonReason;
//
//    /**
//     * 弃标备注
//     */
//    private String abandonRemark;
//
//    /**
//     * 评分（精度10位，小数2位）
//     */
//    private BigDecimal score;
//
//
//}
