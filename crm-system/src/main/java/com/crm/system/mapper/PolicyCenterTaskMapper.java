package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.system.domain.PolicyCenterTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 策略中心任务Mapper接口
 */
@Mapper
public interface PolicyCenterTaskMapper extends BaseMapper<PolicyCenterTask> {

    /**
     * 根据任务编码查询任务
     */
    PolicyCenterTask selectByTaskCode(@Param("taskCode") String taskCode);

    /**
     * 查询未同步的任务列表(status=0)
     */
    List<PolicyCenterTask> selectNotSyncTasks();

    /**
     * 根据客户经理ID查询已拜访任务列表
     */
    List<PolicyCenterTask> selectVisitOverList(@Param("mangerId") Integer mangerId, 
                                                @Param("visitResult") String visitResult,
                                                @Param("startDate") String startDate,
                                                @Param("endDate") String endDate);

    /**
     * 根据客户经理ID查询未拜访任务列表
     */
    List<PolicyCenterTask> selectWaitVisitList(@Param("mangerId") Integer mangerId,
                                                @Param("startDate") String startDate,
                                                @Param("endDate") String endDate);

    /**
     * 查询客户经理拜访任务数量(未反馈和已反馈)
     */
    Object[] selectMangerVisitNum(@Param("mangerId") Integer mangerId);

    /**
     * 根据ID更新visit_result为3
     */
    int updateVisitResultById(@Param("id") Integer id);
}
