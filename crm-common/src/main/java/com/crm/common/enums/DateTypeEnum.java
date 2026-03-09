package com.crm.common.enums;

import lombok.Getter;

/**
 * 日期类型枚举
 */
@Getter
public enum DateTypeEnum {
    DAY("D", "日"),
    MONTH("M", "月"),
    YEAR("Y", "年");
    private String code;
    private String desc;
    DateTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
