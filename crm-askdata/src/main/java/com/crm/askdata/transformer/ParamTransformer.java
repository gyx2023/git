package com.crm.askdata.transformer;

import com.crm.askdata.entity.ToolParam;

/**
 * 参数转换器接口
 * <p>
 * 用于将 AI 提取的参数转换为原接口所需的格式
 *
 * @author crm
 */
public interface ParamTransformer {

    /**
     * 转换参数值
     *
     * @param value    原始值
     * @param paramDef 参数定义
     * @return 转换后的值
     */
    Object transform(Object value, ToolParam paramDef);
}
