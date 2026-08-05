package com.crm.system.domain.bo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 商机统计查询业务对象
 *
 * @author system
 * @date 2026-06-12
 */
@Data
public class OpportunityStatsBo {

    /**
     * 组织ID
     */
    @NotNull(message = "组织ID不能为空")
    private Long orgId;

    /**
     * 开始时间（前端传入格式：yyyy-MM-dd）
     */
    private String leftTimeStr;

    /**
     * 结束时间（前端传入格式：yyyy-MM-dd）
     */
    private String rightTimeStr;

    /**
     * 开始时间（内部使用格式：yyyyMMdd）
     */
    private String leftTime;

    /**
     * 结束时间（内部使用格式：yyyyMMdd）
     */
    private String rightTime;

    /**
     * 商机类型列表
     */
    private List<String> types;
}
