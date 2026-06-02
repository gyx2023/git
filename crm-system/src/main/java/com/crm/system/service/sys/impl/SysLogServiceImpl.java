package com.crm.system.service.sys.impl;

import com.crm.system.domain.dao.sys.SysLog;
import com.crm.system.mapper.sys.SysLogMapper;
import com.crm.system.service.sys.ISysLogService;
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
