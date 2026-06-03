package com.crm.system.service.topp.impl;

import com.crm.system.domain.topp.ToppOrderSyncGroup;
import com.crm.system.mapper.topp.ToppOrderSyncGroupMapper;
import com.crm.system.service.topp.IToppOrderSyncGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 订单同步集团服务大厅Service业务层处理
 *
 * @author crm
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ToppOrderSyncGroupServiceImpl implements IToppOrderSyncGroupService {

    private final ToppOrderSyncGroupMapper baseMapper;

    @Override
    public List<ToppOrderSyncGroup> getPendingList() {
        return baseMapper.selectPendingList();
    }

    @Override
    public void deleteById(Long id) {
        baseMapper.deleteById(id);
    }

    @Override
    public void saveSyncInfo(String procInstId, String taskId, String taskCode, String sendType) {
        ToppOrderSyncGroup entity = new ToppOrderSyncGroup();
        entity.setProcInstId(procInstId);
        entity.setTaskId(taskId);
        entity.setTaskCode(taskCode);
        entity.setSendType(sendType);
        entity.setCreateTime(new Date());
        baseMapper.insert(entity);
    }
}
