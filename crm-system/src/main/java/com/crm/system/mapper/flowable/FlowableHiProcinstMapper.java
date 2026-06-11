package com.crm.system.mapper.flowable;

import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * Flowable历史流程实例表Mapper
 *
 * @author crm
 */
public interface FlowableHiProcinstMapper {

    /**
     * 根据流程实例ID查询流程结束时间
     */
    Date selectEndTimeByProcInstId(@Param("procInstId") String procInstId);
}
