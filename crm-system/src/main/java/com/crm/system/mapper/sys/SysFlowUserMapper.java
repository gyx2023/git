package com.crm.system.mapper.sys;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 流程节点处理人配置（sys_flow_user，老系统同步表）Mapper接口
 *
 * @author crm
 */
@Mapper
public interface SysFlowUserMapper {

    /**
     * 按流程/节点/候选人归属区域查询候选处理人
     * <p>
     * 对齐老框架 SysFlowUserService.getNextUserListByNodeAndUserArea：
     * 按候选人自身归属区域（sys_org.area_id）过滤，不过滤 sys_flow_user.area_id（老口径）。
     * </p>
     *
     * @param areaIds 候选人归属区域ID集合（调用方保证非空）
     * @return [{userId, userName}]
     */
    List<Map<String, Object>> selectHandlersByProcessAndAreas(@Param("processKey") String processKey,
                                                              @Param("taskDefKey") String taskDefKey,
                                                              @Param("areaIds") List<Long> areaIds);
}
