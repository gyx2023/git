package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 亚信同步日志对象 sys_log_yx
 */
@Data
@TableName("sys_log_yx")
public class SysLogYx implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id_", type = IdType.AUTO)
    private Integer id;

    /**
     * org:机构同步 user:人员同步
     */
    @TableField("type_")
    private String type;

    /**
     * 关联ID
     */
    @TableField("info_id")
    private Integer infoId;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 状态 1.同步成功 2.同步失败
     */
    @TableField("status_")
    private String status;

    /**
     * 失败返回消息
     */
    @TableField("erro_msg")
    private String erroMsg;

    /**
     * 失败类型 add新增失败 mod修改失败 del删除失败
     */
    @TableField("erro_type")
    private String erroType;
}
