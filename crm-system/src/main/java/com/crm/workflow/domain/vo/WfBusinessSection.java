package com.crm.workflow.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 业务区块视图对象
 * 描述流程详情页中一个业务数据区块（如订单详情、商机详情、客户详情）
 *
 * @author crm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WfBusinessSection implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 区块标识：order, opportunity, customer */
    private String sectionKey;

    /** 显示标题：订单详情, 商机详情 */
    private String title;

    /** 显示顺序（升序） */
    private Integer order;

    /** 业务实体数据（VO 对象） */
    private Object data;
}
