package com.crm.system.domain.dto;

import lombok.Data;

/**
 * 商机支撑信息DTO
 */
@Data
public class OppoSupportInfoDTO {

    /** 商机编码 */
    private String oppCode;

    /** OA账号 */
    private String oaAccount;

    /** 用户登录名 */
    private String name;

    /** 用户中文名 */
    private String nameCh;
}
