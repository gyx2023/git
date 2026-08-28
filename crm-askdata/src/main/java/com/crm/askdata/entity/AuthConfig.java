package com.crm.askdata.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.crm.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 外部系统认证配置实体
 * <p>
 * 存储外部系统的认证方式与凭证信息，用于工具调用时注入认证 Header。
 * 一个认证配置可被多个工具共享（如指向同一外部系统的多个 API）。
 *
 * @author crm
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("askdata_auth_config")
public class AuthConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 外部系统名称
     */
    @TableField("system_name")
    private String systemName;

    /**
     * 系统描述
     */
    @TableField("system_desc")
    private String systemDesc;

    /**
     * 调用方标识（服务名/应用ID）
     */
    @TableField("caller_id")
    private String callerId;

    /**
     * 认证方式: NONE/SA_TOKEN/API_KEY/BASIC/OAUTH2_CLIENT
     */
    @TableField("auth_type")
    private String authType;

    /**
     * 认证配置JSON（加密存储）
     */
    @TableField("auth_config_json")
    private String authConfigJson;

    /**
     * 状态（0正常 1停用）
     */
    @TableField("status")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    @TableField("del_flag")
    @TableLogic
    private String delFlag;
}
