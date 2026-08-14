package com.crm.common.enums;

import lombok.Getter;

/**
 * 发送状态
 *
 * @author crm
 */
@Getter
public enum SendStatus {
    PENDING("0", "待发送"),
    SUCCESS("1", "成功"),
    FAIL("2", "失败");

    private final String code;
    private final String info;

    SendStatus(String code, String info) {
        this.code = code;
        this.info = info;
    }

}
