package com.crm.system.mapper.cust;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.CustNecRelationDo;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@DS("slave_1")
public interface CustNecRelationMapper extends BaseMapperPlus<CustNecRelationMapper, CustNecRelationDo, CustNecRelationDo> {

    /**
     * 实体客户id与自然客户id对应关系
     */
    @Select("select ctnr.nature_cust_id, ctnr.PROVINCE_CUST_ID from cust_t_nec_relation ctnr where ctnr.state_code = 0 and ctnr.province_code=36")
    List<CustNecRelationDo> getCustTNecRelationList();

}
