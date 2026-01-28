package com.crm.system.service.impl;

import com.crm.system.domain.grp.TfFCustGroupMDo;
import com.crm.system.mapper.grp.TfFCustGroupMMapper;
import com.crm.system.service.ITfFCustGroupMService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TfFCustGroupMService implements ITfFCustGroupMService {
    
    @Autowired
    private TfFCustGroupMMapper tfFCustGroupMMapper;
    
    @Override
    public TfFCustGroupMDo selectByGroupId(String groupId) {
        LambdaQueryWrapper<TfFCustGroupMDo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TfFCustGroupMDo::getGroupId, groupId);
        List<TfFCustGroupMDo> list = tfFCustGroupMMapper.selectList(queryWrapper);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }
}