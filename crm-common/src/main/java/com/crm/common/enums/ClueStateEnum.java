package com.crm.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author pengjun
 * 商机阶段
 */
@Getter
@AllArgsConstructor
public enum ClueStateEnum {
    CREATE("new_create", "新建"),

    AUDIT("audit_finish", "评审通过"),

    REPORT("report_plan_finish", "商机跟踪"),
    PROJECT("project_finish", "立项"),
    CONTRACT("contract_finish", "合同签订"),

    PRODUCING("project_producing", "达产"),

    END("oppo_end", "终结");
    private final String code;
    private final String label;
}
