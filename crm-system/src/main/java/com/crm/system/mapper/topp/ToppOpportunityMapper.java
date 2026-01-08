package com.crm.system.mapper.topp;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.crm.common.core.domain.dto.SignOppoFileExportDTO;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.topp.ToppOpportunityDo;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.StatementType;

import java.time.LocalDateTime;
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

    @Select("{call oppo_state_life_cycle(#{oppId, mode=IN})}")
    @Options(statementType = StatementType.CALLABLE)
    @Results({ // 若实体属性与数据库列名一致，可省略 @Results 映射
        @Result(property = "oppoState", column = "oppoState")
    })
    String callGetOppoState(@Param("oppId") Long oppId);


    List<ToppOpportunityDo> selectOpportunityUnbound(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
