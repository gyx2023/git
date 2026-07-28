package com.crm.askdata.service;

import com.crm.askdata.entity.ValueItem;
import com.crm.askdata.mapper.ValueDomainMapper;
import com.crm.askdata.mapper.ValueItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 智能问数枚举域读取服务。
 * <p>
 * 该服务只读取数据库中已启用的本地枚举项，供 HiAgent 与 ChinaUniconAgent
 * 共用的工具 Schema 生成逻辑调用，不参与工具执行参数校验。
 */
@Service
@RequiredArgsConstructor
public class ValueDomainService {

    private final ValueDomainMapper valueDomainMapper;
    private final ValueItemMapper valueItemMapper;

    public List<ValueItem> getEnabledItems(String domainCode) {
        if (domainCode == null || domainCode.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String normalizedDomainCode = domainCode.trim();
        if (valueDomainMapper.selectEnabledByCode(normalizedDomainCode) == null) {
            return Collections.emptyList();
        }
        List<ValueItem> items = valueItemMapper.selectEnabledByDomainCode(normalizedDomainCode);
        return items == null ? Collections.emptyList() : items;
    }
}
