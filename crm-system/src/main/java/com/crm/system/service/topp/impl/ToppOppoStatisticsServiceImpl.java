package com.crm.system.service.topp.impl;

import com.crm.system.domain.topp.ToppOppoStatistics;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.mapper.topp.ToppOppoStatisticsMapper;
import com.crm.system.service.ISysAreaService;
import com.crm.system.service.topp.IToppOppoStatisticsService;
import com.crm.system.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 商机统计Service业务层处理
 *
 * @author crm
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ToppOppoStatisticsServiceImpl implements IToppOppoStatisticsService {

    private final ToppOppoStatisticsMapper baseMapper;
    private final ISysAreaService sysAreaService;

    private static final long SHENG_AREA_ID = Constants.SHENG_AREA_ID;
    private static final ThreadLocal<SimpleDateFormat> DATE_FMT = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    @Override
    public boolean isTodayStatsDone(int type, String areaType) {
        String today = getToday();
        ToppOppoStatistics stats = baseMapper.selectTodayStats(String.valueOf(type), areaType, null, today);
        if (stats == null || stats.getData() == null || "{}".equals(stats.getData())) {
            return false;
        }
        return true;
    }

    @Override
    public void saveOrUpdate(ToppOppoStatistics oppoStatistics) {
        if (oppoStatistics.getId() != null) {
            baseMapper.updateById(oppoStatistics);
        } else {
            baseMapper.insert(oppoStatistics);
        }
    }

    // ============ 简单统计查询 ============

    @Override
    public List<Map<String, Object>> getSimpleStatsByIndividualArea(String type) {
        switch (type) {
            case "1":
                return baseMapper.selectReserveByIndividualArea(getStartDate2(), getLastDate2());
            case "2":
                return baseMapper.selectMonthCountByIndividualArea(getStartDate(1));
            case "3":
                return baseMapper.selectSignCountByIndividualArea(getStartDate(1));
            default:
                return Collections.emptyList();
        }
    }

    @Override
    public List<Map<String, Object>> getSimpleStatsByFullArea(String type) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<SysAreaVo> cities = sysAreaService.getZhejiangCities();

        // 先查全省汇总
        String areaPrefix = String.valueOf(SHENG_AREA_ID).substring(0, 2);
        List<Map<String, Object>> provData = querySimpleStatsByType(type, SHENG_AREA_ID, "全省", areaPrefix);
        result.addAll(provData);

        // 再查各地市汇总
        for (SysAreaVo city : cities) {
            String cityPrefix = String.valueOf(city.getId()).substring(0, 4);
            List<Map<String, Object>> cityData = querySimpleStatsByType(type, city.getId(), city.getName(), cityPrefix);
            result.addAll(cityData);
        }
        return result;
    }

    private List<Map<String, Object>> querySimpleStatsByType(String type, Long areaId, String areaName, String areaPrefix) {
        switch (type) {
            case "1":
                return baseMapper.selectReserveForArea(areaId, areaName, areaPrefix, getStartDate2(), getLastDate2());
            case "2":
                return baseMapper.selectMonthCountForArea(areaId, areaName, areaPrefix, getStartDate(1));
            case "3":
                return baseMapper.selectSignCountForArea(areaId, areaName, areaPrefix, getStartDate(1));
            default:
                return Collections.emptyList();
        }
    }

    // ============ 图表统计查询 ============

    @Override
    public List<Map<String, Object>> getIncludeTypeCount(int timeLimite, int areaType) {
        String startDate = getStartDate(timeLimite);
        List<Map<String, Object>> result = new ArrayList<>();

        if (areaType != 1) {
            // 市本模式：areaId=null，SQL 返回 c.areaId（商机实际 area.id_）
            result.addAll(baseMapper.selectIncludeTypeByIndividualArea(startDate, String.valueOf(areaType), null));
        } else {
            // 全省模式：先查全省汇总（areaId=330000, LIKE '33%'），再查各地市
            // 1. 全省汇总：areaId=330000，prefix="33"
            String shengPrefix = String.valueOf(SHENG_AREA_ID).substring(0, 2);
            result.addAll(baseMapper.selectIncludeTypeByIndividualArea(startDate, shengPrefix, SHENG_AREA_ID));

            // 2. 各地市汇总：areaId=330100, 330200...，prefix='3301', '3302'...
            List<SysAreaVo> areas = sysAreaService.getZhejiangCities();
            for (SysAreaVo area : areas) {
                Long areaId = area.getId();
                String areaIdStr = String.valueOf(areaId);
                String prefix = areaIdStr.substring(0, 4);
                result.addAll(baseMapper.selectIncludeTypeByIndividualArea(startDate, prefix, areaId));
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getMonthSignData(int areaType) {
        String startDate = getStartDate2();
        String lastDate = getLastDate2();
        if (areaType != 1) {
            return baseMapper.selectMonthSignByIndividualArea(startDate, lastDate, String.valueOf(areaType));
        } else {
            List<Map<String, Object>> result = new ArrayList<>();
            List<Long> cityIds = getCityAreaIds();
            for (Long cityId : cityIds) {
                String prefix;
                if (cityId != null && cityId.longValue() == SHENG_AREA_ID) {
                    prefix = String.valueOf(cityId).substring(0, 2);
                } else {
                    prefix = String.valueOf(cityId).substring(0, 4);
                }
                List<Map<String, Object>> data = baseMapper.selectMonthSignForArea(startDate, lastDate, cityId, prefix);
                result.addAll(data);
            }
            return result;
        }
    }

    @Override
    public List<Map<String, Object>> getMonthOppoData(int areaType) {
        String startDate = getStartDate2();
        String lastDate = getLastDate2();
        if (areaType != 1) {
            return baseMapper.selectMonthOppoByIndividualArea(startDate, lastDate, String.valueOf(areaType));
        } else {
            List<Map<String, Object>> result = new ArrayList<>();
            List<Long> cityIds = getCityAreaIds();
            for (Long cityId : cityIds) {
                String prefix;
                if (cityId != null && cityId.longValue() == SHENG_AREA_ID) {
                    prefix = String.valueOf(cityId).substring(0, 2);
                } else {
                    prefix = String.valueOf(cityId).substring(0, 4);
                }
                List<Map<String, Object>> data = baseMapper.selectMonthOppoForArea(startDate, lastDate, cityId, prefix);
                result.addAll(data);
            }
            return result;
        }
    }

    // ============ 辅助方法 ============

    @Override
    public List<Long> getHaveDataAreaIds(int timeLimite, int areaType) {
        String startDate = getStartDate(timeLimite);
        return baseMapper.selectHaveDataAreaIds(startDate, String.valueOf(areaType));
    }

    @Override
    public List<Long> getHaveOppoDataAreaIds(int areaType) {
        String startDate = getStartDate2();
        String lastDate = getLastDate2();
        return baseMapper.selectHaveOppoDataAreaIds(startDate, lastDate, String.valueOf(areaType));
    }

    @Override
    public List<Long> getCityAreaIds() {
        List<Long> list = new ArrayList<>();
        list.add(SHENG_AREA_ID);
        List<Long> cityIds = baseMapper.selectCityAreaIds(SHENG_AREA_ID);
        list.addAll(cityIds);
        return list;
    }

    // ============ 日期工具方法 ============

    private String getToday() {
        return DATE_FMT.get().format(new Date()) + " 00:00:00";
    }

    /**
     * 获取起始日期（往前推N个月的第一天）
     */
    private String getStartDate(int monthsBefore) {
        Calendar cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 1 - monthsBefore);
        cale.set(Calendar.DAY_OF_MONTH, 1);
        return DATE_FMT.get().format(cale.getTime()) + " 00:00:00";
    }

    /**
     * 获取本年第一天
     */
    private String getStartDate2() {
        Calendar cale = Calendar.getInstance();
        cale.set(Calendar.MONTH, 0);
        cale.set(Calendar.DAY_OF_MONTH, 1);
        return DATE_FMT.get().format(cale.getTime()) + " 00:00:00";
    }

    /**
     * 获取本年最后一天
     */
    private String getLastDate2() {
        Calendar cale = Calendar.getInstance();
        cale.set(Calendar.MONTH, 11);
        cale.set(Calendar.DAY_OF_MONTH, 31);
        return DATE_FMT.get().format(cale.getTime()) + " 00:00:00";
    }
}
