package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.system.domain.RepCustContractMillSumM;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 政企运营平台合同收入报表Mapper接口
 *
 * @author wangjl
 * @date 2021/8/31 11:24
 */
@Mapper
public interface RepCustContractMillSumMMapper extends BaseMapper<RepCustContractMillSumM> {

    /**
     * 批量插入数据
     *
     * @param list 待插入的数据列表
     */
    void insertBatch(@Param("list") List<RepCustContractMillSumM> list);
}
