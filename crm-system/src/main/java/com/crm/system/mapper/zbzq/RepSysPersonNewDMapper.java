package com.crm.system.mapper.zbzq;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.dto.GrpGroupImportDTO;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.zbzq.RepSysPersonNewDDo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@DS("slave_2")
public interface RepSysPersonNewDMapper extends BaseMapperPlus<RepSysPersonNewDMapper, RepSysPersonNewDDo,RepSysPersonNewDDo> {

    Integer getDeptLevel(String oaUserName);

    List<GrpGroupImportDTO> selectGrpDto();

    Page<RepSysPersonNewDDo> selectProvincialManagers(Page<RepSysPersonNewDDo> page, @Param("keyword") String keyword);
}
