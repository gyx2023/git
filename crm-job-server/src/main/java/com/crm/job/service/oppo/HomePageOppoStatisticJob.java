package com.crm.job.service.oppo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.crm.system.domain.topp.ToppOppoStatistics;
import com.crm.system.domain.bo.SysCodeBo;
import com.crm.system.domain.vo.SysCodeVo;
import com.crm.system.service.sys.ISysCodeService;
import com.crm.system.service.topp.IToppOppoStatisticsService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * PC端首页商机数、签约数、签约金额等信息统计定时任务
 * 迁移自老项目 yypt_web OppoStatisticJob
 *
 * @author crm
 */
@Slf4j
@Component
public class HomePageOppoStatisticJob {

    private static final String JOB_HANDLER_NAME = "homePageOppoAndMoneyStatistic";
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    @Resource
    private IToppOppoStatisticsService oppoStatisticsService;


    @Resource
    private ISysCodeService sysCodeService;

    /**
     * 每天凌晨统计商机数据、签约数、签约金额
     */
    @XxlJob(JOB_HANDLER_NAME)
    public void execute() {
        long startTime = System.currentTimeMillis();
        log.info("【首页商机统计任务】开始执行，时间：{}", DateUtil.now());
        XxlJobHelper.log("【首页商机统计任务】开始执行");

        try {
            // 判断今日是否已统计，一天一次
            if (!oppoStatisticsService.isTodayStatsDone(1, "1")) {
                // 1. 储备商机统计
                statisticReserveCountAndMoney();
                // 2. 本月新增统计
                statisticMonthCountAndMoney();
                // 3. 签约商机统计
                statisticSignCountAndMoney();
                // 4. 商机分类统计
                statisticIncludeType();
                // 5. 签约按月统计
                statisticMonthSign();
            }

            long costTime = System.currentTimeMillis() - startTime;
            String msg = String.format("【首页商机统计任务】执行完成，耗时：%dms", costTime);
            log.info(msg);
            XxlJobHelper.handleSuccess(msg);
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            String errorMsg = String.format("【首页商机统计任务】执行失败，耗时：%dms，异常：%s", costTime, e.getMessage());
            log.error(errorMsg, e);
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    // ============ 储备商机统计 ============

    private void statisticReserveCountAndMoney() {
        log.info("【首页商机统计任务】开始统计储备商机");
        // areaType=1: 全省/全市
        List<Map<String, Object>> data1 = oppoStatisticsService.getSimpleStatsByFullArea("1");
        if (CollUtil.isNotEmpty(data1)) {
            saveSimpleData(data1, "1", "1");
        }
        // areaType=2: 省本/市本/县分
        List<Map<String, Object>> data2 = oppoStatisticsService.getSimpleStatsByIndividualArea("1");
        if (CollUtil.isNotEmpty(data2)) {
            saveSimpleData(data2, "1", "2");
        }
    }

    // ============ 本月新增统计 ============

    private void statisticMonthCountAndMoney() {
        log.info("【首页商机统计任务】开始统计本月新增");
        List<Map<String, Object>> data1 = oppoStatisticsService.getSimpleStatsByFullArea("2");
        if (CollUtil.isNotEmpty(data1)) {
            saveSimpleData(data1, "2", "1");
        }
        List<Map<String, Object>> data2 = oppoStatisticsService.getSimpleStatsByIndividualArea("2");
        if (CollUtil.isNotEmpty(data2)) {
            saveSimpleData(data2, "2", "2");
        }
    }

    // ============ 签约商机统计 ============

    private void statisticSignCountAndMoney() {
        log.info("【首页商机统计任务】开始统计签约商机");
        List<Map<String, Object>> data1 = oppoStatisticsService.getSimpleStatsByFullArea("3");
        if (CollUtil.isNotEmpty(data1)) {
            saveSimpleData(data1, "3", "1");
        }
        List<Map<String, Object>> data2 = oppoStatisticsService.getSimpleStatsByIndividualArea("3");
        if (CollUtil.isNotEmpty(data2)) {
            saveSimpleData(data2, "3", "2");
        }
    }

    // ============ 商机分类统计 ============

    /**
     * 商机分类统计（严格对齐老项目 OppoStatisticJob.statisticIncludeType）
     */
    private void statisticIncludeType() {
        log.info("【首页商机统计任务】开始统计商机分类");

        // areaType=1: 全省/全市模式，timeLimite = 1,3,6
        for (int timeLimite : new int[]{1, 3, 6}) {
            getIncludeTypeCount(timeLimite, 1);
        }

        // 各地市（含省级），取 getCityAreaIds() 返回的6位ID，截断前4位作为 areaType
        List<Long> cityAreaIds = oppoStatisticsService.getCityAreaIds();
        for (Long areaId : cityAreaIds) {
            int areaType = Integer.parseInt(String.valueOf(areaId).substring(0, 4));
            for (int timeLimite : new int[]{1, 3, 6}) {
                getIncludeTypeCount(timeLimite, areaType);
            }
        }
    }

    /**
     * 商机分类统计 - 单次执行（对齐老项目 getIncludeTypeCount）
     *
     * @param timeLimite 时间范围 1/3/6
     * @param areaType   区域类型 1=全省/全市，其他=市本(4位如3305)
     */
    private void getIncludeTypeCount(int timeLimite, int areaType) {
        // 1. 查询商机分类统计数据
        List<Map<String, Object>> includeTypeCount = oppoStatisticsService.getIncludeTypeCount(timeLimite, areaType);

        // 2. 获取区域列表
        List<Long> areaList;
        if (areaType != 1) {
            // 市本模式：只查有数据的区域（6位完整ID）
            areaList = oppoStatisticsService.getHaveDataAreaIds(timeLimite, areaType);
        } else {
            // 全省模式：所有地市（含省级）
            areaList = oppoStatisticsService.getCityAreaIds();
        }

        // 3. 按老项目逻辑：用指针 j 顺序遍历 includeTypeCount（因为 SQL ORDER BY areaId）
        JSONObject json1 = new JSONObject();
        int j = 0;

        for (Long areaId1 : areaList) {
            List<JSONObject> list = new ArrayList<>();
            List<String> codeType = new ArrayList<>();
            long total = 0;

            for (int i = j; i < includeTypeCount.size(); i++) {
                Map<String, Object> row = includeTypeCount.get(i);
                Object areaId2Obj = row.get("areaId");
                if (areaId2Obj == null) {
                    continue;
                }

                // 老项目 L148: areaId1.toString().equals(areaId2.toString())
                String areaId2Str = String.valueOf(areaId2Obj);
                String areaId1Str = String.valueOf(areaId1);

                if (areaId1Str.equals(areaId2Str)) {
                    // 老项目 L153-162: 提取 count, type, typeName
                    long count = row.get("cnt") != null ? Long.parseLong(String.valueOf(row.get("cnt"))) : 0;
                    String type = row.get("typeCode") != null ? String.valueOf(row.get("typeCode")) : "";
                    String typeName = row.get("typeName") != null ? String.valueOf(row.get("typeName")) : "";

                    codeType.add(type);

                    JSONObject item = new JSONObject();
                    item.put("count", count);
                    item.put("type", type);
                    item.put("typeName", typeName);
                    list.add(item);
                    total += count;
                } else {
                    // 老项目 L170: j = i，然后 break
                    j = i;
                    break;
                }
            }

            // 4. 补全缺失的商机类型（count=0）
            SysCodeBo sysCodeBo = new SysCodeBo();
            sysCodeBo.setType("opp_type");
            List<SysCodeVo> allCodeList = sysCodeService.queryList(sysCodeBo);
            for (SysCodeVo sysCode : allCodeList) {
                if (!codeType.contains(sysCode.getCode())) {
                    JSONObject item = new JSONObject();
                    item.put("count", 0);
                    item.put("type", sysCode.getCode());
                    item.put("typeName", sysCode.getName());
                    list.add(item);
                }
            }

            // 5. 添加合计
            JSONObject totalObj = new JSONObject();
            totalObj.put("total", total);
            list.add(totalObj);

            json1.put(String.valueOf(areaId1), list);
        }

        // 6. 保存（对齐老项目 setOppoStatistics + saveOrUpdate）
        String areaTypeStr = areaType != 1 ? areaType + "00" : String.valueOf(areaType);
        ToppOppoStatistics oppoStatistics = new ToppOppoStatistics();
        oppoStatistics.setType("4");
        oppoStatistics.setAreaType(areaTypeStr);
        oppoStatistics.setData(JSON.toJSONString(json1));
        oppoStatistics.setTimeLimite(String.valueOf(timeLimite));
        oppoStatistics.setStatisticsDate(getToday());
        oppoStatisticsService.saveOrUpdate(oppoStatistics);
    }

    /**
     * 保存商机分类统计数据（旧方法，已废弃，保留以免影响其他逻辑）
     * @deprecated 使用 getIncludeTypeCount 替代
     */
    @Deprecated
    private void saveIncludeTypeData(List<Map<String, Object>> dataList, String areaType, String timeLimite) {
        JSONObject json = new JSONObject();
        if (CollUtil.isNotEmpty(dataList)) {
            Map<String, List<JSONObject>> grouped = new LinkedHashMap<>();
            Map<String, Long> totals = new LinkedHashMap<>();
            Map<String, List<String>> areaCodeTypes = new LinkedHashMap<>();

            for (Map<String, Object> row : dataList) {
                String areaId = String.valueOf(row.get("areaId"));
                long count = row.get("cnt") == null ? 0L : Long.parseLong(String.valueOf(row.get("cnt")));
                String typeCode = String.valueOf(row.getOrDefault("typeCode", ""));
                String typeName = String.valueOf(row.getOrDefault("typeName", ""));

                JSONObject item = new JSONObject();
                item.put("count", count);
                item.put("type", typeCode);
                item.put("typeName", typeName);
                grouped.computeIfAbsent(areaId, k -> new ArrayList<>()).add(item);
                totals.merge(areaId, count, Long::sum);
                areaCodeTypes.computeIfAbsent(areaId, k -> new ArrayList<>()).add(typeCode);
            }

            SysCodeBo sysCodeBo = new SysCodeBo();
            sysCodeBo.setType("opp_type");
            List<SysCodeVo> allCodeList = sysCodeService.queryList(sysCodeBo);

            for (Map.Entry<String, List<JSONObject>> entry : grouped.entrySet()) {
                List<JSONObject> list = entry.getValue();
                List<String> existCodes = areaCodeTypes.getOrDefault(entry.getKey(), Collections.emptyList());
                for (SysCodeVo sysCode : allCodeList) {
                    if (!existCodes.contains(sysCode.getCode())) {
                        JSONObject item = new JSONObject();
                        item.put("count", 0);
                        item.put("type", sysCode.getCode());
                        item.put("typeName", sysCode.getName());
                        list.add(item);
                    }
                }
                JSONObject totalObj = new JSONObject();
                totalObj.put("total", totals.getOrDefault(entry.getKey(), 0L));
                list.add(totalObj);
                json.put(entry.getKey(), list);
            }
        }

        ToppOppoStatistics stats = new ToppOppoStatistics();
        stats.setType("4");
        stats.setAreaType(areaType);
        stats.setTimeLimite(timeLimite);
        stats.setData(JSON.toJSONString(json));
        stats.setStatisticsDate(getToday());
        oppoStatisticsService.saveOrUpdate(stats);
    }

    // ============ 签约按月统计 ============

    private void statisticMonthSign() {
        log.info("【首页商机统计任务】开始统计签约按月");
        // areaType=1：全省+各地市汇总
        saveMonthSignOrOppoData(1);
        // 各地市本及下属县分（含 330000 → prefix=3300，对应 area_type="330000"）
        List<Long> cityAreaIds = oppoStatisticsService.getCityAreaIds();
        for (Long areaId : cityAreaIds) {
            int areaPrefix = Integer.parseInt(String.valueOf(areaId).substring(0, 4));
            saveMonthSignOrOppoData(areaPrefix);
        }
    }

    // ============ 数据保存方法 ============

    /**
     * 保存简单统计数据（储备/本月/签约）
     */
    private void saveSimpleData(List<Map<String, Object>> dataList, String type, String areaType) {
        JSONObject json = new JSONObject();
        for (Map<String, Object> row : dataList) {
            String count = String.valueOf(row.getOrDefault("cnt", "0"));
            // 0 无小数点，非 0 保留原始精度
            String money = formatMoney(row.get("money"));
            Object areaId = row.get("areaId");
            JSONObject item = new JSONObject();
            item.put("count", count);
            item.put("money", money);
            json.put(String.valueOf(areaId), item);
        }
        ToppOppoStatistics stats = new ToppOppoStatistics();
        stats.setType(type);
        stats.setAreaType(areaType);
        stats.setData(JSON.toJSONString(json));
        stats.setStatisticsDate(getToday());
        oppoStatisticsService.saveOrUpdate(stats);
    }

    /**
     * 保存签约按月统计数据
     * 严格还原老项目 OppoStatisticJob.getMonthSignData(int areaType) 逻辑：
     * - areaType=1：areaList = getCityAreaIds()（含 330000 + 各地市）
     * - areaType!=1：areaList = getHaveOppoDataAreaIds(areaType)
     * - 按 areaList 顺序遍历，用指针 j 在 monthOppoData 中匹配，没有数据的 area 也补全 12 个月 0
     */
    private void saveMonthSignOrOppoData(int areaType) {
        List<Map<String, Object>> monthSignData = oppoStatisticsService.getMonthSignData(areaType);
        List<Map<String, Object>> monthOppoData = oppoStatisticsService.getMonthOppoData(areaType);

        // 获取 areaList（与老项目逻辑一致）
        List<Long> areaList;
        if (areaType != 1) {
            areaList = oppoStatisticsService.getHaveOppoDataAreaIds(areaType);
        } else {
            areaList = oppoStatisticsService.getCityAreaIds();
        }

        // 将 signData 按 areaId+month 索引，便于匹配查找（语义等价于老项目按月遍历匹配）
        Map<String, Map<String, Object>> signIndex = new HashMap<>();
        if (CollUtil.isNotEmpty(monthSignData)) {
            for (Map<String, Object> row : monthSignData) {
                String key = String.valueOf(row.get("areaId")) + "_" + String.valueOf(row.get("monthVal"));
                signIndex.put(key, row);
            }
        }

        JSONObject json1 = new JSONObject();
        int j = 0;  // monthOppoData 遍历指针
        for (Long areaId1 : areaList) {
            int monthNumber = 1;
            List<JSONObject> list = new ArrayList<>();
            String areaId1Str = String.valueOf(areaId1);

            for (int i = j; i < monthOppoData.size(); i++) {
                Map<String, Object> obj = monthOppoData.get(i);
                String areaId2 = String.valueOf(obj.get("areaId"));

                if (areaId1Str.equals(areaId2)) {
                    String month = String.valueOf(obj.get("monthVal"));
                    // 补全前面缺失的月份
                    while (!month.equals(String.valueOf(monthNumber)) && monthNumber < 13) {
                        JSONObject item = new JSONObject();
                        item.put("month", String.valueOf(monthNumber));
                        item.put("money", "0");
                        item.put("signTotal", "0");
                        item.put("oppoTotal", "0");
                        list.add(item);
                        monthNumber++;
                    }
                    // 当前月数据
                    String oppoTotal = "0";
                    String money = "0";
                    String signTotal = "0";
                    Object oppoTotalObj = obj.get("total");
                    if (oppoTotalObj != null && !"".equals(String.valueOf(oppoTotalObj))) {
                        oppoTotal = String.valueOf(oppoTotalObj);
                    }
                    // 查找匹配的签约数据
                    Map<String, Object> signRow = signIndex.get(areaId2 + "_" + month);
                    if (signRow != null) {
                        Object moneyObj = signRow.get("money");
                        if (moneyObj != null && !"".equals(String.valueOf(moneyObj))) {
                            money = formatMoney(moneyObj);
                        }
                        Object signTotalObj = signRow.get("total");
                        if (signTotalObj != null && !"".equals(String.valueOf(signTotalObj))) {
                            signTotal = String.valueOf(signTotalObj);
                        }
                    }
                    JSONObject item = new JSONObject();
                    item.put("month", month);
                    item.put("money", money);
                    item.put("signTotal", signTotal);
                    item.put("oppoTotal", oppoTotal);
                    list.add(item);
                    monthNumber++;
                } else {
                    // 当前 area 处理结束，补全剩余月份 0
                    while (monthNumber < 13) {
                        JSONObject item = new JSONObject();
                        item.put("month", String.valueOf(monthNumber));
                        item.put("money", "0");
                        item.put("signTotal", "0");
                        item.put("oppoTotal", "0");
                        list.add(item);
                        monthNumber++;
                    }
                    j = i;
                    break;
                }
            }
            // 若该 area 在 monthOppoData 中完全没有数据，循环不会进入，需补全 12 个月 0
            while (monthNumber < 13) {
                JSONObject item = new JSONObject();
                item.put("month", String.valueOf(monthNumber));
                item.put("money", "0");
                item.put("signTotal", "0");
                item.put("oppoTotal", "0");
                list.add(item);
                monthNumber++;
            }
            json1.put(areaId1Str, list);
        }

        ToppOppoStatistics stats = new ToppOppoStatistics();
        stats.setType("5");
        if (areaType != 1) {
            stats.setAreaType(areaType + "00");
        } else {
            stats.setAreaType(String.valueOf(areaType));
        }
        stats.setData(JSON.toJSONString(json1));
        stats.setStatisticsDate(getToday());
        oppoStatisticsService.saveOrUpdate(stats);
    }

    private Date getToday() {
        try {
            return DATE_FMT.parse(DATE_FMT.format(new Date()));
        } catch (ParseException e) {
            return new Date();
        }
    }

    /**
     * 格式化金额：0 无小数点，非 0 保留原始精度不变。
     * 例：BigDecimal("0.000000") -> "0"，BigDecimal("132.500000") -> "132.500000"
     */
    private static String formatMoney(Object moneyVal) {
        if (moneyVal == null) {
            return "0";
        }
        java.math.BigDecimal bd;
        try {
            bd = new java.math.BigDecimal(String.valueOf(moneyVal));
        } catch (NumberFormatException e) {
            return String.valueOf(moneyVal);
        }
        if (bd.signum() == 0) {
            return "0";
        }
        return bd.toPlainString();
    }
}
