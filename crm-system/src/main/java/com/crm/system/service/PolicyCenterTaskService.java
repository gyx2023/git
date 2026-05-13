package com.crm.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.domain.PolicyCenterTask;

import java.util.List;

/**
 * 策略中心任务Service接口
 */
public interface PolicyCenterTaskService extends IService<PolicyCenterTask> {

    /**
     * 根据任务编码查询任务
     */
    PolicyCenterTask getTaskByTaskCode(String taskCode);

    /**
     * 查询未同步的任务列表
     */
    List<PolicyCenterTask> getNotSyncPolicyTask();

    /**
     * 查询客户经理已拜访任务列表
     */
    List<PolicyCenterTask> getVisitOverListByManger(Integer mangerId, String visitResult, 
                                                     Integer pageNo, Integer pageSize, 
                                                     String startDate, String endDate);

    /**
     * 查询客户经理未拜访任务列表
     */
    List<PolicyCenterTask> getWaitVisitListByManger(Integer mangerId, Integer pageNo, Integer pageSize, 
                                                     String startDate, String endDate);

    /**
     * 查询客户经理拜访任务数量
     */
    Object[] getMangerVisitNum(Integer mangerId);

    /**
     * 根据ID更新visit_result为3
     */
    Integer updateStatusById(Integer id);
}
