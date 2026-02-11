package com.crm.system.service;

import java.util.Map;

public interface ISysParamService {
    Map<String, String> findValueMapsByType(String type);
}
