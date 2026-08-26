package com.crm.system.domain.bo;

import com.crm.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 台账数据主表 业务对象 zqzt_ledger_info
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ZqztLedgerInfoBo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 服务大厅工单号 */
    private String orderNo;

    /** 签约日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date signDate;

    /** 服务大厅工单创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date orderCreateTime;

    private String cityId;
    private String districtId;
    private String streetId;
    private String industryId;
    private String companyName;
    private String creditCode;
    private String regAddress;
    private String actualAddress;
    private Integer signRoutes;
    private String specialScene;
    private String companyPrincipal;
    private String principalPhone;
    private String safetyOfficer1;
    private String safetyOfficer1Phone;
    private String safetyOfficer2;
    private String safetyOfficer2Phone;
    private String constructContact;
    private String constructContactPhone;
    private String unicomManager;
    private String unicomManagerPhone;

    /** 摄像头信息 */
    private String cameraIsNewInstall;
    private String cameraPurchaseType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date cameraInstallDate;
    private String cameraAccount;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date cameraFinishDate;

    /** 宽带信息 */
    private String broadbandIsReuse;
    private String broadbandIsSporadic;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date broadbandSporadicPlanDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date broadbandSporadicActualDate;
    private String broadbandConstructStatus;
    private String broadbandSporadicOrderNo;
    private String broadbandAccount;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date broadbandFinishDate;

    /** 云智眼账户 */
    private String cloudAccount;
    private String cloudPassword;
    private String cloudOrgId;
    private String cloudAccessKey;
    private String cloudVisionPackageId;

    /** 火焰卫士 */
    private String infoEntryStatus;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fullProcessDate;
    private String status;
    private String remark;
    private String isValid;

    /** 查询：签约日期范围(起止 yyyy-MM-dd) */
    private String signDateStart;
    private String signDateEnd;

    /** 宽带实际完成时间范围(起止 yyyy-MM-dd) */
    private String broadbandActualDateStart;
    private String broadbandActualDateEnd;
    /** 宽带实际完成时间为空（"未完成"复选框） */
    private Boolean broadbandActualDateEmpty;
    /** 设备ID（子表模糊匹配） */
    private String deviceId;
    /** 设备ID含空值 */
    private Boolean deviceIdEmpty;
    /** 机构ID含空值 */
    private Boolean cloudOrgIdEmpty;

    /** 导出上下文：登录人区域代码(sys_area.id)，由 applyExport 注入 */
    private String areaCode;

    /** 导出上下文：客户经理OA账号，由 applyExport 注入 */
    private String custManagerOauser;

    /** 导出上下文：是否管理员 */
    private Boolean isAdmin;

    /** 导出上下文：是否脱敏 */
    private Boolean isDesensitize;

    /** 云智眼配置信息（多组） */
    private List<ZqztLedgerCloudConfigBo> cloudVisionConfigs;
}
