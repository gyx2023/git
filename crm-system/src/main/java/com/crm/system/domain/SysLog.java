package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统日志
 *
 * @author wangzh
 */
@Data
@TableName("sys_log")
public class SysLog implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 主键
     */
    @TableId(value = "id_")
    private Long id;
    /**
     * 日志类型
     */
    @TableField(value = "type_")
    private String type;

    /**
     * 日志内容
     */
    @TableField(value = "content_")
    private String content;

    /**
     * 日志级别
     */
    @TableField(value = "level_")
    private String level;

    /**
     * 操作的IP地址
     */
    @TableField(value = "computer_ip")
    private String computerIp;


    /**
     * 操作人员
     */
    @TableField(value = "creator_")
    private Long creator;


    /**
     * 操作时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 所属组织
     */
    @TableField(value = "org_id")
    private Long orgId;

    /**
     * 备注
     */
    @TableField(value = "remark_")
    private String remark;


    /**
     * 登陆类型
     */
    @TableField(value = "login_type")
    private String loginType;

    /**
     * 登陆应用
     */
    @TableField(value = "login_app")
    private String loginApp;


    @TableField(exist = false)
    private String creatorName;
    @TableField(exist = false)
    private String creatorOrgName;
    @TableField(exist = false)
    private Long creatorOrgId;

}
