package com.crm.system.mapper.tcomm;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.tcomm.TcommTaskDo;
import com.crm.system.domain.vo.tcomm.TcommTaskVo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 待办任务Mapper接口
 *
 * @author crm
 * @date 2025-12-02
 */
@Mapper
public interface TcommTaskMapper extends BaseMapperPlus<TcommTaskMapper, TcommTaskDo, TcommTaskVo> {

}
