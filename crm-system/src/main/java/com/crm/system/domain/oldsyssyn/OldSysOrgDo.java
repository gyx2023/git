package com.crm.system.domain.oldsyssyn;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_org")
@ExcelIgnoreUnannotated
public class OldSysOrgDo {


    @TableId("id_")
    private Integer id;

    @TableField("name_")
    private String name;

    @TableField("code_")
    private String code;

    @TableField("order_")
    private Integer order;

    @TableField("area_id")
    private Integer areaId;

    @TableField("parent_")
    private Integer parent;

    @TableField("relation_person")
    private String relationPerson;

    @TableField("relation_tel")
    private String relationTel;


    @TableField("relation_email")
    private String relationEmail;


    @TableField("relation_addr")
    private String relationAddr;

    @TableField("remark_")
    private String remark;

    @TableField("creator_")
    private Integer creator;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("modifier_")
    private Integer modifier;

    @TableField("modify_time")
    private LocalDateTime modifyTime;

    @TableField("is_valid")
    private Boolean isValid;

    @TableField("org_id_yx")
    private String orgIdYx;

    @TableField("grid_code")
    private String gridCode;

    @TableField("org_name_yx")
    private String orgNameYx;

    @TableField("org_type")
    private String orgType;

    @TableField("syn_status")
    private String synStatus;
}
