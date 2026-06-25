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
    private String authAreaIds;
    private Integer startRow;
    private Integer pageSize;
}
