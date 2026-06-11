package com.crm.system.service.zbzq.impl;

import cn.hutool.core.util.ObjectUtil;
import com.crm.system.domain.dto.SalesupBakTblBidSuboppoDTO;
import com.crm.system.mapper.zbzq.SalesupBakTblBidSuboppoMapper;
import com.crm.system.service.zbzq.ISalesupBakTblBidSuboppoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SalesupBakTblBidSuboppoServiceImpl implements ISalesupBakTblBidSuboppoService {
    @Resource
    private SalesupBakTblBidSuboppoMapper salesupBakTblBidSuboppoMapper;


    @Override
    public List<String> selectSignedOppoData(List<String> oppoCodes) {
        if (ObjectUtil.isEmpty(oppoCodes)) {
            return new ArrayList<>();
        }
        return salesupBakTblBidSuboppoMapper.selectSignedOppoData(oppoCodes);
    }

    @Override
    public List<SalesupBakTblBidSuboppoDTO> querySalesupBidList(String dateTime) {
        return salesupBakTblBidSuboppoMapper.querySalesupBidList(dateTime);
    }
}
