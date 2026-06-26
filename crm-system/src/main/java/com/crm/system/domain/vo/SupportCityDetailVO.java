package com.crm.system.domain.vo;

import lombok.Data;

/**
 * 支撑情况地市统计明细返回
 *
 * @author crm
 */
@Data
public class SupportCityDetailVO {

    /** 商机编号 */
    private String oppCode;

    /** 商机名称 */
    private String oppName;

    /** 所属区县 */
    private String areaName;

    /** 客户经理姓名 */
    private String custManageName;

    /** 客户经理电话 */
    private String custManageTel;

    /** 支撑经理姓名 */
    private String designerName;

    /** 支撑经理电话 */
    private String designerTel;

    /** 支撑经理所属区域 */
    private String designerArea;

    /** 支撑类型（任务名称） */
    private String taskCode;

    /** 创建时间 */
    private String createTime;

    /** 审核时间 */
    private String auditTime;

    /** 评分 */
    private Double score;

}
