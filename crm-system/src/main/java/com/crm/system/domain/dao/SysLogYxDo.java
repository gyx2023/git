package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_log_yx")
public class SysLogYxDo {

    @TableId(value = "id_", type = IdType.AUTO)
    private Integer id;

    @TableField("type_")
    private String type;

    @TableField("info_id")
    private Integer infoId;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("status_")
    private String status;

    @TableField("erro_msg")
    private String erroMsg;

    @TableField("erro_type")
    private String erroType;

    public SysLogYxDo(String type, Integer infoId, Date createTime, Date updateTime, String status, String erroMsg,String erroType) {
        this.type = type;
        this.infoId = infoId;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.status = status;
        this.erroMsg = erroMsg;
        this.erroType = erroType;
    }
}
