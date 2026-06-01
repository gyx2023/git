package com.crm.system.mapper;

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
}
