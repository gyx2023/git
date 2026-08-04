package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 集团待办同步对象 pending_group_todo
 */
@Data
@TableName("pending_group_todo")
public class PendingGroupTodo implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id_", type = IdType.AUTO)
    private Long id;

    @TableField("proc_inst_id")
    private String procInstId;

    @TableField("task_id")
    private String taskId;

    @TableField("operator_type")
    private String operatorType;

    @TableField("pending_title")
    private String pendingTitle;

    @TableField("pending_user_id")
    private String pendingUserId;

    /**
     * 0:未发送,1:发送成功,2:发送失败,3:发送中待回执
     */
    @TableField("send_status")
    private String sendStatus;

    @TableField("create_time")
    private Date createTime;

    @TableField("send_time")
    private Date sendTime;

    @TableField("fail_times")
    private Integer failTimes;

    @TableField("send_msg")
    private String sendMsg;

    @TableField("rec_msg")
    private String recMsg;
}
