package com.crm.system.service;

import com.crm.system.domain.dao.CustMaintInfo;

import java.util.List;
import java.util.function.Consumer;

/**
 * 客户维护信息查询服务接口
 * 用于查询iob_gov2.CUST_MAINT_INFO表
 *
 * @author 肖英杰
 */
public interface ICustMaintInfoService {

    void streamingQuery(String params, Consumer<List<CustMaintInfo>> consumer);
}
