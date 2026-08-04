package com.crm.system.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 领导信息 DTO（升级预警策略共用）
 *
 * @author crm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderInfoDto {
    private Long userId;
    private Long deptId;
    private String nickName;
    private String phone;
    private String oaUserName;
    private String fullName;
}
