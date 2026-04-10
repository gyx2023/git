package com.crm.system.domain.vo;

import lombok.Data;

@Data
public class ContractEformRespVo {
    private String contractCode;
    private String contractName;
    private String customerName;
    private String subjectName;
    private String agentPersonOa;
    private String contractAmount;
    private String signTime;
    private String startTime;
    private String endTime;
    private String tradeType;
}
