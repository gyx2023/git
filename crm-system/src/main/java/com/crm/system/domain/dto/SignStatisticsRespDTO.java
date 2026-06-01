package com.crm.system.domain.dto;

import lombok.Data;


@Data
public class SignStatisticsRespDTO {
    private String deptId;
    private String deptName;
    private Integer countTb;
    private Double moneyTb;
    private Integer ybwTb;
    private Integer ybyqTb;
    private Integer ybwqTb;
    private Integer wqTb;
    private Integer countZb;
    private Double moneyZb;
    private Integer ybwZb;
    private Integer ybyqZb;
    private Integer ybwqZb;
    private Integer wqZb;
    private String zbl;
}
