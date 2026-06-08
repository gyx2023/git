package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.dao.DwdRepSysPersonD;
import com.crm.system.domain.vo.DwdRepSysPersonDVo;
import com.crm.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author crm
 * @date 2025-11-24
 */
public interface DwdRepSysPersonDMapper extends BaseMapperPlus<DwdRepSysPersonDMapper, DwdRepSysPersonD, DwdRepSysPersonDVo> {

    /**
     * 批量根据姓名和电话查询人员地区信息
     * @param personInfos 人员信息列表 [{fullName: "张三", mobile: "13800138000"}, ...]
     * @return 人员地区信息列表
     */
    List<DwdRepSysPersonDVo> selectAreaAndCounty(@Param("personInfos") List<Map<String, String>> personInfos);

    /**
     * 清空表数据
     */
    void truncate();

    /**
     * 从IOB数据库分页查询人员信息列表(在职标志为0)
     */
    @DS("slave_2")
    List<Map<String, Object>> selectDwdRepSysPersonListFromIob(@Param("startRow") Integer startRow, @Param("endRow") Integer endRow);

    /**
     * 从IOB数据库查询人员总数(在职标志为0)
     */
    @DS("slave_2")
    int selectPersonCountFromIob();

    /**
     * 批量插入人员信息数据
     */
    int insertBatch(@Param("list") List<DwdRepSysPersonD> list);
}
