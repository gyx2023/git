package com.crm.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author pengjun
 * 商机阶段
 */
@Getter
@AllArgsConstructor
public enum OppoStateEnum {
    CREATE("oppo_status_cjsj", "商机创建"),

    SUPPORT("oppo_status_sjzc", "商机支撑"),

    BIDDING("oppo_status_ztb", "招投标"),
    SIGNING("oppo_status_sjqy", "商机签约"),
    IMPLEMENTATION("oppo_status_sjss", "商机实施"),

    PRODUCTION("oppo_status_sjdc", "商机达产"),

    TERMINATED("oppo_status_end", "商机终止");


    private final String code;
    private final String label;
}
