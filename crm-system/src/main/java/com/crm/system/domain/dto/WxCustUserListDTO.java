package com.crm.system.domain.dto;

import lombok.Data;

@Data
public class WxCustUserListDTO {
    private String orderType;
    private Integer tabType;
    private String selectId;
    private String areaId;
    private String countryId;
    private String grpCode;
    private String orderNumber;
    private String createTimeStart;
    private String createTimeEnd;
    private String custName;
    private String businessDep;
    private String isImpPrt;
    private String grpType;
    private String svcNum;
    private String isDistribute;
    private Long userId;
    /** 客户经理过滤（预警客户列表点击到期量跳转时传入，对应老框架 md.user_id = userId） */
    private String managerId;
    private String authAreaIds;
    private Integer startRow;
    private Integer pageSize;
}
