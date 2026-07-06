package com.crm.system.mapper.zbzq;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.vo.NatureCustVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 从源库查询浙江省自然客户数据（用于同步到省内缓存表）
 *
 * @author crm
 * @date 2026-06-17
 */
@DS("slave_1")
public interface NatureCustSyncQueryMapper {

    /**
     * 统计源表中符合条件的记录总数
     */

    int countByCondition();

    /**
     * 分页查询浙江省（REG_PROVINCE_CODE='36'）自然客户
     *
     * @param offset 偏移量
     * @param limit  每页条数
     */
    List<NatureCustVo> selectByPage(@Param("offset") int offset, @Param("limit") int limit);
}
