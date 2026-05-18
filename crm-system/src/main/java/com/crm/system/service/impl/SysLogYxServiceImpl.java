package com.crm.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.system.domain.SysLogYx;
import com.crm.system.mapper.SysLogYxMapper;
import com.crm.system.service.SysLogYxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 亚信同步日志Service实现
 */
@Slf4j
@Service
public class SysLogYxServiceImpl extends ServiceImpl<SysLogYxMapper, SysLogYx> implements SysLogYxService {
}
