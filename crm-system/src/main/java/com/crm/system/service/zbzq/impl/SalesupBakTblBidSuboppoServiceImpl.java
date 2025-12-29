package com.crm.system.service.zbzq.impl;

import cn.hutool.core.util.ObjectUtil;
import com.crm.system.mapper.zbzq.SalesupBakTblBidSuboppoMapper;
import com.crm.system.service.zbzq.ISalesupBakTblBidSuboppoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SalesupBakTblBidSuboppoServiceImpl implements ISalesupBakTblBidSuboppoService {
    @Resource
    private SalesupBakTblBidSuboppoMapper salesupBakTblBidSuboppoMapper;


    @Override
    public List<Long> selectSignedOppoData(List<Long> ids) {
        if (ObjectUtil.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<String> oppoIds = ids.stream().map(String::valueOf).collect(Collectors.toList());
        return salesupBakTblBidSuboppoMapper.selectSignedOppoData(oppoIds);
    }
}
