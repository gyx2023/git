package com.crm.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 台账数据主表 视图对象
 * 含区域名称回显与子表列表；脱敏由 Service 在返回前处理（同一 Vo 复用，修改回显不脱敏）
 */
@Data
public class ZqztLedgerInfoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNo;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date signDate;
    /** 服务大厅工单创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
    private String cameraIsNewInstall;
    private String cameraPurchaseType;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date cameraInstallDate;
    private String cameraAccount;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date cameraFinishDate;
    private String broadbandIsReuse;
    private String broadbandIsSporadic;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date broadbandSporadicPlanDate;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date broadbandSporadicActualDate;
    private String broadbandConstructStatus;
    private String broadbandSporadicOrderNo;
    private String broadbandAccount;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date broadbandFinishDate;
    private String cloudAccount;
    private String cloudPassword;
    private String cloudOrgId;
    private String cloudAccessKey;
    private String cloudVisionPackageId;
    private String infoEntryStatus;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date fullProcessDate;
    private String status;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    private String createdBy;
    private String updatedBy;
    private String isValid;

    /** 区域名称回显 */
    private String cityName;
    private String districtName;
    private String streetName;
    private String industryName;

    /** 云智眼配置信息（多组） */
    private List<ZqztLedgerCloudConfigVo> cloudVisionConfigs;
}
