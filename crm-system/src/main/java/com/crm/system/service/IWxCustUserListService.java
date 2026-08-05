package com.crm.system.service;

import com.crm.common.core.page.TableDataInfo;

import java.util.Map;

public interface IWxCustUserListService {
    TableDataInfo<Map<String, Object>> queryList(Map<String, Object> params);
}
