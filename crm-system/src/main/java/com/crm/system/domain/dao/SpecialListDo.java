package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 特殊名单对象 zqzt_sys_speciallist
 *
 * @author crm
 * @date 2026-06-16
 */
@Data
@TableName("zqzt_sys_speciallist")
public class SpecialListDo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 场景编码
     */
    private String sceneCode;


    /**
     * 目标值
     */
    private String targetValue;

    /**
     * 状态
     */
    private String status;

    /**
     * 生效时间
     */
    private Date effectiveTime;

    /**
     * 失效时间
     */
    private Date expireTime;

    /**
     * 原因
     */
    private String reason;

    /**
     * 创建人
     */
    private Integer createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private Integer updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

}
