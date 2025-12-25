package com.crm.common.core.domain.dto;

import lombok.Data;


@Data
public class ContractStatisticsUserRespDTO {
    private String userName;
    private String userId;
    private String deptId;
    private Integer countQy;
    private Double moneyQy;
    private Integer ybwQy;
    private Integer ybyqQy;
    private Integer ybwqQy;
    private Integer wqQy;
    private Double gxedMoney;
}
