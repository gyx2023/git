package com.crm.system.service.travel;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "pre-travel-report")
public class PreTravelReportProperties {

    private String apiUrl;
    private String apiKey;
    private String appKey;
    private String userId;
}
