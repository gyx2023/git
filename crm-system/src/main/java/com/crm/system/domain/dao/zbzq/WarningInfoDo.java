package com.crm.system.domain.dao.zbzq;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class WarningInfoDo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String type;
    private String warningLevel;
    private String orderId;
    private String scenarioExplanation;
    private Map<String, Object> keyInfo;
}
