package com.crm.system.service.statistics;

import com.crm.common.core.domain.PageQuery;
import com.crm.system.domain.dto.ContractUserReqDTO;
import com.crm.system.domain.dto.ContractUserRespDTO;
import com.crm.common.core.page.TableDataInfo;

import java.util.List;

public interface ContractUserService {

    TableDataInfo<ContractUserRespDTO> selectPageList(ContractUserReqDTO reqDTO, PageQuery pageQuery);


    List<ContractUserRespDTO> getContractUserRespDTOS(ContractUserReqDTO reqDTO);
}
