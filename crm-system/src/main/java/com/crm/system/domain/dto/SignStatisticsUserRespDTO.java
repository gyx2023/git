package com.crm.system.domain.dto;

import lombok.Data;


@Data
public class SignStatisticsUserRespDTO {
    private String userName;
    private String userId;
    private String deptId;
    private Integer countTb;
    private Double moneyTb;
    private Integer ybwTb;
    private Integer ybyqTb;
    private Integer ybwqTb;
    private Integer wqTb;
    private Integer countZb;
    private Integer ybwZb;
    private Integer ybyqZb;
    private Integer ybwqZb;
    private Integer wqZb;
    private Double moneyZb;
    private Double gxedZb;
}
