package com.crm.system.domain.vo;

import lombok.Data;

@Data
public class SystemCustomerVo {
    private Long id;

    private String custName;
    private String custManagerName;
    private String custManagerDep;
}
