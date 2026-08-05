package com.crm.system.mapper.accManager;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.accManager.WaringListTable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 离职人员预警清单表 Mapper接口
 *
 * @author lin_Yao
 * @date 2025/3/13
 */
@Mapper
public interface WaringListTableMapper extends BaseMapperPlus<WaringListTableMapper, WaringListTable, WaringListTable> {

    /**
     * 根据预警类型删除数据
     *
     * @param waringType 预警类型: 1-离职人员, 2-长期未登录
     * @return 删除条数
     */
    int deleteByWaringType(@Param("waringType") String waringType);
}
