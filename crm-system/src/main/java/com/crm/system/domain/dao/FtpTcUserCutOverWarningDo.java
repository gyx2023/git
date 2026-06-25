package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户级割接预警 ftp_tc_user_cut_over_warning
 *
 * @author crm
 */
@Data
@TableName("ftp_tc_user_cut_over_warning")
public class FtpTcUserCutOverWarningDo implements Serializable {

    @TableId(value = "id_", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "CUTOVER_ID")
    private String cutoverId;

    @TableField(value = "CUTOVER_NO")
    private String cutoverNo;

    @TableField(value = "CUTOVER_AREA")
    private String cutoverArea;

    @TableField(value = "CIRCUIT_NAME")
    private String circuitName;

    @TableField(value = "BUSINESS_ID")
    private String businessId;

    @TableField(value = "BSS_NUMBER")
    private String bssNumber;

    @TableField(value = "CUSTOMER")
    private String customer;

    @TableField(value = "A_COMPANY")
    private String aCompany;

    @TableField(value = "A_COMPANY_ADDR")
    private String aCompanyAddr;

    @TableField(value = "Z_COMPANY")
    private String zCompany;

    @TableField(value = "Z_COMPANY_ADDR")
    private String zCompanyAddr;

    @TableField(value = "CUST_GRP_ID")
    private Integer custGrpId;

    @TableField(value = "GRP_CODE")
    private String grpCode;

    @TableField(value = "GRP_NAME")
    private String grpName;

    @TableField(value = "MAIN_ORDER_FLOW_NO")
    private String mainOrderFlowNo;

    @TableField(value = "ORDER_FLOW_NO")
    private String orderFlowNo;

    @TableField(value = "ZB_BUILD_ID")
    private String zbBuildId;

    @TableField(value = "BUILDING_NAME")
    private String buildingName;

    @TableField(value = "BREAK_STATE")
    private String breakState;

    @TableField(value = "IMPACT_BIZ")
    private String impactBiz;

    @TableField(value = "SERV_MANAGER_ID")
    private String servManagerId;

    @TableField(value = "create_date")
    private Date createDate;
}
