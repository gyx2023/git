package com.crm.system.mapper.cust;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.cust.CustTNecRelationDo;
import org.apache.ibatis.annotations.Delete;

/**
 * 实体客户id与自然客户id对应关系同步Mapper接口
 *
 * @author crm
 */
public interface CustTNecRelationMapper extends BaseMapperPlus<CustTNecRelationMapper, CustTNecRelationDo, CustTNecRelationDo> {

    /**
     * 清空cust_t_nec_relation表（使用TRUNCATE）
     * 用于数据同步前的表清空操作
     */
    @Delete("truncate table cust_t_nec_relation")
    void truncateTable();

}
