package com.crm.system.service.cust.impl;

import com.crm.system.domain.dao.CustNecRelationDo;
import com.crm.system.domain.dao.CustTNecRelationDo;
import com.crm.system.mapper.cust.CustNecRelationMapper;
import com.crm.system.mapper.cust.CustTNecRelationMapper;
import com.crm.system.service.cust.CustTNecRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


/**
 * cust_t_nec_relation Service业务层处理
 */
@Service
@Slf4j
public class CustTNecRelationServiceImpl implements CustTNecRelationService {

    @Resource
    private CustTNecRelationMapper custTNecRelationMapper;

    @Resource
    private CustNecRelationMapper custNecRelationMapper;

    @Override
    public void truncateTable() {
        custTNecRelationMapper.truncateTable();
    }

    @Override
    public List<CustNecRelationDo> getCustTNecRelationList() {
        return custNecRelationMapper.getCustTNecRelationList();
    }

    @Override
    public void insertBatch(List<CustTNecRelationDo> batchList) {
        custTNecRelationMapper.insertBatch(batchList);
    }
}
