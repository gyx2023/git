package com.crm.system.domain.bo;

import com.crm.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
@Data
@EqualsAndHashCode(callSuper = true)
public class SystemCustomerBo extends BaseEntity {
    private Long id;

    private String custName;
    private String custManagerName;
    private String custManagerDep;
}
