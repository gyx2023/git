package com.crm.workflow.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 业务操作视图对象
 * 描述流程详情页中可执行的业务操作按钮
 *
 * @author crm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WfBusinessAction implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 操作标识：close, modifySignTime, conversion */
    private String actionKey;

    /** 按钮文本：关闭商机, 修改签约时间, 转化 */
    private String label;

    /** 按钮类型：primary, warning, success */
    private String type;

    /** 按钮图标 */
    private String icon;
}
