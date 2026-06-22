package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.system.domain.dao.PendingGroupTodo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 集团待办同步Mapper接口
 */
@Mapper
public interface PendingGroupTodoMapper extends BaseMapper<PendingGroupTodo> {

}
