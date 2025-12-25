package com.crm.system.service.statistics;

import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.dto.ContractStatisticsReqDTO;
import com.crm.common.core.domain.dto.ContractStatisticsRespDTO;
import com.crm.common.core.domain.dto.ContractStatisticsUserRespDTO;
import com.crm.common.core.page.TableDataInfo;

import java.util.List;

public interface StatisticsContractService {

    TableDataInfo<ContractStatisticsRespDTO> selectPageList(ContractStatisticsReqDTO reqDTO, PageQuery pageQuery);


    TableDataInfo<ContractStatisticsUserRespDTO> selectUserPageList(ContractStatisticsReqDTO reqDTO, PageQuery pageQuery);


    List<ContractStatisticsRespDTO> selectOrgList(ContractStatisticsReqDTO reqDTO);

    List<ContractStatisticsUserRespDTO> selectUserList(ContractStatisticsReqDTO reqDTO);
}
