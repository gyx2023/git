package com.crm.system.domain.dto;

import lombok.Data;

@Data
public class SalesupBakTblBidSuboppoDTO {

    /**
     * 关联商机ID
     */
    private Long projectId;
    /**
     * 商机编码
     */
    private String oppoCode;
    /**
     * 标讯ID
     */
    private String infoId;
    /**
     * 标讯任务ID
     */
    private String bidTaskManagerId;
    /**
     * 商机挂牌类型: GREEN绿牌/NONE未挂牌/RED红牌/YELLOW黄牌
     */
    private String colorBrand;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 预算金额
     */

    private String budgetAmount;

    /**
     * 是否有效
     */
    private boolean valid;
}
