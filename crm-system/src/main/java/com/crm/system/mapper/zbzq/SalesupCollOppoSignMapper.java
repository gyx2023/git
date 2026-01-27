package com.crm.system.mapper.zbzq;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.zbzq.SalesupCollOppoSignDo;
import com.crm.system.domain.vo.zbzq.SalesupCollOppoSignVo;
import com.crm.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * VIEWMapper接口
 *
 * @author crm
 * @date 2025-12-16
 */
@DS("slave_1")
public interface SalesupCollOppoSignMapper extends BaseMapperPlus<SalesupCollOppoSignMapper, SalesupCollOppoSignDo, SalesupCollOppoSignVo> {

    @Select("SELECT s.*,p.oppo_number FROM salesup_coll_oppo_sign s  " +
        "inner join salesup_coll_oppo_pools p on s.oppo_id =p.oppo_id " +
        "WHERE s.create_date >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
        "AND p.oppo_number LIKE CONCAT('%', #{prefix}, '%')")
    List<SalesupCollOppoSignVo> selectContractSignData(@Param("days") int days, @Param("prefix") String prefix);

}
