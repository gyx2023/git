package com.crm.system.domain.dto;

import lombok.Data;

/**
 * 领导角色用户 DTO（兜底 fallback 用）
 */
@Data
public class LeaderDto {
    private Long userId;
    private Long deptId;
    private String nickName;
    private String phonenumber;
}
