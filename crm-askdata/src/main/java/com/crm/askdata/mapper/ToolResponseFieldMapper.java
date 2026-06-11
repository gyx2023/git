package com.crm.askdata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.askdata.entity.ToolResponseField;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工具响应字段 Mapper
 *
 * @author crm
 */
@Mapper
public interface ToolResponseFieldMapper extends BaseMapper<ToolResponseField> {

    /**
     * 根据工具ID查询响应字段映射列表
     *
     * @param toolId 工具ID
     * @return 响应字段列表（按排序号排序）
     */
    @Select("SELECT * FROM askdata_tool_response_field WHERE tool_id = #{toolId} ORDER BY sort_order ASC")
    List<ToolResponseField> selectByToolId(Long toolId);
}
