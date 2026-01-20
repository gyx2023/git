package com.crm.system.domain.zbzq;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName( "rep_sys_person_new_d")
public class RepSysPersonNewDDo implements Serializable {

    /**
     * 员工ID
     */
    private String empId;

    /**
     * HR编码
     */
    private String hrCode;

    /**
     * 姓名
     */
    private String fullName;

    /**
     * OA用户名
     */
    private String oaUserName;

    /**
     * 在职标识
     */
    private String onjobFlag;

    /**
     * 用户身份证号
     */
    private String userIdentityCode;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 用户人员类型编码
     */
    private String userPersonTypeCode;

    /**
     * 标签分类编码
     */
    private String labelCategoryCode;

    /**
     * 元素名称3映射编码
     */
    private String elementName3MapCode;

    /**
     * 人员大类型
     */
    private String personBigType;

    /**
     * 区域ID
     */
    private String areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 县ID
     */
    private String countyId;

    /**
     * 县名称
     */
    private String countyName;

    /**
     * HX区域ID
     */
    private String hxZoneId;

    /**
     * HX区域名称
     */
    private String hxZoneName;

    /**
     * 部门类型编码
     */
    private String deptTypeCode;

    /**
     * 备用字段1
     */
    private String bakColumn1;

    /**
     * 备用字段2
     */
    private String bakColumn2;

    /**
     * 备用字段3
     */
    private String bakColumn3;

    /**
     * 备用字段4
     */
    private String bakColumn4;

    /**
     * 备用字段5
     */
    private String bakColumn5;

    /**
     * 备用字段6
     */
    private String bakColumn6;

    /**
     * 离职日期
     */
    private String quitDate;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建日期
     */
    private String creationDate;

    /**
     * 最后更新人
     */
    private String lastUpdatedBy;

    /**
     * 最后更新日期
     */
    private String lastUpdateDate;

    /**
     * 备用字段7
     */
    private String bakColumn7;

    /**
     * 备用字段8
     */
    private String bakColumn8;

    /**
     * 备用字段9
     */
    private String bakColumn9;

    /**
     * SR区域ID
     */
    private String srZoneId;

    /**
     * ETL数据周期
     */
    private Date etlDataCycle;

    /**
     * ETL时间
     */
    private Date etlTime;

    /**
     * 人员归属主岗组织编码
     */
    private String bakColumn10;

    /**
     * 人员归属主岗组织名称
     */
    private String bakColumn11;
}
