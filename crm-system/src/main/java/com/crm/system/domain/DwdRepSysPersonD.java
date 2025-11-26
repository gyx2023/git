package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.crm.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】对象 dwd_rep_sys_person_d
 *
 * @author crm
 * @date 2025-11-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dwd_rep_sys_person_d")
public class DwdRepSysPersonD extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     *
     */
    @TableId(value = "id_")
    private Long id;
    /**
     *
     */
    private String empId;
    /**
     *
     */
    private String hrCode;
    /**
     *
     */
    private String fullName;
    /**
     *
     */
    private String oaUserName;
    /**
     *
     */
    private Integer onjobFlag;
    /**
     *
     */
    private String userIdentityCode;
    /**
     *
     */
    private String MOBILE;
    /**
     *
     */
    private String userPersonTypeCode;
    /**
     *
     */
    private String labelCategoryCode;
    /**
     *
     */
    private String elementName3MapCode;
    /**
     *
     */
    private String personBigType;
    /**
     *
     */
    private Long areaId;
    /**
     *
     */
    private String areaName;
    /**
     *
     */
    private Long countyId;
    /**
     *
     */
    private String countyName;
    /**
     *
     */
    private String hxZoneId;
    /**
     *
     */
    private String hxZoneName;
    /**
     *
     */
    private String deptTypeCode;
    /**
     *
     */
    private String bakColumn1;
    /**
     *
     */
    private String bakColumn2;
    /**
     *
     */
    private String bakColumn3;
    /**
     *
     */
    private String bakColumn4;
    /**
     *
     */
    private String bakColumn5;
    /**
     *
     */
    private String bakColumn6;
    /**
     *
     */
    private Date quitDate;
    /**
     *
     */
    private String createdBy;
    /**
     *
     */
    private Date creationDate;
    /**
     *
     */
    private String lastUpdatedBy;
    /**
     *
     */
    private Date lastUpdateDate;
    /**
     *
     */
    private String bakColumn7;
    /**
     *
     */
    private String bakColumn8;
    /**
     *
     */
    private String bakColumn9;
    /**
     *
     */
    private String srZoneId;
    /**
     *
     */
    private Date etlDataCycle;
    /**
     *
     */
    private Date etlTime;

}
