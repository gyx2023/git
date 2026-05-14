package com.crm.system.service.brain.impl;

import com.crm.system.mapper.brain.SmartBrainKpiDataMapper;
import com.crm.system.service.brain.SmartBrainKpiDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 智慧大脑指标计算 服务
 *
 * @author ：fupp
 */
@Service
@Slf4j
public class SmartBrainKpiDataServiceImpl implements SmartBrainKpiDataService {

    @Resource
    private SmartBrainKpiDataMapper mapper;

    /**
     * 区域类型 0 省份 1 地市 2县级
     */
    private static final int AREA_TYPE_PROVINCE = 0;
    private static final int AREA_TYPE_CITY = 1;
    private static final int AREA_TYPE_COUNTY = 2;

    /**
     * 账期类型 D 日 M 月 Y 年
     */
    private static final String CYCLE_TYPE_DAY = "D";
    private static final String CYCLE_TYPE_MONTH = "M";
    public static final String YEAR_MONTH_DAY_PATTERN = "yyyyMMdd";

    /**
     * 根据账期类型和账期删除指标数据
     *
     * @param cycleDate 账期
     * @param cycleType 账期类型(D-日,M-月,Y-年)
     * @return 删除记录数
     */
    @Override
    public int deleteKpiData(String cycleDate, String cycleType) {
        return mapper.deleteKpiData(cycleDate, cycleType);
    }

    /**
     * 根据账期查询指标数据
     *
     * @param cycleDate
     */
    @Override
    public List<Map> getKpiDataList(String cycleDate) {
        List<Map> data = mapper.getKpiDataList(cycleDate);
        dealData(data);
        return data;
    }

    private void dealData(List<Map> data) {
        // 格式化数值，实现智能数字格式化
        for (Map row : data) {
            format(row, "kpiValue");
            format(row, "monthVal");
            format(row, "monthRatio");
            format(row, "yearVal");
            format(row, "yearRatio");
            format(row, "kpiValAdd1");
            format(row, "kpiValAdd2");
        }
    }

    private void format(Map<String, Object> map, String field) {
        try {
            Object obj = map.get(field);
            if (obj == null) {
                return;
            }
            BigDecimal num = new BigDecimal(obj.toString());
            map.put(field, formatNumber(num));
        } catch (Exception ignored) {
        }
    }

    /**
     * 智能数字格式化方法
     * 4.00 -> 4
     * 4.10 -> 4.1
     * 4.11 -> 4.11
     */
    private String formatNumber(BigDecimal number) {
        if (number == null) {
            return null;
        }
        double d = number.doubleValue();
        if (d == Math.floor(d)) {
            return String.valueOf((long) d);
        }
        return number.stripTrailingZeros().toPlainString();
    }

    /**
     * 维系预警工单数-省级统计
     *
     * @param cycleDate 统计周期日期（日格式：yyyy-MM-dd，月格式：yyyy-MM）
     * @param cycleType 统计周期类型（D-日，M-月）
     * @param kpiId     指标编码
     * @return
     */
    @Override
    public int buildProvinceKpiInsertSql(String cycleDate, String cycleType, String kpiId) {
        String[] compare = calculateCompareDates(cycleDate, cycleType);
        String today = getDataQuery(cycleDate, cycleType, "today", false, AREA_TYPE_PROVINCE);
        String compare1 = getDataQuery(compare[0], cycleType, "compare1", false, AREA_TYPE_PROVINCE);
        String compare2 = getDataQuery(compare[1], cycleType, "compare2", false, AREA_TYPE_PROVINCE);
        return mapper.buildProvinceKpiInsertSql(
            cycleDate, cycleType, kpiId, today, compare1, compare2
        );
    }

    /**
     * 维系预警工单数-地市级统计
     *
     * @param cycleDate 统计周期日期（日格式：yyyy-MM-dd，月格式：yyyy-MM）
     * @param cycleType 统计周期类型（D-日，M-月）
     * @param kpiId     指标编码
     * @return
     */
    @Override
    public int buildCityKpiInsertSql(String cycleDate, String cycleType, String kpiId) {
        String[] compare = calculateCompareDates(cycleDate, cycleType);
        String today = getDataQuery(cycleDate, cycleType, "today", false, AREA_TYPE_CITY);
        String compare1 = getDataQuery(compare[0], cycleType, "compare1", false, AREA_TYPE_CITY);
        String compare2 = getDataQuery(compare[1], cycleType, "compare2", false, AREA_TYPE_CITY);
        return mapper.buildCityKpiInsertSql(
            cycleDate, cycleType, kpiId, today, compare1, compare2
        );
    }

    /**
     * 维系预警工单数-区县级统计
     *
     * @param cycleDate 统计周期日期（日格式：yyyy-MM-dd，月格式：yyyy-MM）
     * @param cycleType 统计周期类型（D-日，M-月）
     * @param kpiId     指标编码
     * @return
     */
    @Override
    public int buildCountyKpiInsertSql(String cycleDate, String cycleType, String kpiId) {
        String[] compare = calculateCompareDates(cycleDate, cycleType);
        String allToday = getDataQuery(cycleDate, cycleType, "all_today", false, AREA_TYPE_PROVINCE);
        String today = getDataQuery(cycleDate, cycleType, "today", true, AREA_TYPE_PROVINCE);
        String compare1 = getDataQuery(compare[0], cycleType, "compare1", true, AREA_TYPE_PROVINCE);
        String compare2 = getDataQuery(compare[1], cycleType, "compare2", true, AREA_TYPE_PROVINCE);
        return mapper.buildProvinceClosedLoopKpiInsertSql(
            cycleDate, cycleType, kpiId, allToday, today, compare1, compare2
        );
    }

    // -------------------------------------------------------------------------
    // 工具方法：子查询构造
    // -------------------------------------------------------------------------
    private String getDataQuery(String date, String cycleType, String alias, boolean onlyClosed, int level) {
        StringBuilder q = new StringBuilder();
        switch (level) {
            case AREA_TYPE_PROVINCE:
                q.append("(SELECT COUNT(1) today_count FROM view_topp_order o WHERE ");
                break;
            case AREA_TYPE_CITY:
                q.append("(SELECT LEFT(area_id,4) area_id, COUNT(1) today_count FROM view_topp_order o WHERE ");
                break;
            case AREA_TYPE_COUNTY:
                q.append("(SELECT area_id, COUNT(1) today_count FROM view_topp_order o WHERE ");
                break;
            default:
                throw new IllegalArgumentException("Invalid level");
        }
        if (CYCLE_TYPE_DAY.equals(cycleType)) {
            q.append("DATE_FORMAT(create_time,'%Y%m%d')='").append(date).append("'");
        } else if (CYCLE_TYPE_MONTH.equals(cycleType)) {
            q.append("DATE_FORMAT(create_time,'%Y%m')='").append(date.length() == 6 ? date : date.substring(0, 6)).append("'");
        } else {
            q.append("DATE_FORMAT(create_time,'%Y')='").append(date.length() == 4 ? date : date.substring(0, 4)).append("'");
        }
        if (onlyClosed) {
            q.append(" AND status_='end'");
        }
        if (level == AREA_TYPE_CITY) {
            q.append(" GROUP BY LEFT(area_id,4)");
        }
        if (level == AREA_TYPE_COUNTY) {
            q.append(" GROUP BY area_id");
        }
        q.append(") ").append(alias);
        return q.toString();
    }

    private String getCityJoinCondition(String cycleType, String alias) {
        return "LEFT(area.area_id, 4) = " + alias + ".area_id";
    }

    /**
     * 闭环数指标-省级统计
     *
     * @param cycleDate 统计周期日期（日格式：yyyy-MM-dd，月格式：yyyy-MM）
     * @param cycleType 统计周期类型（D-日，M-月）
     * @param kpiId     指标编码
     * @return
     */
    @Override
    public int buildProvinceClosedLoopKpiInsertSql(String cycleDate, String cycleType, String kpiId) {
        String[] compare = calculateCompareDates(cycleDate, cycleType);
        String allToday = getDataQuery(cycleDate, cycleType, "all_today", false, AREA_TYPE_PROVINCE);
        String today = getDataQuery(cycleDate, cycleType, "today", true, AREA_TYPE_PROVINCE);
        String compare1 = getDataQuery(compare[0], cycleType, "compare1", true, AREA_TYPE_PROVINCE);
        String compare2 = getDataQuery(compare[1], cycleType, "compare2", true, AREA_TYPE_PROVINCE);
        return mapper.buildProvinceClosedLoopKpiInsertSql(
            cycleDate, cycleType, kpiId, allToday, today, compare1, compare2
        );
    }

    /**
     * 闭环数指标-地市级统计
     *
     * @param cycleDate 统计周期日期（日格式：yyyy-MM-dd，月格式：yyyy-MM）
     * @param cycleType 统计周期类型（D-日，M-月）
     * @param kpiId     指标编码
     * @return
     */
    @Override
    public int buildCityClosedLoopKpiInsertSql(String cycleDate, String cycleType, String kpiId) {
        String[] compare = calculateCompareDates(cycleDate, cycleType);
        String allToday = getDataQuery(cycleDate, cycleType, "all_today", false, AREA_TYPE_CITY);
        String today = getDataQuery(cycleDate, cycleType, "today", true, AREA_TYPE_CITY);
        String compare1 = getDataQuery(compare[0], cycleType, "compare1", true, AREA_TYPE_CITY);
        String compare2 = getDataQuery(compare[1], cycleType, "compare2", true, AREA_TYPE_CITY);
        return mapper.buildCityClosedLoopKpiInsertSql(
            cycleDate, cycleType, kpiId, allToday, today, compare1, compare2
        );
    }

    /**
     * 闭环数指标-区县级统计
     *
     * @param cycleDate 统计周期日期（日格式：yyyy-MM-dd，月格式：yyyy-MM）
     * @param cycleType 统计周期类型（D-日，M-月）
     * @param kpiId     指标编码
     * @return
     */
    @Override
    public int buildCountyClosedLoopKpiInsertSql(String cycleDate, String cycleType, String kpiId) {
        String[] compare = calculateCompareDates(cycleDate, cycleType);
        String allToday = getDataQuery(cycleDate, cycleType, "all_today", false, AREA_TYPE_COUNTY);
        String today = getDataQuery(cycleDate, cycleType, "today", true, AREA_TYPE_COUNTY);
        String compare1 = getDataQuery(compare[0], cycleType, "compare1", true, AREA_TYPE_COUNTY);
        String compare2 = getDataQuery(compare[1], cycleType, "compare2", true, AREA_TYPE_COUNTY);
        return mapper.buildCountyClosedLoopKpiInsertSql(
            cycleDate, cycleType, kpiId, allToday, today, compare1, compare2
        );
    }


    /**
     * 计算环比和同比日期
     *
     * @param currentDate 当前日期
     * @param cycleType   统计周期类型(D-日,M-月)
     * @return String数组，[0]为环比日期，[1]为同比日期
     */
    private static String[] calculateCompareDates(String currentDate, String cycleType) {
        String[] dates = new String[2];
        String compareDate1 = ""; // 环比日期
        String compareDate2 = ""; // 同比日期

        if (CYCLE_TYPE_DAY.equals(cycleType)) {
            // 日统计逻辑
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(YEAR_MONTH_DAY_PATTERN);
                Date today = sdf.parse(currentDate);
                Calendar cal = Calendar.getInstance();

                // 昨天
                cal.setTime(today);
                cal.add(Calendar.DAY_OF_MONTH, -1);
                compareDate1 = sdf.format(cal.getTime());

                // 去年同一天
                cal.setTime(today);
                cal.add(Calendar.YEAR, -1);
                compareDate2 = sdf.format(cal.getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (CYCLE_TYPE_MONTH.equals(cycleType)) {
            // 月统计逻辑
            try {
                // 处理不同格式的日期
                String yearMonth;
                if (currentDate.contains("-")) {
                    yearMonth = currentDate.replace("-", "");
                } else if (currentDate.length() == 8) {
                    yearMonth = currentDate.substring(0, 6);
                } else {
                    yearMonth = currentDate;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
                Date currentMonth = sdf.parse(yearMonth);
                Calendar cal = Calendar.getInstance();

                // 上个月
                cal.setTime(currentMonth);
                cal.add(Calendar.MONTH, -1);
                compareDate1 = sdf.format(cal.getTime());

                // 去年同月
                cal.setTime(currentMonth);
                cal.add(Calendar.YEAR, -1);
                compareDate2 = sdf.format(cal.getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("Y".equals(cycleType)) {
            // 年统计逻辑
            try {
                String yearStr;
                if (currentDate.contains("-")) {
                    yearStr = currentDate.substring(0, 4);
                } else if (currentDate.length() == 8) {
                    yearStr = currentDate.substring(0, 4);
                } else {
                    yearStr = currentDate.substring(0, 4);
                }

                // 上一年
                int lastYear = Integer.parseInt(yearStr) - 1;
                compareDate1 = String.valueOf(lastYear);

                // 暂时没有同比逻辑，设为空字符串或者默认值
                compareDate2 = "";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        dates[0] = compareDate1;
        dates[1] = compareDate2;
        return dates;
    }

    /**
     * 维系工单平均处理时长-省级统计
     *
     * @param cycleDate 统计周期日期（日格式：yyyy-MM-dd，月格式：yyyy-MM）
     * @param cycleType 统计周期类型（D-日，M-月）
     * @param kpiId     指标编码
     * @return
     */
    @Override
    public int buildProvinceAvgDealTimeKpiInsertSql(String cycleDate, String cycleType, String kpiId) {
        String[] compare = calculateCompareDates(cycleDate, cycleType);
        String today = getAvgDealTimeDataQuery(cycleDate, cycleType, "today", 0);
        String compare1 = getAvgDealTimeDataQuery(compare[0], cycleType, "compare1", 0);
        String compare2 = getAvgDealTimeDataQuery(compare[1], cycleType, "compare2", 0);
        return mapper.buildProvinceAvgDealTimeKpiInsertSql(
            cycleDate, cycleType, kpiId, today, compare1, compare2
        );
    }

    /**
     * 维系工单平均处理时长-地市级统计
     *
     * @param cycleDate 统计周期日期（日格式：yyyy-MM-dd，月格式：yyyy-MM）
     * @param cycleType 统计周期类型（D-日，M-月）
     * @param kpiId     指标编码
     * @return
     */
    @Override
    public int buildCityAvgDealTimeKpiInsertSql(String cycleDate, String cycleType, String kpiId) {
        String[] compare = calculateCompareDates(cycleDate, cycleType);
        String today = getAvgDealTimeDataQuery(cycleDate, cycleType, "today", 1);
        String compare1 = getAvgDealTimeDataQuery(compare[0], cycleType, "compare1", 1);
        String compare2 = getAvgDealTimeDataQuery(compare[1], cycleType, "compare2", 1);
        return mapper.buildCityAvgDealTimeKpiInsertSql(
            cycleDate, cycleType, kpiId, today, compare1, compare2
        );
    }

    /**
     * 维系工单平均处理时长-区县级统计
     *
     * @param cycleDate 统计周期日期（日格式：yyyy-MM-dd，月格式：yyyy-MM）
     * @param cycleType 统计周期类型（D-日，M-月）
     * @param kpiId     指标编码
     * @return
     */
    @Override
    public int buildCountyAvgDealTimeKpiInsertSql(String cycleDate, String cycleType, String kpiId) {
        String[] compare = calculateCompareDates(cycleDate, cycleType);
        String today = getAvgDealTimeDataQuery(cycleDate, cycleType, "today", 2);
        String compare1 = getAvgDealTimeDataQuery(compare[0], cycleType, "compare1", 2);
        String compare2 = getAvgDealTimeDataQuery(compare[1], cycleType, "compare2", 2);
        return mapper.buildCountyAvgDealTimeKpiInsertSql(
            cycleDate, cycleType, kpiId, today, compare1, compare2
        );
    }

    /**
     * 平均处理时长数据查询方法
     *
     * @param date      查询日期
     * @param cycleType 周期类型(D-日,M-月)
     * @param alias     别名
     * @param level     区域层级: 0-省级, 1-地市级, 2-区县级
     * @return SQL查询字符串
     */
    private String getAvgDealTimeDataQuery(String date, String cycleType, String alias, int level) {
        StringBuilder q = new StringBuilder();
        switch (level) {
            case AREA_TYPE_PROVINCE:
                q.append("(SELECT ROUND(IFNULL(sum(deal_time)/3600/COUNT(1),0),2) today_count FROM view_topp_order o WHERE ");
                break;
            case AREA_TYPE_CITY:
                q.append("(SELECT LEFT(area_id,4) area_id, ROUND(IFNULL(sum(deal_time)/3600/COUNT(1),0),2) today_count FROM view_topp_order o WHERE ");
                break;
            case AREA_TYPE_COUNTY:
                q.append("(SELECT area_id, ROUND(IFNULL(sum(deal_time)/3600/COUNT(1),0),2) today_count FROM view_topp_order o WHERE ");
                break;
            default:
                throw new IllegalArgumentException("Invalid level");
        }
        if (CYCLE_TYPE_DAY.equals(cycleType)) {
            q.append("DATE_FORMAT(create_time,'%Y%m%d')='").append(date).append("' AND status_='end'");
        } else if (CYCLE_TYPE_MONTH.equals(cycleType)) {
            q.append("DATE_FORMAT(create_time,'%Y%m')='").append(date.length() == 6 ? date : date.substring(0, 6)).append("' AND status_='end'");
        } else {
            q.append("DATE_FORMAT(create_time,'%Y')='").append(date.length() == 4 ? date : date.substring(0, 4)).append("' AND status_='end'");
        }
        if (level == AREA_TYPE_CITY) {
            q.append(" GROUP BY LEFT(area_id,4)");
        }
        if (level == AREA_TYPE_COUNTY) {
            q.append(" GROUP BY area_id");
        }
        q.append(") ").append(alias);
        return q.toString();
    }

    /**
     * 用户活跃数-省级统计
     *
     * @param cycleDate 统计周期日期（日格式：yyyy-MM-dd，月格式：yyyy-MM）
     * @param cycleType 统计周期类型（D-日，M-月）
     * @param kpiId     指标编码
     * @return
     */
    @Override
    public int buildProvinceDailyActiveUsersKpiInsertSql(String cycleDate, String cycleType, String kpiId) {
        String[] compare = calculateCompareDates(cycleDate, cycleType);
        String today = getDailyActiveUsersByAreaQuery(cycleDate, cycleType, "today", AREA_TYPE_PROVINCE);
        String compare1 = getDailyActiveUsersByAreaQuery(compare[0], cycleType, "compare1", AREA_TYPE_PROVINCE);
        String compare2 = getDailyActiveUsersByAreaQuery(compare[1], cycleType, "compare2", AREA_TYPE_PROVINCE);
        return mapper.buildProvinceDailyActiveUsersKpiInsertSql(
            cycleDate, cycleType, kpiId, today, compare1, compare2
        );
    }

    /**
     * 用户活跃数-地市级统计
     *
     * @param cycleDate 统计周期日期（日格式：yyyy-MM-dd，月格式：yyyy-MM）
     * @param cycleType 统计周期类型（D-日，M-月）
     * @param kpiId     指标编码
     * @return
     */
    @Override
    public int buildCityDailyActiveUsersKpiInsertSql(String cycleDate, String cycleType, String kpiId) {
        String[] compare = calculateCompareDates(cycleDate, cycleType);
        String today = getDailyActiveUsersByAreaQuery(cycleDate, cycleType, "today", AREA_TYPE_CITY);
        String compare1 = getDailyActiveUsersByAreaQuery(compare[0], cycleType, "compare1", AREA_TYPE_CITY);
        String compare2 = getDailyActiveUsersByAreaQuery(compare[1], cycleType, "compare2", AREA_TYPE_CITY);
        return mapper.buildCityDailyActiveUsersKpiInsertSql(
            cycleDate, cycleType, kpiId, today, compare1, compare2
        );
    }

    /**
     * 用户活跃数-区县级统计
     *
     * @param cycleDate 统计周期日期（日格式：yyyy-MM-dd，月格式：yyyy-MM）
     * @param cycleType 统计周期类型（D-日，M-月）
     * @param kpiId     指标编码
     * @return
     */
    @Override
    public int buildCountyDailyActiveUsersKpiInsertSql(String cycleDate, String cycleType, String kpiId) {
        String[] compare = calculateCompareDates(cycleDate, cycleType);
        String today = getDailyActiveUsersByAreaQuery(cycleDate, cycleType, "today", AREA_TYPE_COUNTY);
        String compare1 = getDailyActiveUsersByAreaQuery(compare[0], cycleType, "compare1", AREA_TYPE_COUNTY);
        String compare2 = getDailyActiveUsersByAreaQuery(compare[1], cycleType, "compare2", AREA_TYPE_COUNTY);
        return mapper.buildCountyDailyActiveUsersKpiInsertSql(
            cycleDate, cycleType, kpiId, today, compare1, compare2
        );
    }

    /**
     * 日活跃用户数查询方法(地市/区县级别)
     *
     * @param date      查询日期
     * @param cycleType 周期类型(D-日,M-月)
     * @param alias     别名
     * @return SQL查询字符串
     */
    private String getDailyActiveUsersByAreaQuery(String date, String cycleType, String alias, int level) {
        StringBuilder q = new StringBuilder();
        switch (level) {
            case AREA_TYPE_PROVINCE:
                q.append("(SELECT COUNT(DISTINCT user_id) today_count ");
                break;
            case AREA_TYPE_CITY:
                q.append("(SELECT LEFT(so.area_id,4) area_id, COUNT(DISTINCT sl.user_id) today_count ");
                break;
            case AREA_TYPE_COUNTY:
                q.append("(SELECT so.area_id, COUNT(DISTINCT sl.user_id) today_count ");
                break;
            default:
                throw new IllegalArgumentException("Invalid level");
        }
        q.append("FROM sys_login_log sl ")
            .append("INNER JOIN sys_user su ON sl.user_id=su.id_ ");
        if (level == AREA_TYPE_CITY || level == AREA_TYPE_COUNTY) {
            q.append("LEFT JOIN sys_org so ON su.org_id=so.id_ ");
        }
        q.append("WHERE ");
        if (CYCLE_TYPE_DAY.equals(cycleType)) {
            q.append("DATE_FORMAT(sl.login_time,'%Y%m%d')='").append(date).append("'");
        } else if (CYCLE_TYPE_MONTH.equals(cycleType)) {
            q.append("DATE_FORMAT(sl.login_time,'%Y%m')='").append(date.length() == 6 ? date : date.substring(0, 6)).append("'");
        } else {
            q.append("DATE_FORMAT(sl.login_time,'%Y')='").append(date.length() == 4 ? date : date.substring(0, 4)).append("'");
        }
        if (level == AREA_TYPE_CITY) {
            q.append(" GROUP BY LEFT(so.area_id,4)");
        }
        if (level == AREA_TYPE_COUNTY) {
            q.append(" GROUP BY so.area_id");
        }
        q.append(") ").append(alias);
        return q.toString();
    }

    /**
     * 操作日志数-省级统计
     *
     * @param cycleDate 统计周期日期（日格式：yyyy-MM-dd，月格式：yyyy-MM）
     * @param cycleType 统计周期类型（D-日，M-月）
     * @param kpiId     指标编码
     * @return
     */
    @Override
    public int buildProvinceLogCountKpiInsertSql(String cycleDate, String cycleType, String kpiId) {
        String[] compare = calculateCompareDates(cycleDate, cycleType);
        String today = getLogCountQuery(cycleDate, cycleType, "today");
        String compare1 = getLogCountQuery(compare[0], cycleType, "compare1");
        String compare2 = getLogCountQuery(compare[1], cycleType, "compare2");
        return mapper.buildProvinceLogCountKpiInsertSql(
            cycleDate, cycleType, kpiId, today, compare1, compare2
        );
    }

    /**
     * 大于3s接口数-省级统计-临时插入一条空指标值数据
     *
     * @param cycleDate 统计周期日期（日格式：yyyy-MM-dd，月格式：yyyy-MM）
     * @param cycleType 统计周期类型（D-日，M-月）
     * @param kpiId     指标编码
     * @return
     */
    @Override
    public int buildProvinceLongCountKpiInsertSql(String cycleDate, String cycleType, String kpiId) {
        String today = getLogCountQuery(cycleDate, cycleType, "today");
        return mapper.buildProvinceLongCountKpiInsertSql(
            cycleDate, cycleType, kpiId, today
        );
    }

    /**
     * 日志数量查询方法
     *
     * @param date      查询日期
     * @param cycleType 周期类型(D-日,M-月)
     * @param alias     别名
     * @return SQL查询字符串
     */
    private String getLogCountQuery(String date, String cycleType, String alias) {
        StringBuilder query = new StringBuilder();
        query.append("(SELECT IFNULL(COUNT(1),0) as today_count ")
            .append("FROM sys_log o WHERE type_='操作日志' AND ");

        if (CYCLE_TYPE_DAY.equals(cycleType)) {
            query.append("DATE_FORMAT(create_time,'%Y%m%d') = '").append(date).append("'");
        } else if (CYCLE_TYPE_MONTH.equals(cycleType)) {
            query.append("DATE_FORMAT(create_time, '%Y%m') = '").append(date.length() == 6 ? date : date.substring(0, 6)).append("'");
        } else {
            query.append("DATE_FORMAT(create_time, '%Y') = '").append(date.length() == 4 ? date : date.substring(0, 4)).append("'");
        }

        query.append(") ").append(alias);
        return query.toString();
    }

}
