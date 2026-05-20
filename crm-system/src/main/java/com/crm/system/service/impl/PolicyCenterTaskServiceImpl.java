package com.crm.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.system.domain.PolicyCenterTask;
import com.crm.system.mapper.PolicyCenterTaskMapper;
import com.crm.system.service.PolicyCenterTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 策略中心任务Service实现
 */
@Slf4j
@Service
public class PolicyCenterTaskServiceImpl extends ServiceImpl<PolicyCenterTaskMapper, PolicyCenterTask>
        implements PolicyCenterTaskService {

    @Override
    public List<PolicyCenterTask> getNotSyncPolicyTask() {
        return baseMapper.selectNotSyncTasks();
    }
}
