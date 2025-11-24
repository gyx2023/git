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
    private String name;
    /**
     * 
     */
    private String type;
    /**
     * 
     */
    private Long parent;
    /**
     * 
     */
    private String pinyin;
    /**
     * 
     */
    private String latlng;
    /**
     * 
     */
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
    private Long order;
    /**
     * 政企专区区县配置 1-市级中台调度 2-区县中台
     */
    private Integer zqType;
    /**
     * 映射集团区域代码
     */
    private Long zbArea;
    /**
     * 集团 县市编码
     */
    private String jtCode;

}
