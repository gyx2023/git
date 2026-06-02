package com.crm.system.domain.dto;

import lombok.Data;

import java.lang.Double;

@Data
public class ContractStatisticsRespDTO {
    private String deptId;
    private String deptName;
    private Integer countQy;
    private Double moneyQy;
    private Integer ybwQy;
    private Integer ybyqQy;
    private Integer ybwqQy;
    private Integer wqQy;
}
