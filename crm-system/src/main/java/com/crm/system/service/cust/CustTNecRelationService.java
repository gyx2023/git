package com.crm.system.service.cust;

import com.crm.system.domain.dao.CustNecRelationDo;
import com.crm.system.domain.dao.CustTNecRelationDo;

import java.util.List;

/**
 * cust_t_nec_relation服务接口
 */
public interface CustTNecRelationService {

    void truncateTable();

    List<CustNecRelationDo> getCustTNecRelationList();

    void insertBatch(List<CustTNecRelationDo> batchList);

}
