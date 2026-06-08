package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.system.domain.dao.Pending;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 待办Mapper接口
 */
@Mapper
public interface PendingMapper extends BaseMapper<Pending> {

    /**
     * 查询待推送的待办数据
     */
    List<Pending> selectPendingList();

    /**
     * 批量更新失败次数
     */
    int updateFailTimes(Map<String, Object> params);
}
