package com.crm.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class KafkaMessageRecordEntity implements Serializable {
    private String messageId;
    private String messageBody;
    private String bizType;
    private String lineCode;
}
