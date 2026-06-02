package com.crm.system.domain.vo;

import java.util.Date;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;


/**
 * 【请填写功能名称】视图对象 dwd_rep_sys_person_d
 *
 * @author crm
 * @date 2025-11-24
 */
@Data
@ExcelIgnoreUnannotated
public class DwdRepSysPersonDVo {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long id;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String empId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String hrCode;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String fullName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String oaUserName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Integer onjobFlag;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String userIdentityCode;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String mobile;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String userPersonTypeCode;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String labelCategoryCode;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String elementName3MapCode;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String personBigType;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long areaId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String areaName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long countyId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String countyName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String hxZoneId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String hxZoneName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String deptTypeCode;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String bakColumn1;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String bakColumn2;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String bakColumn3;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String bakColumn4;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String bakColumn5;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String bakColumn6;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date quitDate;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String createdBy;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date creationDate;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String lastUpdatedBy;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date lastUpdateDate;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String bakColumn7;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String bakColumn8;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String bakColumn9;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String srZoneId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date etlDataCycle;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date etlTime;


}
