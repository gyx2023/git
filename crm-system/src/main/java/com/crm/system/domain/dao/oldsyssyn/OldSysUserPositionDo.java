package com.crm.system.domain.dao.oldsyssyn;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 老系统用户职务关系对象 sys_user_position
 *
 * @author crm
 * @date 2026-05-24
 */
@Data
@TableName("sys_user_position")
public class OldSysUserPositionDo {

    /**
     * 主键ID
     */
    @TableId(value = "id_", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 职务ID
     */
    private Long positionId;

    /**
     * 是否有效
     */
    @TableField(value = "valid_")
    private String valid;

    /**
     * 创建时间
     */
    private Date createTime;

}
