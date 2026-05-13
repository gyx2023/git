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
    public PolicyCenterTask getTaskByTaskCode(String taskCode) {
        return baseMapper.selectByTaskCode(taskCode);
    }

    @Override
    public List<PolicyCenterTask> getNotSyncPolicyTask() {
        return baseMapper.selectNotSyncTasks();
    }

    @Override
    public List<PolicyCenterTask> getVisitOverListByManger(Integer mangerId, String visitResult, 
                                                            Integer pageNo, Integer pageSize, 
                                                            String startDate, String endDate) {
        List<PolicyCenterTask> list = baseMapper.selectVisitOverList(mangerId, visitResult, startDate, endDate);
        return listSplit(list, pageNo, pageSize);
    }

    @Override
    public List<PolicyCenterTask> getWaitVisitListByManger(Integer mangerId, Integer pageNo, Integer pageSize, 
                                                            String startDate, String endDate) {
        List<PolicyCenterTask> list = baseMapper.selectWaitVisitList(mangerId, startDate, endDate);
        return listSplit(list, pageNo, pageSize);
    }

    @Override
    public Object[] getMangerVisitNum(Integer mangerId) {
        return baseMapper.selectMangerVisitNum(mangerId);
    }

    @Override
    public Integer updateStatusById(Integer id) {
        return baseMapper.updateVisitResultById(id);
    }

    /**
     * 对list集合进行分页处理
     */
    private List<PolicyCenterTask> listSplit(List<PolicyCenterTask> list, int pageNo, int pageSize) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        int total = list.size();
        int fromIndex = pageSize * (pageNo - 1);
        int toIndex = (pageSize * pageNo) > total ? total : (pageSize * pageNo);
        
        if (fromIndex >= total) {
            return new ArrayList<>();
        }
        
        return list.subList(fromIndex, toIndex);
    }
}
