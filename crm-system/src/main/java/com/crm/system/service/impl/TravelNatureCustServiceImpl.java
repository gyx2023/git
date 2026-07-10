package com.crm.system.service.impl;

import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dao.travel.ZqztTravelNatureCust;
import com.crm.system.domain.vo.NatureCustVo;
import com.crm.system.mapper.travel.ZqztTravelNatureCustMapper;
import com.crm.system.service.ITravelNatureCustService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自然客户查询服务实现
 *
 * @author crm
 * @date 2026-06-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TravelNatureCustServiceImpl implements ITravelNatureCustService {

    private final ZqztTravelNatureCustMapper zqztTravelNatureCustMapper;

    @Override
    public List<NatureCustVo> searchNatureCust(String keyword) {
        if (StringUtils.isEmpty(keyword)) {
            return Collections.emptyList();
        }
        List<ZqztTravelNatureCust> list = zqztTravelNatureCustMapper.searchByKeyword(keyword.trim());
        return list.stream().map(item -> {
            NatureCustVo vo = new NatureCustVo();
            vo.setNatureCustId(item.getNatureCustId());
            vo.setNatureCustName(item.getNatureCustName());
            return vo;
        }).collect(Collectors.toList());
    }
}
