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
@EqualsAndHashCode(callSuper = true)
@TableName("sys_area")
public class SysArea extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     *
     */
    @TableId(value = "id_")
    private Long id;
    /**
     *
     */
    @TableId(value = "name_")
    private String name;
    /**
     *
     */
    @TableId(value = "type_")
    private String type;
    /**
     *
     */
    @TableId(value = "parent_")
    private Long parent;
    /**
     *
     */
    @TableId(value = "pinyin_")
    private String pinyin;
    /**
     *
     */
    @TableId(value = "latlng_")
    private String latlng;
    /**
     *
     */
    @TableId(value = "remark_")
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
    @TableId(value = "order_")
    private Long order;
    /**
     * 政企专区区县配置 1-市级中台调度 2-区县中台
     */
    @TableId(value = "zq_type")
    private Integer zqType;
    /**
     * 映射集团区域代码
     */
    @TableId(value = "zb_area")
    private Long zbArea;
    /**
     * 集团 县市编码
     */
    @TableId(value = "jt_code")
    private String jtCode;

}
