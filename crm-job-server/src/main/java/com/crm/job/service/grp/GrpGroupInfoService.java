package com.crm.job.service.grp;

import com.crm.system.domain.dao.GrpGroupInfoDo;
import java.util.List;

/**
 * 集团信息服务接口
 * 用于处理grp_group_info表的相关操作
 *
 * @author crm
 */
public interface GrpGroupInfoService {

    /**
     * 批量插入集团信息
     *
     * @param groupInfoList 集团信息列表
     * @return 插入成功的数量
     */
    int batchInsert(List<GrpGroupInfoDo> groupInfoList);

    /**
     * 清空grp_group_info表数据
     *
     * @return 删除的记录数
     */
    int truncateTable();

    /**
     * 根据集团ID更新集团信息
     *
     * @param groupInfo 集团信息
     * @return 更新成功的数量
     */
    int updateByGrpId(GrpGroupInfoDo groupInfo);

    /**
     * 批量更新集团信息（如果存在则更新，不存在则插入）
     *
     * @param groupInfoList 集团信息列表
     * @return 处理成功的数量
     */
    int batchUpsert(List<GrpGroupInfoDo> groupInfoList);

    Long selectTotalCount ();
}
