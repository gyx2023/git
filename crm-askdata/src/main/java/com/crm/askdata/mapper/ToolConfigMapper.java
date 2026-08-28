package com.crm.askdata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.askdata.entity.ToolConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工具配置 Mapper
 *
 * @author crm
 */
@Mapper
public interface ToolConfigMapper extends BaseMapper<ToolConfig> {

    /**
     * 根据工具名查询配置
     *
     * @param toolName 工具名称
     * @return 工具配置
     */
    @Select("SELECT * FROM askdata_tool_config WHERE tool_name = #{toolName}")
    ToolConfig selectByToolName(String toolName);

    /**
     * 查询所有启用的工具配置
     *
     * @return 工具配置列表
     */
    @Select("SELECT * FROM askdata_tool_config WHERE is_enabled = TRUE ORDER BY id")
    List<ToolConfig> selectEnabledTools();

    /**
     * 查询指定页面可用的启用工具配置，page_codes 必须与 pageCode 精确相等。
     *
     * @param pageCode 页面编码
     * @return 工具配置列表
     */
    @Select("SELECT * FROM askdata_tool_config "
        + "WHERE is_enabled = TRUE "
        + "AND page_codes = #{pageCode} "
        + "ORDER BY id")
    List<ToolConfig> selectEnabledToolsByPageCode(@Param("pageCode") String pageCode);
}
