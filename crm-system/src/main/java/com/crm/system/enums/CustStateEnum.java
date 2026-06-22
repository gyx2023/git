package com.crm.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 集团状态
 */
@Getter
@AllArgsConstructor
public enum CustStateEnum {
    CUST_STATE_0("0","03"),
    CUST_STATE_1("1","02"),
    CUST_STATE_2("2","05"),
    CUST_STATE_3("3","06"),
    CUST_STATE_4("4","04");

    private final String code;
    private final String label;
}
