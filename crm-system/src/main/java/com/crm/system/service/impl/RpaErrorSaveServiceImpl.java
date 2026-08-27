package com.crm.system.service.impl;

import com.crm.system.domain.dao.rpa.RpaErrorSaveDo;
import com.crm.system.mapper.rpa.RpaErrorSaveMapper;
import com.crm.system.service.rpa.IRpaErrorSaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * RPA 错误信息服务实现。
 */
@Service
@RequiredArgsConstructor
public class RpaErrorSaveServiceImpl implements IRpaErrorSaveService {
    @Resource
    private RpaErrorSaveMapper rpaErrorSaveMapper;

    @Override
    public int insertRpaErrorSave(RpaErrorSaveDo rpaErrorSave) {
        return rpaErrorSaveMapper.insert(rpaErrorSave);
    }
}
