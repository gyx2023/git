package com.crm.system.domain.dto;

import lombok.Data;

/**
 * 领导查询 DTO（宽带升级预警用）
 */
@Data
public class BossLeaderDto {
    private String empId;
    private String oaUserName;
    private String fullName;
    private String areaName;
    private String countyName;
}
