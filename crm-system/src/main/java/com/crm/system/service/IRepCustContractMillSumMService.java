package com.crm.system.service;

import com.crm.system.domain.RepCustContractMillSumM;

import java.util.List;

/**
 * 政企运营平台合同收入报表Service接口
 *
 * @author wangjl
 * @date 2021/8/31 11:24
 */
public interface IRepCustContractMillSumMService {

    /**
     * 批量插入数据
     *
     * @param list 待插入的数据列表
     */
    void insertBatch(List<RepCustContractMillSumM> list);
}
