package com.crm.system.domain.dao.accManager;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 离职人员预警清单表
 *
 * @author lin_Yao
 * @date 2025/3/13
 */
@Data
@TableName("waring_list_table")
public class WaringListTable implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("cycle_id")
    private String cycleId;

    @TableField("oa_user_name")
    private String oaUserName;

    @TableField("full_name")
    private String fullName;

    @TableField("waring_type")
    private String waringType;

    @TableField("state")
    private Integer state;

    @TableField("area_id")
    private String areaId;

    @TableField("area_name")
    private String areaName;

    @TableField("county_id")
    private String countyId;

    @TableField("county_name")
    private String countyName;

    @TableField("cust_level4_manager")
    private String custLevel4Manager;

    @TableField("cust_level4_manager_count")
    private Integer custLevel4ManagerCount;

    @TableField("natural_cust_manager")
    private String naturalCustManager;

    @TableField("natural_cust_manager_count")
    private Integer naturalCustManagerCount;

    @TableField("entity_cust_manager")
    private String entityCustManager;

    @TableField("entity_cust_manager_count")
    private Integer entityCustManagerCount;

    @TableField("opportunity_manager")
    private String opportunityManager;

    @TableField("opportunity_manager_count")
    private Integer opportunityManagerCount;

    @TableField("contract_manager")
    private String contractManager;

    @TableField("contract_manager_count")
    private Integer contractManagerCount;

    @TableField("retention_cust_manager")
    private String retentionCustManager;

    @TableField("retention_cust_manager_count")
    private Integer retentionCustManagerCount;

    @TableField("contact_person")
    private String contactPerson;

    @TableField("contact_person_count")
    private Integer contactPersonCount;

    @TableField("create_time")
    private Date createTime;
}
