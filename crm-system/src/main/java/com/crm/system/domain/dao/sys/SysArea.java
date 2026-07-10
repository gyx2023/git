package com.crm.system.domain.dao.sys;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;

/**
 * 区域信息对象 sys_area
 *
 * @author crm
 * @date 2025-11-24
 */
@Data
@TableName("sys_area")
public class SysArea implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId(value = "id_")
    private Long id;
    /**
     *
     */
    @TableField("name_")
    private String name;
    /**
     * 区域类型(1:省份,2:地市,3:区县)
     */
    @TableField("type_")
    private String type;
    /**
     *
     */
    @TableField("parent_")
    private Long parent;
    /**
     *
     */
    @TableField("pinyin_")
    private String pinyin;
    /**
     *
     */
    @TableField("latlng_")
    private String latlng;
    /**
     *
     */
    @TableField("remark_")
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
    @TableField("order_")
    private Long order;
    /**
     * 政企专区区县配置 1-市级中台调度 2-区县中台
     */
    @TableField("zq_type")
    private Integer zqType;
    /**
     * 主备区域
     */
    @TableField("zb_area")
    private String zbArea;
    /**
     * 集团 县市编码
     */
    @TableField("jt_code")
    private String jtCode;

    @TableField("emp_area_id")
    private int empAreaId;

    @TableField("emp_county_id")
    private int empCountyId;

    /**
     * 百强区县
     */
    @TableField("top100_counties")
    private String top100Counties;
}
