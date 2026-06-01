package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("cust_t_roster_customer_nature_member_province")
public class CustTRosterCustomerNatureMemberProvinceDo {
    @TableId(value = "id_", type = IdType.AUTO)
    private Integer id;
    private String natureCustId;
    private String rosterCustomerId;
}
