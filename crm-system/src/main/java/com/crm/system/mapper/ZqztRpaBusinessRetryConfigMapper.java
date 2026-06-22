package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.system.domain.ZqztRpaBusinessRetryConfigDo;

/**
 * RPA业务重试配置Mapper接口
 *
 * @author crm
 */
public interface ZqztRpaBusinessRetryConfigMapper extends BaseMapper<ZqztRpaBusinessRetryConfigDo> {

    /**
     * 根据 bizCode 查询重试配置
     */
    ZqztRpaBusinessRetryConfigDo selectByBizCode(String bizCode);
}
