package com.crm.system.mapper.topp;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.crm.common.core.domain.dto.SignOppoFileExportDTO;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.topp.ToppOpportunityDo;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author crm
 * @date 2025-12-02
 */
@Mapper
public interface ToppOpportunityMapper extends BaseMapperPlus<ToppOpportunityMapper, ToppOpportunityDo, ToppOpportunityVo> {
    @InterceptorIgnore(dataPermission = "true")
    List<SignOppoFileExportDTO> selectToppOpportunity();
}
