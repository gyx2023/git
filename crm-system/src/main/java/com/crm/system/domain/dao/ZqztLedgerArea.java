package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 台账省市区县街道数据字典 zqzt_ledger_area
 */
@Data
@TableName("zqzt_ledger_area")
public class ZqztLedgerArea implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 组织code */
    private String orgCode;

    /** 组织名称 */
    private String orgName;

    /** 上级code */
    private String parentCode;

    /** 组织全名称 */
    private String orgFullName;

    /** 组织层级(市/区县/乡镇街道) */
    private String orgLevel;

    /** 对应sys_area.id */
    private String zqId;

    /** 对应sys_area名称 */
    private String zqName;

    /** 对应sys_area类型 */
    private String zqType;

    /** 展示顺序（默认org_code顺序） */
    private Integer sortOrder;

    /** 创建时间 */
    private Date createTime;

    /** 是否有效 */
    private String isValid;
}
