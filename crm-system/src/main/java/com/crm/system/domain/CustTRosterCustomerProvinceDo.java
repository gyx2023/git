package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 名单客户省份关系对象 cust_t_roster_customer_province
 * 数据源：slave_1（MySQL从库）
 *
 * @author xyj
 * @date 2026-04-30
 */
@Data
@TableName("cust_t_roster_customer_province")
public class CustTRosterCustomerProvinceDo {

    /**
     * 主键ID
     */
    @TableField("id_")
    private Integer id;

    /**
     * 主要标识
     */
    @TableField("major_flag")
    private String majorFlag;

    /**
     * 名单客户ID
     */
    @TableField("roster_customer_id")
    private String rosterCustomerId;
}
