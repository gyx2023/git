package com.crm.system.service.impl.statistics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.system.domain.dao.sys.SysArea;
import com.crm.system.domain.dto.SupportCityStatsDTO;
import com.crm.system.domain.vo.SupportCityDetailVO;
import com.crm.system.domain.vo.SupportCityStatsVO;
import com.crm.system.mapper.sys.SysAreaMapper;
import com.crm.system.mapper.statistics.SupportCityStatsMapper;
import com.crm.system.service.statistics.ISupportCityStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 支撑情况地市统计Service实现
 *
 * @author crm
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SupportCityStatsServiceImpl implements ISupportCityStatsService {

    private final SupportCityStatsMapper supportCityStatsMapper;
    private final SysAreaMapper sysAreaMapper;

    /** 浙江省编码前缀 */
    private static final String PROVINCE_CODE_PREFIX = "33";

    @Override
    public List<SupportCityStatsVO> selectSupportCityStatsList(SupportCityStatsDTO dto) {
        String areaId = dto.getAreaId();
        String startDate = dto.getStartDate();
        String endDate = dto.getEndDate();

        SysArea area = sysAreaMapper.selectById(Long.valueOf(areaId));
        if (area == null) {
            log.warn("区域不存在: {}", areaId);
            return new ArrayList<>();
        }

        String type = area.getType();
        List<SupportCityStatsVO> resultList = new ArrayList<>();

        if ("一级".equals(type)) {
            // 省分用户：查询省本级 + 各地市 + 总计
            // 省本级
            queryAndAdd(resultList, areaId, area.getName(), true, startDate, endDate);
            // 各地市
            List<SysArea> cities = getChildren(areaId);
            for (SysArea city : cities) {
                String cityPrefix = String.valueOf(city.getId()).substring(0, 4);
                queryAndAdd(resultList, cityPrefix, city.getName(), false, startDate, endDate);
            }
            // 总计
            queryAndAdd(resultList, PROVINCE_CODE_PREFIX, "总计", false, startDate, endDate);

        } else if ("二级".equals(type)) {
            // 市分用户：查询市本级 + 各区县 + 总计
            String cityPrefix = areaId.substring(0, 4);
            // 市本级
            queryAndAdd(resultList, areaId, area.getName(), true, startDate, endDate);
            // 各区县
            List<SysArea> counties = getChildren(areaId);
            for (SysArea county : counties) {
                queryAndAdd(resultList, String.valueOf(county.getId()), county.getName(), true, startDate, endDate);
            }
            // 总计
            queryAndAdd(resultList, cityPrefix, "总计", false, startDate, endDate);

        } else {
            // 区县用户：查询本区县 + 总计
            queryAndAdd(resultList, areaId, area.getName(), true, startDate, endDate);
            queryAndAdd(resultList, areaId, "总计", true, startDate, endDate);
        }

        return resultList;
    }

    @Override
    public List<SupportCityDetailVO> selectSupportCityDetailList(String areaId, String startDate, String endDate) {
        boolean exactMatch = areaId.length() > 4;
        String prefix = exactMatch ? areaId : areaId;
        return supportCityStatsMapper.selectDetailByAreaPrefix(prefix, exactMatch, startDate, endDate);
    }

    @Override
    public List<SupportCityStatsVO> exportSupportCityStatsList(SupportCityStatsDTO dto) {
        return selectSupportCityStatsList(dto);
    }

    /**
     * 查询单个区域统计数据并添加到结果列表
     */
    private void queryAndAdd(List<SupportCityStatsVO> resultList, String areaPrefix,
                              String areaName, boolean exactMatch,
                              String startDate, String endDate) {
        Map<String, Object> stats = supportCityStatsMapper.selectStatsByAreaPrefix(
                areaPrefix, exactMatch, startDate, endDate);

        SupportCityStatsVO vo = new SupportCityStatsVO();
        vo.setAreaId(areaPrefix);
        vo.setAreaName(areaName);
        if (stats != null) {
            Object taskCount = stats.get("taskCount");
            Object oppoCount = stats.get("oppoCount");
            vo.setTaskCount(taskCount != null ? Integer.valueOf(taskCount.toString()) : 0);
            vo.setOppoCount(oppoCount != null ? Integer.valueOf(oppoCount.toString()) : 0);
        } else {
            vo.setTaskCount(0);
            vo.setOppoCount(0);
        }
        resultList.add(vo);
    }

    /**
     * 使用 LambdaQueryWrapper 获取指定区域的子级区域列表
     */
    private List<SysArea> getChildren(String parentId) {
        LambdaQueryWrapper<SysArea> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysArea::getParent, Long.valueOf(parentId))
               .orderByAsc(SysArea::getId);
        return sysAreaMapper.selectList(wrapper);
    }

}
