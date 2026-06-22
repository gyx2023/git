package com.crm.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.domain.dao.PolicyCenterTask;

import java.util.List;

/**
 * 策略中心任务Service接口
 */
public interface PolicyCenterTaskService extends IService<PolicyCenterTask> {

    /**
     * 查询未同步的任务列表
     */
    List<PolicyCenterTask> getNotSyncPolicyTask();
}
