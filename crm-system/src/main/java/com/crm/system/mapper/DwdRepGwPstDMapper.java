package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.system.domain.DwdRepGwPstD;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 经分人员职位表Mapper接口
 */
@Mapper
public interface DwdRepGwPstDMapper extends BaseMapper<DwdRepGwPstD> {

    /**
     * 从IOB数据库查询人员职位列表
     */
    @DS("slave_2")
    List<Map<String, Object>> selectDwdRepGwPstListFromIob();

    /**
     * 批量插入人员职位数据
     */
    int insertBatch(@Param("list") List<DwdRepGwPstD> list);
}
