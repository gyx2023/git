package com.crm.system.service.statistics;

import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.dto.SignStatisticsReqDTO;
import com.crm.common.core.domain.dto.SignStatisticsRespDTO;
import com.crm.common.core.domain.dto.SignStatisticsUserRespDTO;
import com.crm.common.core.page.TableDataInfo;

import java.util.List;

public interface StatisticsSignService {

    TableDataInfo<SignStatisticsRespDTO> selectPageList(SignStatisticsReqDTO reqDTO, PageQuery pageQuery);

    Boolean isLastOrg(String deptId);

    TableDataInfo<SignStatisticsUserRespDTO> selectUserPageList(SignStatisticsReqDTO reqDTO, PageQuery pageQuery);


    List<SignStatisticsRespDTO> selectOrgList(SignStatisticsReqDTO reqDTO);

    List<SignStatisticsUserRespDTO> selectUserList(SignStatisticsReqDTO reqDTO);
}
