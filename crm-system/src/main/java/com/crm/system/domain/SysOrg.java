package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 组织机构对象 sys_org
 */
@Data
@TableName("sys_org")
public class SysOrg implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id_", type = IdType.AUTO)
    private Integer id;

    /**
     * 组织名称
     */
    @TableField("name_")
    private String name;

    /**
     * 组织编码
     */
    @TableField("code_")
    private String code;

    /**
     * 父组织ID
     */
    @TableField("parent_")
    private Integer parent;

    /**
     * 顺序
     */
    @TableField("order_")
    private Integer order;

    /**
     * 所属行政区域ID
     */
    @TableField("area_id")
    private Integer areaId;

    /**
     * 联系人
     */
    @TableField("relation_person")
    private String relationPerson;

    /**
     * 联系电话
     */
    @TableField("relation_tel")
    private String relationTel;

    /**
     * 联系邮箱
     */
    @TableField("relation_email")
    private String relationEmail;

    /**
     * 联系地址
     */
    @TableField("relation_addr")
    private String relationAddr;

    /**
     * 备注
     */
    @TableField("remark_")
    private String remark;

    /**
     * 是否可用
     */
    @TableField("is_valid")
    private String isValid;

    /**
     * 营销组织ID
     */
    @TableField("org_id_yx")
    private String orgIdYx;

    /**
     * 网格编码
     */
    @TableField("grid_code")
    private String gridCode;

    /**
     * 营销组织名称
     */
    @TableField("org_name_yx")
    private String orgNameYx;

    /**
     * 组织类型
     */
    @TableField("org_type")
    private String orgType;

    /**
     * 同步状态
     */
    @TableField("syn_status")
    private String synStatus;
}
