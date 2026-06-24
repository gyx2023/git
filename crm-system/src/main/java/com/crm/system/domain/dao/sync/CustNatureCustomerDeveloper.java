package com.crm.system.domain.dao.sync;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 自然客户开发者实体（源表 zq_user_36.cust_t_nature_customer_developer）
 *
 * @author crm
 * @date 2026-06-18
 */
@Data
@TableName("cust_t_nature_customer_developer")
public class CustNatureCustomerDeveloper implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long natureCustId;

    private String natureCustName;

    private String developerId;

    private String developerName;

    private String main;

    private String email;

    private String phoneNumber;

    private Date createDate;

    private String state;

    private String operationCode;

    private Date updateDate;

    private String remark;

    private String devStaffCode;

    private String provinceCode;

    private String eparchyCode;

    private String cityCode;

    private String oaCode;

    private String channelName;

    private String channelCode;

    private String orgName;

    private String serviceLevel;

    private String developerType;

    private String orgCode;

    private String tydm;

    private String regProvinceCode;

    private String regEparchyCode;

    private String regCityCode;
}
