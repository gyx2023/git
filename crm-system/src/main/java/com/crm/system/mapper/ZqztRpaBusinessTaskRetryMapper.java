package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.system.domain.ZqztRpaBusinessTaskRetryDo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * RPA业务重试任务记录Mapper接口
 *
 * @author crm
 */
public interface ZqztRpaBusinessTaskRetryMapper extends BaseMapper<ZqztRpaBusinessTaskRetryDo> {

    /**
     * 查询需要重试的任务 (next_retry_time <= now AND status in (0,1))
     */
    List<ZqztRpaBusinessTaskRetryDo> selectPendingRetryTasks(@Param("now") Date now);

    /**
     * 更新任务状态
     */
    int updateTaskStatus(@Param("id") Long id, @Param("status") Integer status, @Param("retryCount") Integer retryCount,
                         @Param("lastError") String lastError, @Param("nextRetryTime") Date nextRetryTime);
}
