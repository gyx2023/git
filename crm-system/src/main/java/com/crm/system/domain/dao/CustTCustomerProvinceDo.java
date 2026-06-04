package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("cust_t_roster_customer_province")
public class CustTCustomerProvinceDo {
    @TableId(value = "id_", type = IdType.AUTO)
    private Integer id;
    private String majorFlag;
    private String rosterCustomerId;
}
