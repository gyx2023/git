package com.crm.askdata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

/**
 * 智能问数参数枚举域。
 * <p>
 * 枚举域是 HiAgent 与 ChinaUniconAgent 共用的工具参数字典定义；
 * sourceType 当前仅记录维护来源，Schema 生成只读取本地枚举项表。
 */
@Data
@TableName("askdata_value_domain")
public class ValueDomain {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("domain_code")
    private String domainCode;

    @TableField("domain_name")
    private String domainName;

    @TableField("source_type")
    private String sourceType;

    @TableField("source_config")
    private String sourceConfig;

    @TableField("parent_domain_code")
    private String parentDomainCode;

    @TableField("cache_ttl_seconds")
    private Integer cacheTtlSeconds;

    /** 状态：ENABLED / DISABLED。 */
    private String status;

    @Version
    private Integer version;
}
