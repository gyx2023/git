package com.crm.system.service.impl;

import com.crm.system.domain.SysLog;
import com.crm.system.mapper.SysLogMapper;
import com.crm.system.service.ISysLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class SysLogServiceImpl implements ISysLogService {
    @Resource
    private SysLogMapper sysLogMapper;

    @Override
    public void saveLog(SysLog log) {
        sysLogMapper.insert(log);
    }
}
