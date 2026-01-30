package com.crm.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GrpExpandTypeenum {

    PROVINCE_ZT("01", "省直拓"),
    CITY_ZT("02", "市直拓"),
    COUNTY_ZT("03", "县直拓");


    private final String code;
    private final String label;
}
