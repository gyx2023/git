package com.crm.common.enums;

/**
 * 系统参数开关状态枚举
 */
public enum SwitchStatus {
    ON("on", "开启"),
    OFF("off", "关闭");

    private final String value;
    private final String description;

    SwitchStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 判断给定的值是否为开启状态
     *
     * @param value 待判断的值
     * @return 是否开启
     */
    public static boolean isOn(String value) {
        return ON.getValue().equalsIgnoreCase(value);
    }

    /**
     * 判断给定的值是否为关闭状态
     *
     * @param value 待判断的值
     * @return 是否关闭
     */
    public static boolean isOff(String value) {
        return OFF.getValue().equalsIgnoreCase(value);
    }

    /**
     * 根据值获取枚举对象
     *
     * @param value 枚举值
     * @return 枚举对象
     */
    public static SwitchStatus fromValue(String value) {
        for (SwitchStatus status : values()) {
            if (status.getValue().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid switch status value: " + value);
    }
}