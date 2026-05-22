package com.crm.system.service.impl;

import com.crm.system.domain.RepCustContractMillSumM;
import com.crm.system.mapper.RepCustContractMillSumMMapper;
import com.crm.system.service.IRepCustContractMillSumMService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 政企运营平台合同收入报表Service业务层处理
 *
 * @author wangjl
 * @date 2021/8/31 11:24
 */
@Slf4j
@Service
public class RepCustContractMillSumMServiceImpl implements IRepCustContractMillSumMService {

    @Resource
    private RepCustContractMillSumMMapper repCustContractMillSumMMapper;

    /**
     * 批量插入数据
     *
     * @param list 待插入的数据列表
     */
    @Override
    public void insertBatch(List<RepCustContractMillSumM> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        log.info("开始插入{}条数据", list.size());
        long startTime = System.currentTimeMillis();
        try {
            repCustContractMillSumMMapper.insertBatch(list);
            long endTime = System.currentTimeMillis();
            log.info("插入{}条数据成功,耗时:{}ms", list.size(), (endTime - startTime));
        } catch (Exception e) {
            log.error("插入数据失败", e);
            throw e;
        }
    }
}
