package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统用户customer_no对象 sys_user_cust_no
 *
 * @author crm
 */
@Data
@TableName("sys_user_cust_no")
public class SysUserCustNo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id_")
    private Integer id;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Integer userId;

    /**
     * 客户编号
     */
    @TableField(value = "customer_no")
    private String customerNo;
}
