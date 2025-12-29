package com.crm.system.service.statistics;

import cn.hutool.core.lang.tree.Tree;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.dto.BiddingListReqDTO;
import com.crm.common.core.domain.dto.BiddingListRespDTO;
import com.crm.common.core.page.TableDataInfo;

import java.util.List;

public interface BiddingListService {

    TableDataInfo<BiddingListRespDTO> selectPageList(BiddingListReqDTO reqDTO, PageQuery pageQuery);

    List<BiddingListRespDTO> getBiddingListRespDTOS(BiddingListReqDTO reqDTO);
    List<Tree<Long>>   querySysDeptTree();
}
