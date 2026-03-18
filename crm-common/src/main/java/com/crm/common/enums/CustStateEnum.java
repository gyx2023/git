package com.crm.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 集团状态
 */
@Getter
@AllArgsConstructor
public enum CustStateEnum {
    CUST_STATE_0("0", "02"),
    CUST_STATE_1("1", "03"),
    CUST_STATE_2("2", "04"),
    CUST_STATE_3("3", "05"),
    CUST_STATE_4("4", "06");

    private final String code;
    private final String label;
}
