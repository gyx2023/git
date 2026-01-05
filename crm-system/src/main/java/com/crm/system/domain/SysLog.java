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
    @TableId(value = "type_")
    private String type;

    /**
     * 日志内容
     */
    @TableId(value = "content_")
    private String content;

    /**
     * 日志级别
     */
    @TableId(value = "level_")
    private String level;

    /**
     * 操作的IP地址
     */
    @TableId(value = "computer_ip")
    private String computerIp;


    /**
     * 操作人员
     */
    private Long creator;


    /**
     * 操作时间
     */
    private LocalDateTime createTime;

    /**
     * 所属组织
     */
    @TableId(value = "org_id")
    private Long orgId;

    /**
     * 备注
     */
    @TableId(value = "remark_")
    private String remark;


    /**
     * 登陆类型
     */
    @TableId(value = "login_type")
    private String loginType;

    /**
     * 登陆应用
     */
    @TableId(value = "login_app")
    private String loginApp;


    @TableField(exist = false)
    private String creatorName;
    @TableField(exist = false)
    private String creatorOrgName;
    @TableField(exist = false)
    private Long creatorOrgId;

}
