package com.crm.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OppoConversionStateEnum {
    AUTO_CLOSE("1", "自动关闭"),
    INITIATE_CLOSE("2", "发起关闭"),
    CONTRACT_ASSOCIATION("3", "关联合同"),
    FORM_ASSOCIATION("4", "关联电子表单"),
    BID_INFO_ASSOCIATION("5", "关联标讯"),
    OTHER("6", "其他");

    private final String code;
    private final String label;
}
