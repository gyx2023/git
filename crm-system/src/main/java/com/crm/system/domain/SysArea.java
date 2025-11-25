package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import com.crm.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】对象 sys_area
 *
 * @author crm
 * @date 2025-11-24
 */
@Data
@TableName("sys_area")
public class SysArea implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *
     */
    @TableId(value = "id_")
    private Long id;
    /**
     *
     */
    @TableField(value = "name_")
    private String name;
    /**
     *
     */
    @TableField(value = "type_")
    private String type;
    /**
     *
     */
    @TableField(value = "parent_")
    private Long parent;
    /**
     *
     */
    @TableField(value = "pinyin_")
    private String pinyin;
    /**
     *
     */
    @TableField(value = "latlng_")
    private String latlng;
    /**
     *
     */
    @TableField(value = "remark_")
    private String remark;
    /**
     * bss编码
     */
    private String bssCode;
    /**
     * bss父级编码
     */
    private String bssCodeParent;
    /**
     *
     */
    @TableField(value = "order_")
    private Long order;
    /**
     * 政企专区区县配置 1-市级中台调度 2-区县中台
     */
    @TableField(value = "zq_type")
    private Integer zqType;
    /**
     * 映射集团区域代码
     */
    @TableField(value = "zb_area")
    private Long zbArea;
    /**
     * 集团 县市编码
     */
    @TableField(value = "jt_code")
    private String jtCode;


}
