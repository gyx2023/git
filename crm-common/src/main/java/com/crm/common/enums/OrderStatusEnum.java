package com.crm.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态
 *
 * @author crm
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    DRAFT("draft", "待提交"),
    RUNNING("running", "运行中"),
    TRACKING("tracking", "跟踪中"),
    END("end", "已结束"),
    ABANDON("abandon", "已废弃");

    private final String code;
    private final String desc;
}
