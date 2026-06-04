package com.crm.system.enums;

import lombok.Getter;

/**
 * 合同状态枚举
 * @author: fpp
 */
@Getter
public enum ContractStatusEnum {
    /**
     * 合同状态：
     * 0	其他
     * 1	已审批
     * 10	取消
     * 11	起草
     * 12	终止解除
     * 13	作废申请中
     * 14	删除
     * 15	已定稿
     * 16	签订中
     * 2	审签中
     * 3	暂停履行
     * 4	终止履行
     * 5	办结
     * 6	变更补充
     * 7	作废
     * 9	履行中
     */
    OTHER("0", "其他"),
    APPROVED("1", "已审批"),
    CANCELLED("10", "取消"),
    DRAFT("11", "起草"),
    TERMINATED("12", "终止解除"),
    CANCELLATION_IN_PROGRESS("13", "作废申请中"),
    DELETED("14", "删除"),
    FINALIZED("15", "已定稿"),
    SIGNING("16", "签订中"),
    UNDER_REVIEW("2", "审签中"),
    PAUSED("3", "暂停履行"),
    TERMINATED_FULFILLMENT("4", "终止履行"),
    COMPLETED("5", "办结"),
    AMENDED("6", "变更补充"),
    CANCELLED_FINAL("7", "作废"),
    IN_PROGRESS("9", "履行中");

    private final String code;
    private final String description;

    ContractStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    /**
    * 根据状态码获取枚举实例
     *
         * @param code 状态码
     * @return 对应的枚举实例，若未找到则返回 null
        */
    public static ContractStatusEnum fromCode(String code) {
        for (ContractStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
