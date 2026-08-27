package com.crm.system.domain.dto;

import lombok.Data;

/**
 * 组织部门查询 DTO（宽带升级预警用）
 */
@Data
public class OrgDepartmentDto {
    private String countyId;
    private String countyName;
    private String areaName;
}
