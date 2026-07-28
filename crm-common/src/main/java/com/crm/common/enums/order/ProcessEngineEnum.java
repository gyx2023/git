package com.crm.common.enums.order;

import lombok.Getter;

/**
 * 订单表流程引擎字段枚举
 */
@Getter
public enum ProcessEngineEnum {
    /**
     * 新框架流程引擎
     */
    FLOWABLE,

    /**
     * 老框架流程引擎
     */
    ACTIVITI,
    /**
     * 其他无流程引擎
     */
    OTHER;


}
