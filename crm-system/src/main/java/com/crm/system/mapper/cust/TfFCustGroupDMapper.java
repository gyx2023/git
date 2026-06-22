package com.crm.system.mapper.cust;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.TfFCustGroupDDo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 集团实体客户增量信息Mapper接口
 *
 * @author crm
 */
public interface TfFCustGroupDMapper extends BaseMapperPlus<TfFCustGroupDMapper, TfFCustGroupDDo, TfFCustGroupDDo> {

    List<TfFCustGroupDDo> getTfFCustGroupDList(@Param("createDate") String createDate);
}
