package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 经分人员职位表对象 dwd_rep_gw_pst_d
 */
@Data
@TableName("dwd_rep_gw_pst_d")
public class DwdRepGwPstD implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id_", type = IdType.AUTO)
    private Integer id;

    /**
     * 员工ID
     */
    @TableField("EMP_ID")
    private String empId;

    /**
     * HR编码
     */
    @TableField("HR_CODE")
    private String hrCode;

    /**
     * MDM组织编码
     */
    @TableField("MDM_ORG_CODE")
    private Integer mdmOrgCode;

    /**
     * 组织名称
     */
    @TableField("ORG_NAME")
    private String orgName;

    /**
     * 职位编码
     */
    @TableField("POS_CODE")
    private Integer posCode;

    /**
     * ETL数据周期
     */
    @TableField("ETL_DATA_CYCLE")
    private Date etlDataCycle;

    /**
     * ETL时间
     */
    @TableField("ETL_TIME")
    private Date etlTime;
}
