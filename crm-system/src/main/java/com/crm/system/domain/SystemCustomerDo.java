package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
