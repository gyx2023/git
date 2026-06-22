package com.crm.system.domain.dao.sys;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 老系统sys_user表实体（仅用于查询迁移任务所需字段）
 *
 * @author crm
 */
@Data
@TableName("sys_user")
public class SysUserOld implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id_")
    private Long id;

    @TableField("name_ch")
    private String nameCh;

    @TableField("oa_account")
    private String oaAccount;
}
