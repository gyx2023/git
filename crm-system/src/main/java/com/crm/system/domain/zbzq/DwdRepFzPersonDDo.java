package com.crm.system.domain.zbzq;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("dwd_rep_fz_person_d")
public class DwdRepFzPersonDDo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 员工ID
     */
    @TableField("EMP_ID")
    private String empId;

    /**
     * 姓名
     */
    @TableField("FULL_NAME")
    private String fullName;

    /**
     * 用户身份证号/标识号
     */
    @TableField("USER_IDENTITY_CODE")
    private String userIdentityCode;

    /**
     * 服务类型编码
     */
    @TableField("SERVICE_TYPE_CODE")
    private String serviceTypeCode;

    /**
     * 渠道总部编码
     */
    @TableField("CHANNEL_HQ_CODE")
    private String channelHqCode;

    /**
     * 渠道名称
     */
    @TableField("CHANNEL_NAME")
    private String channelName;

    /**
     * 区域ID
     */
    @TableField("AREA_ID")
    private String areaId;

    /**
     * 区域名称
     */
    @TableField("AREA_NAME")
    private String areaName;

    /**
     * 区县ID
     */
    @TableField("COUNTY_ID")
    private String countyId;

    /**
     * 区县名称
     */
    @TableField("COUNTY_NAME")
    private String countyName;

    /**
     * 渠道状态编码
     */
    @TableField("QD_STATE_CODE")
    private String qdStateCode;

    /**
     * 使用状态编码
     */
    @TableField("USE_STATUS_CODE")
    private String useStatusCode;

    /**
     * 备用字段1
     */
    @TableField("BAK_COLUMN1")
    private String bakColumn1;

    /**
     * 备用字段2
     */
    @TableField("BAK_COLUMN2")
    private String bakColumn2;

    /**
     * 备用字段3
     */
    @TableField("BAK_COLUMN3")
    private String bakColumn3;

    /**
     * 备用字段4
     */
    @TableField("BAK_COLUMN4")
    private String bakColumn4;

    /**
     * 备用字段5
     */
    @TableField("BAK_COLUMN5")
    private String bakColumn5;

    /**
     * 备用字段6
     */
    @TableField("BAK_COLUMN6")
    private String bakColumn6;

    /**
     * 创建人
     */
    @TableField( "CREATED_BY")
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField("CREATION_DATE")
    private Date creationDate;

    /**
     * 最后更新人
     */
    @TableField( "LAST_UPDATED_BY")
    private String lastUpdatedBy;

    /**
     * 最后更新时间
     */
    @TableField( "LAST_UPDATE_DATE")
    private Date lastUpdateDate;

    /**
     * ETL数据周期
     */
    @TableField("ETL_DATA_CYCLE")
    private Date etlDataCycle;

    /**
     * ETL处理时间（含毫秒）
     */
    @TableField("ETL_TIME")
    private Date etlTime;
}
