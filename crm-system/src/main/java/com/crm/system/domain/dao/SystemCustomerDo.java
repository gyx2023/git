package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("system_customer")
public class SystemCustomerDo{
    private static final long serialVersionUID=1L;

    /**
     * 主键
     */
    @TableId(value = "id_")
    private Long id;

    private String custName;
    private String custManagerName;
    private String custManagerDep;
}
