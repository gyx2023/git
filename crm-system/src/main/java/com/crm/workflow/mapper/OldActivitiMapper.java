package com.crm.workflow.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 老系统 Activiti 表查询 Mapper
 * 直连老系统的 ACT_ 表（同库不同表前缀），用于新老系统并行期间查询历史流程数据
 */
@Mapper
public interface OldActivitiMapper {

    // ========== 待办查询 (ACT_RU_TASK) ==========

    /**
     * 查询老系统待办任务列表
     */
    List<Map<String, Object>> selectOldTodoList(@Param("userId") String userId);

    /**
     * 查询老系统待办任务数量
     */
    long selectOldTodoCount(@Param("userId") String userId);

    // ========== 已办查询 (ACT_HI_TASKINST) ==========

    /**
     * 查询老系统已办任务列表（分页）
     */
    List<Map<String, Object>> selectOldFinishedList(@Param("userId") String userId,
                                                    @Param("offset") int offset,
                                                    @Param("size") int size);

    /**
     * 查询老系统已办任务数量
     */
    long selectOldFinishedCount(@Param("userId") String userId);

    // ========== 我发起的流程 (ACT_HI_PROCINST) ==========

    /**
     * 查询老系统我发起的流程列表（分页）
     */
    List<Map<String, Object>> selectOldOwnList(@Param("userId") String userId,
                                                @Param("offset") int offset,
                                                @Param("size") int size);

    /**
     * 查询老系统我发起的流程数量
     */
    long selectOldOwnCount(@Param("userId") String userId);

    // ========== 流程详情 (ACT_HI_PROCINST + ACT_HI_ACTINST + ACT_GE_BYTEARRAY) ==========

    /**
     * 查询老系统流程实例详情
     */
    Map<String, Object> selectOldProcInst(@Param("procInsId") String procInsId);

    /**
     * 查询老系统流程历史活动节点（处理轨迹）
     */
    List<Map<String, Object>> selectOldHistoricActivityInsts(@Param("procInsId") String procInsId);

    /**
     * 查询老系统流程历史任务实例
     */
    List<Map<String, Object>> selectOldHistoricTaskInsts(@Param("procInsId") String procInsId);

    /**
     * 查询老系统流程变量列表
     */
    List<Map<String, Object>> selectOldProcVariables(@Param("procInsId") String procInsId);

    /**
     * 获取老系统BPMN定义XML
     */
    @Select("SELECT gb.NAME_, gb.BYTES_ FROM ACT_GE_BYTEARRAY gb " +
            "LEFT JOIN ACT_RE_PROCDEF pd ON pd.RESOURCE_NAME_ = gb.NAME_ AND gb.DEPLOYMENT_ID_ = pd.DEPLOYMENT_ID_ " +
            "LEFT JOIN ACT_HI_PROCINST hp ON hp.PROC_DEF_ID_ = pd.ID_ " +
            "WHERE hp.PROC_INST_ID_ = #{procInsId} " +
            "UNION " +
            "SELECT gb.NAME_, gb.BYTES_ FROM ACT_GE_BYTEARRAY gb " +
            "LEFT JOIN ACT_RE_PROCDEF pd ON pd.RESOURCE_NAME_ = gb.NAME_ AND gb.ID_ = pd.DEPLOYMENT_ID_ " +
            "LEFT JOIN ACT_HI_PROCINST hp ON hp.PROC_DEF_ID_ = pd.ID_ " +
            "WHERE hp.PROC_INST_ID_ = #{procInsId}")
    List<Map<String, Object>> selectOldBpmnResources(@Param("procInsId") String procInsId);

    /**
     * 根据流程定义ID获取BPMN资源
     */
    @Select("SELECT gb.NAME_, gb.BYTES_ FROM ACT_GE_BYTEARRAY gb " +
            "WHERE gb.DEPLOYMENT_ID_ = #{deploymentId} " +
            "   OR gb.ID_ = #{deploymentId}")
    List<Map<String, Object>> selectOldBpmnResourcesByDeployId(@Param("deploymentId") String deploymentId);

    /**
     * 查询老系统流程定义
     */
    @Select("SELECT ID_, KEY_, NAME_, VERSION_, DEPLOYMENT_ID_, DESCRIPTION_ " +
            "FROM ACT_RE_PROCDEF WHERE KEY_ = #{processKey} ORDER BY VERSION_ DESC LIMIT 1")
    Map<String, Object> selectOldProcDefByKey(@Param("processKey") String processKey);

    /**
     * 查询老系统流程定义 Key
     */
    @Select("SELECT pd.KEY_ FROM ACT_HI_PROCINST hp " +
            "JOIN ACT_RE_PROCDEF pd ON hp.PROC_DEF_ID_ = pd.ID_ " +
            "WHERE hp.PROC_INST_ID_ = #{procInsId}")
    String selectOldProcDefKey(@Param("procInsId") String procInsId);

    /**
     * 查询老系统的流程意见/备注（ACT_HI_COMMENT）
     */
    @Select("SELECT ID_ AS id, TYPE_ AS type, TIME_ AS time, USER_ID_ AS userId, TASK_ID_ AS taskId, PROC_INST_ID_ AS procInstId, MESSAGE_ AS message " +
            "FROM ACT_HI_COMMENT WHERE PROC_INST_ID_ = #{procInsId} ORDER BY TIME_ ASC")
    List<Map<String, Object>> selectOldComments(@Param("procInsId") String procInsId);

    /**
     * 查询老系统流程参与者（ACT_HI_IDENTITYLINK）
     */
    @Select("SELECT ID_, TYPE_, USER_ID_, GROUP_ID_, TASK_ID_, PROC_INST_ID_ " +
            "FROM ACT_HI_IDENTITYLINK WHERE PROC_INST_ID_ = #{procInsId}")
    List<Map<String, Object>> selectOldIdentityLinks(@Param("procInsId") String procInsId);

    /**
     * 按 taskId 查询老系统单个历史任务
     */
    Map<String, Object> selectOldHistoricTaskByTaskId(@Param("taskId") String taskId);

    /**
     * 按 taskId + 变量名查询老系统单个流程变量
     */
    Map<String, Object> selectOldHistoricVariable(@Param("taskId") String taskId,
                                                   @Param("variableName") String variableName);
}
