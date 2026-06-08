package com.crm.system.domain.topp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 订单同步集团服务大厅对象 topp_order_sync_group
 *
 * @author crm
 */
@Data
@TableName("topp_order_sync_group")
public class ToppOrderSyncGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id_", type = IdType.AUTO)
    private Long id;

    /**
     * 流程实例ID
     */
    @TableField("procInstId")
    private String procInstId;

    /**
     * 任务ID
     */
    @TableField("taskId")
    private String taskId;

    /**
     * 任务编码
     */
    @TableField("taskCode")
    private String taskCode;

    /**
     * 发送类型 1-草稿 2-处理中 E-流程完结
     */
    @TableField("sendType")
    private String sendType;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;
}
