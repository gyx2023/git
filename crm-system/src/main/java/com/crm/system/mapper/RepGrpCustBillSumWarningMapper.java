package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.system.domain.RepGrpCustBillSumWarning;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 集团客户账单汇总预警Mapper接口
 */
@Mapper
public interface RepGrpCustBillSumWarningMapper extends BaseMapper<RepGrpCustBillSumWarning> {

    /**
     * 批量插入集团客户账单汇总预警数据
     *
     * @param list 数据列表
     * @return 插入条数
     */
    int insertBatch(List<RepGrpCustBillSumWarning> list);
}
