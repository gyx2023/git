package com.crm.common.enums;

import lombok.Getter;

/**
 * 待办操作类型
 *
 * @author crm
 */
@Getter
public enum PendingOpType {
    ADD("a", "新增"),
    UPDATE("u", "更新");

    private final String code;
    private final String info;

    PendingOpType(String code, String info) {
        this.code = code;
        this.info = info;
    }

}
