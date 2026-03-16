package com.crm.common.core.domain.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ZqztGrpCustAddUserListReqDTO {

    /**
     * 地市编码
     */
    private List<Map<String, String>> addInuseUserList;

}
