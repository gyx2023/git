package com.crm.workflow.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 业务上下文视图对象
 * 携带流程详情页所需的全部业务数据
 *
 * @author crm
 */
@Data
public class WfBusinessContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 数据来源：new（Flowable）/ old（Activiti） */
    private String source;

    /** 流程是否已结束 */
    private Boolean finished;

    /** 业务数据区块列表，按 order 排序 */
    private List<WfBusinessSection> sections;

    /** 可执行的操作（如关闭商机、修改签约时间） */
    private List<WfBusinessAction> actions;
}
