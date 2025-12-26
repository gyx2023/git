package com.crm.system.service.zbzq.impl;

import com.crm.system.mapper.zbzq.SalesupBakTblBidSuboppoMapper;
import com.crm.system.service.zbzq.ISalesupBakTblBidSuboppoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class SalesupBakTblBidSuboppoServiceImpl implements ISalesupBakTblBidSuboppoService {
    @Resource
    private SalesupBakTblBidSuboppoMapper salesupBakTblBidSuboppoMapper;


    @Override
    public List<Long> selectSignedOppoData(List<Long> ids) {
        List<Long> list = salesupBakTblBidSuboppoMapper.selectSignedOppoData(ids);
        return list;
    }
}
