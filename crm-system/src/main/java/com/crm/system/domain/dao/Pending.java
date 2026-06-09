package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 待办对象 pending
 */
@Data
@TableName("pending")
public class Pending implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id_")
    private Long id;

    @TableField("pending_code")
    private String pendingCode;

    @TableField("operator_type")
    private String operatorType;

    @TableField("pending_title")
    private String pendingTitle;

    @TableField("pending_date")
    private String pendingDate;

    @TableField("pending_user_id")
    private String pendingUserID;

    @TableField("pending_URL")
    private String pendingURL;

    @TableField("pending_status")
    private String pendingStatus;

    @TableField("pending_level")
    private Integer pendingLevel;

    @TableField("pending_city_code")
    private String pendingCityCode;

    @TableField("pending_source_user_id")
    private String pendingSourceUserID;

    @TableField("pending_source")
    private String pendingSource;

    @TableField("pending_note")
    private String pendingNote;

    @TableField("last_update_date")
    private String lastUpdateDate;

    @TableField("send_status")
    private String sendStatus;

    @TableField("send_time")
    private Date sendTime;

    @TableField("response_msg")
    private String responseMsg;

    @TableField("fail_times")
    private Integer failTimes;
}
