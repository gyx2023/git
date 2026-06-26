package com.crm.system.mapper.zbzq;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.dao.sync.CustNatureCustomerDeveloper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 自然客户开发者源库 Mapper（读 slave_1 源库 zq_user_36）
 *
 * @author crm
 * @date 2026-06-18
 */
@DS("slave_1")
public interface CustNatureCustomerDeveloperSourceMapper {

    /**
     * 统计源表中符合条件的记录总数
     */
    int countByCondition();

    /**
     * 分页查询源表中符合条件的记录
     *
     * @param offset 偏移量
     * @param limit  每页条数
     */
    List<CustNatureCustomerDeveloper> selectByPage(@Param("offset") int offset, @Param("limit") int limit);
}
