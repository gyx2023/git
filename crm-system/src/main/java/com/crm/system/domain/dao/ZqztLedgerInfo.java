package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 台账数据主表 zqzt_ledger_info
 */
@Data
@TableName("zqzt_ledger_info")
public class ZqztLedgerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 服务大厅工单号 */
    private String orderNo;

    /** 签约日期 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date signDate;

    /** 服务大厅工单创建时间 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date orderCreateTime;

    /** 地市ID(zqzt_ledger_area组织code) */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String cityId;

    /** 区县ID */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String districtId;

    /** 街道ID */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String streetId;

    /** 所属行业ID(1-13) */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String industryId;

    /** 企业名称 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String companyName;

    /** 社会统一信用代码 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String creditCode;

    /** 注册地址 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String regAddress;

    /** 实际地址 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String actualAddress;

    /** 签约路数 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer signRoutes;

    /** 特殊场景 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String specialScene;

    /** 企业负责人 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String companyPrincipal;

    /** 企业负责人联系方式 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String principalPhone;

    /** 安全负责人1 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String safetyOfficer1;

    /** 安全负责人1联系方式 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String safetyOfficer1Phone;

    /** 安全负责人2 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String safetyOfficer2;

    /** 安全负责人2联系方式 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String safetyOfficer2Phone;

    /** 企业施工联系人 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String constructContact;

    /** 企业施工联系人联系方式 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String constructContactPhone;

    /** 联通客户经理 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String unicomManager;

    /** 联通客户经理联系方式 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String unicomManagerPhone;

    /** 摄像头是否新装(1是/0否) */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String cameraIsNewInstall;

    /** 摄像头购买方式(个人购买/公司购买) */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String cameraPurchaseType;

    /** 摄像头安装完成时间 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date cameraInstallDate;

    /** 摄像头配置施工账号 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String cameraAccount;

    /** 摄像头账号竣工时间 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date cameraFinishDate;

    /** 宽带是否利旧(OLD-利旧/NEW-新装) */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String broadbandIsReuse;

    /** 宽带零星工程(1是/0否) */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String broadbandIsSporadic;

    /** 宽带零星工程计划完工时间 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date broadbandSporadicPlanDate;

    /** 宽带实际完成时间 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date broadbandSporadicActualDate;

    /** 宽带施工状态 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String broadbandConstructStatus;

    /** 宽带零星工程单号 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String broadbandSporadicOrderNo;

    /** 宽带账号 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String broadbandAccount;

    /** 宽带竣工时间 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date broadbandFinishDate;

    /** 云智眼账号 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String cloudAccount;

    /** 云智眼密码 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String cloudPassword;

    /** 云智眼机构ID */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String cloudOrgId;

    /** 云智眼接入秘钥 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String cloudAccessKey;

    /** 云智眼视频资源包ID */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String cloudVisionPackageId;

    /** 火焰卫士企业信息录入状态(1是/0否) */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String infoEntryStatus;

    /** 火焰卫士完成时间 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date fullProcessDate;

    /** 全流程状态(1已完成/0未完成) */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String status;

    /** 备注 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String remark;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 创建人员工号 */
    private String createdBy;

    /** 更新人员工号 */
    private String updatedBy;

    /** 同步火焰卫士状态(0-默认，1-待同步，2-同步中，3-同步成功，4-同步失败) */
    private String flameGuardianSyn;

    /** 是否有效(0失效/1有效) */
    private String isValid;
}
