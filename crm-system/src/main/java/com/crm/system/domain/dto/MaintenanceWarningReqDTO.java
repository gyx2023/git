package com.crm.system.domain.dto;

import lombok.Data;

/**
 * 维系预警报表请求DTO
 */
@Data
public class MaintenanceWarningReqDTO {

    /** 报表Tab类型 1-6 */
    private Integer tabType;

    /** 预警类型（orderType） */
    private String orderType;

    /** 开始日期 */
    private String leftTime;

    /** 结束日期 */
    private String rightTime;

    /** 地市ID（多选，逗号分隔） */
    private String areaId;

    /** 区县ID（多选，逗号分隔） */
    private String countryId;

    /** 客户归属部门 */
    private String businessDep;

    /** 是否重保客户 */
    private String isImpPrt;

    /** 集团类别 */
    private String grpType;

    /** 是否派发（0:未派发 1:已派发） */
    private String isDistribute;

    /** 客户经理编码 */
    private String managerId;

    /** 客户经理名称 */
    private String managerName;

    /** 客户经理手机号码 */
    private String phoneId;

    /** 客户名称（清单查询用） */
    private String grpName;

    /** 客户15位编码（清单查询用） */
    private String grpCode;

    /** 客户编号（清单查询用） */
    private String custGrpId;

    /** 用户号码（清单查询用） */
    private String svcNum;

    /** 当前页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    /** 分页起始行（由 Service 计算后注入，不从前端传入） */
    private Integer startRow;
}
