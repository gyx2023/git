package com.crm.askdata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.askdata.entity.ToolParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工具参数 Mapper
 *
 * @author crm
 */
@Mapper
public interface ToolParamMapper extends BaseMapper<ToolParam> {

    /**
     * 根据工具ID查询参数列表
     *
     * @param toolId 工具ID
     * @return 参数列表（按排序号排序）
     */
    @Select("SELECT * FROM askdata_tool_param WHERE tool_id = #{toolId} ORDER BY sort_order ASC")
    List<ToolParam> selectByToolId(Long toolId);
}
