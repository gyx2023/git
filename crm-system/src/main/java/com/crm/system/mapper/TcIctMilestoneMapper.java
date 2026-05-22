package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.system.domain.TcIctMilestone;
import org.apache.ibatis.annotations.Mapper;

/**
 * 合同里程碑 Mapper 接口
 */
@Mapper
public interface TcIctMilestoneMapper extends BaseMapper<TcIctMilestone> {
    /**
     * 清空合同里程碑表
     */
    void clearMilestoneTable();
}
