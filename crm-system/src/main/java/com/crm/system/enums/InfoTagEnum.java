package com.crm.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 合同扩展信息标签枚举
 *
 * @author crm
 */
@Getter
@AllArgsConstructor
public enum InfoTagEnum {

    /**
     * 合同关联用户列表
     */
    GRP_CONTRACT_USER_LIST("GRP_CONTRACT_USER_LIST", "合同关联用户列表");

    /**
     * 标签值
     */
    private final String value;

    /**
     * 标签描述
     */
    private final String description;

    /**
     * 根据值获取枚举
     *
     * @param value 标签值
     * @return 对应的枚举项
     */
    public static InfoTagEnum getByValue(String value) {
        for (InfoTagEnum tag : values()) {
            if (tag.getValue().equals(value)) {
                return tag;
            }
        }
        return null;
    }
}
