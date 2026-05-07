package com.crm.askdata.model;

import com.crm.askdata.entity.ToolConfig;
import com.crm.askdata.entity.ToolParam;
import lombok.Data;

import java.util.List;

/**
 * 工具配置完整对象（包含参数列表）
 *
 * @author crm
 */
@Data
public class ToolConfigWithParams {

    /**
     * 工具配置
     */
    private ToolConfig config;

    /**
     * 参数列表
     */
    private List<ToolParam> params;
}
