package com.crm.workflow.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.page.TableDataInfo;
import com.crm.workflow.domain.dto.SupportHoursQueryDTO;
import com.crm.workflow.domain.vo.SupportHoursVO;
import com.crm.workflow.service.ISupportHoursService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 支撑工时统计Service实现
 *
 * @author crm
 */
@Slf4j
@Service
public class SupportHoursServiceImpl implements ISupportHoursService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询支撑工时统计数据（分页）
     */
    @Override
    public TableDataInfo<SupportHoursVO> querySupportHoursList(SupportHoursQueryDTO queryDTO) {
        log.info("查询支撑工时统计，参数：{}", queryDTO);
        
        List<SupportHoursVO> list;
        if (queryDTO.getTabType() == null || queryDTO.getTabType() == 1) {
            // 商机支撑人日统计
            list = querySupportHoursListByOpportunity(queryDTO);
        } else {
            // 个人支撑商机统计
            list = querySupportHoursListByManager(queryDTO);
        }
        
        // 手动分页
        Page<SupportHoursVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        int total = list.size();
        int fromIndex = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        int toIndex = Math.min(fromIndex + queryDTO.getPageSize(), total);
        
        List<SupportHoursVO> pageList = fromIndex < total ? list.subList(fromIndex, toIndex) : new ArrayList<>();
        
        TableDataInfo<SupportHoursVO> tableDataInfo = new TableDataInfo<>();
        tableDataInfo.setRows(pageList);
        tableDataInfo.setTotal(total);
        tableDataInfo.setCode(200);
        tableDataInfo.setMsg("查询成功");
        
        return tableDataInfo;
    }

    /**
     * 导出支撑工时统计数据（不分页）
     */
    @Override
    public List<SupportHoursVO> exportSupportHoursList(SupportHoursQueryDTO queryDTO) {
        log.info("导出支撑工时统计，参数：{}", queryDTO);
        
        if (queryDTO.getTabType() == null || queryDTO.getTabType() == 1) {
            // 商机支撑人日统计
            return querySupportHoursListByOpportunity(queryDTO);
        } else {
            // 个人支撑商机统计
            return querySupportHoursListByManager(queryDTO);
        }
    }

    /**
     * 商机支撑人日统计
     */
    private List<SupportHoursVO> querySupportHoursListByOpportunity(SupportHoursQueryDTO queryDTO) {
        log.info("开始查询商机支撑人日统计，参数：{}", queryDTO);
        
        List<SupportHoursVO> resultList = new ArrayList<>();
        
        try {
            // 1. 构建查询商机的SQL
            String opportunitySql = buildOpportunityQuerySql(queryDTO);
            log.info("商机查询SQL: {}", opportunitySql);
            
            // 2. 查询商机列表
            List<Map<String, Object>> opportunityList = jdbcTemplate.queryForList(opportunitySql);
            log.info("查询到商机数量: {}", opportunityList.size());
            
            // 3. 对每个商机，查询其关联的支撑任务工时数据
            for (Map<String, Object> opp : opportunityList) {
                SupportHoursVO vo = new SupportHoursVO();
                
                // 设置基础信息
                vo.setOppId(getIntValue(opp.get("id")));
                vo.setOppCode(getStringValue(opp.get("oppCode")));
                vo.setOppName(getStringValue(opp.get("oppName")));
                vo.setCustName(getStringValue(opp.get("custName")));
                vo.setXpectContractMoney(formatMoney(opp.get("expectContractMoney")));
                vo.setUnicomType(getStringValue(opp.get("unicomType")));
                vo.setCustManagerName(getStringValue(opp.get("custManagerName")));
                vo.setOppTime(getStringValue(opp.get("designerTime")));
                vo.setFirstManagetName(getStringValue(opp.get("firstManagerName")));
                vo.setSecondManagetName(getStringValue(opp.get("secondManagerName")));
                vo.setSupportStage(getStringValue(opp.get("supportStage")));
                
                // 获取支撑阶段名称
                String supportStageCode = getStringValue(opp.get("supportStage"));
                vo.setTaskName(getSupportStageName(supportStageCode));
                
                // 4. 查询该商机的工时数据（tcomm_task表）
                Integer oppId = getIntValue(opp.get("id"));
                if (oppId != null) {
                    // 查询task工时数据
                    List<Map<String, Object>> taskHoursList = queryTaskHours(oppId, queryDTO);
                    // 查询技术解析评审工时数据
                    List<Map<String, Object>> projectTechHoursList = queryProjectTechHours(oppId, queryDTO);
                    
                    // 5. 聚合工时数据
                    LinkedHashMap<String, Double> monthHourMap = new LinkedHashMap<>();
                    LinkedHashMap<String, Double> weeklyHourMap = new LinkedHashMap<>();
                    double totalHours = 0.0;
                    
                    // 初始化月份和周周期
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date leftDate = sdf.parse(queryDTO.getLeftTime());
                    Date rightDate = sdf.parse(queryDTO.getRightTime());
                    List<String> yearMonths = getYearMonthsBetween(leftDate, rightDate);
                    List<String> weekCycles = getWeeklyCycles(leftDate, rightDate);
                    
                    for (String month : yearMonths) {
                        monthHourMap.put(month, 0.0);
                    }
                    for (String cycle : weekCycles) {
                        weeklyHourMap.put(cycle, 0.0);
                    }
                    
                    // 处理task工时数据
                    Set<String> taskCodes = new HashSet<>();
                    for (Map<String, Object> taskHours : taskHoursList) {
                        // 跳过技术解析评审类型
                        String taskCode = getStringValue(taskHours.get("taskCode"));
                        if ("onlineProgram".equals(taskCode)) {
                            continue;
                        }
                        
                        Integer taskId = getIntValue(taskHours.get("taskId"));
                        // 判断流程是否结束
                        if (isTaskFlowEnded(taskId)) {
                            Double hours = getDoubleValue(taskHours.get("supportHours"));
                            String createTime = getStringValue(taskHours.get("createTime"));
                            
                            if (hours != null && hours > 0) {
                                totalHours += hours;
                                
                                // 按月聚合
                                if (createTime != null && !createTime.isEmpty()) {
                                    String month = createTime.substring(0, 7); // yyyy-MM
                                    if (monthHourMap.containsKey(month)) {
                                        monthHourMap.put(month, monthHourMap.get(month) + hours);
                                    }
                                    
                                    // 按周聚合
                                    String cycle = findWeeklyCycle(leftDate, rightDate, sdf.parse(createTime.substring(0, 10)));
                                    if (cycle != null && weeklyHourMap.containsKey(cycle)) {
                                        weeklyHourMap.put(cycle, weeklyHourMap.get(cycle) + hours);
                                    }
                                }
                            }
                        }
                        
                        if (taskCode != null && !taskCode.isEmpty()) {
                            taskCodes.add(taskCode);
                        }
                    }
                    
                    // 处理技术解析评审工时数据
                    for (Map<String, Object> techHours : projectTechHoursList) {
                        Integer projectTechId = getIntValue(techHours.get("projectTechId"));
                        // 判断流程是否结束
                        if (isProjectTechFlowEnded(projectTechId)) {
                            Double hours = getDoubleValue(techHours.get("supportHours"));
                            String createTime = getStringValue(techHours.get("createTime"));
                            
                            if (hours != null && hours > 0) {
                                totalHours += hours;
                                
                                // 按月聚合
                                if (createTime != null && !createTime.isEmpty()) {
                                    String month = createTime.substring(0, 7); // yyyy-MM
                                    if (monthHourMap.containsKey(month)) {
                                        monthHourMap.put(month, monthHourMap.get(month) + hours);
                                    }
                                    
                                    // 按周聚合
                                    String cycle = findWeeklyCycle(leftDate, rightDate, sdf.parse(createTime.substring(0, 10)));
                                    if (cycle != null && weeklyHourMap.containsKey(cycle)) {
                                        weeklyHourMap.put(cycle, weeklyHourMap.get(cycle) + hours);
                                    }
                                }
                            }
                        }
                    }
                    
                    vo.setMonthHour(monthHourMap);
                    vo.setWeeklyHours(weeklyHourMap);
                    vo.setTotalHours(totalHours);
                }
                
                resultList.add(vo);
            }
            
            log.info("商机支撑人日统计查询完成，结果数量: {}", resultList.size());
        } catch (Exception e) {
            log.error("查询商机支撑人日统计失败", e);
        }
        
        return resultList;
    }

    /**
     * 个人支撑商机统计
     */
    private List<SupportHoursVO> querySupportHoursListByManager(SupportHoursQueryDTO queryDTO) {
        log.info("开始查询个人支撑商机统计，参数：{}", queryDTO);
        
        List<SupportHoursVO> resultList = new ArrayList<>();
        
        try {
            // 1. 查询支撑经理参与的商机列表（userId + oppoId组合）
            String managerSql = buildManagerQuerySql(queryDTO);
            log.info("支撑经理查询SQL: {}", managerSql);
            
            List<Map<String, Object>> managerList = jdbcTemplate.queryForList(managerSql);
            log.info("查询到支撑经理-商机组合数量: {}", managerList.size());
            
            // 2. 对每个支撑经理和商机的组合，统计工时数据
            for (Map<String, Object> managerData : managerList) {
                Integer userId = getIntValue(managerData.get("userId"));
                Integer oppoId = getIntValue(managerData.get("oppoId"));
                
                if (userId == null || oppoId == null) {
                    continue;
                }
                
                // 查询商机详细信息
                Map<String, Object> oppInfo = queryOpportunityById(oppoId);
                if (oppInfo == null) {
                    continue;
                }
                
                SupportHoursVO vo = new SupportHoursVO();
                
                // 设置支撑经理姓名
                String supportManagerName = queryUserName(userId);
                vo.setSupportManagerName(supportManagerName);
                
                // 设置商机基础信息
                vo.setOppId(getIntValue(oppInfo.get("id")));
                vo.setOppCode(getStringValue(oppInfo.get("oppCode")));
                vo.setOppName(getStringValue(oppInfo.get("oppName")));
                vo.setCustName(getStringValue(oppInfo.get("custName")));
                vo.setXpectContractMoney(formatMoney(oppInfo.get("expectContractMoney")));
                vo.setUnicomType(getStringValue(oppInfo.get("unicomType")));
                vo.setCustManagerName(getStringValue(oppInfo.get("custManagerName")));
                vo.setOppTime(getStringValue(oppInfo.get("designerTime")));
                vo.setFirstManagetName(getStringValue(oppInfo.get("firstManagerName")));
                vo.setSecondManagetName(getStringValue(oppInfo.get("secondManagerName")));
                vo.setSupportStage(getStringValue(oppInfo.get("supportStage")));
                vo.setTaskName(getSupportStageName(getStringValue(oppInfo.get("supportStage"))));
                
                // 3. 查询该支撑经理在该商机下的工时数据
                List<Map<String, Object>> taskHoursList = queryTaskHoursByManager(oppoId, userId, queryDTO);
                List<Map<String, Object>> projectTechHoursList = queryProjectTechHoursByManager(oppoId, userId, queryDTO);
                
                // 4. 聚合工时数据
                LinkedHashMap<String, Double> monthHourMap = new LinkedHashMap<>();
                LinkedHashMap<String, Double> weeklyHourMap = new LinkedHashMap<>();
                double totalHours = 0.0;
                
                // 初始化月份和周周期
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date leftDate = sdf.parse(queryDTO.getLeftTime());
                Date rightDate = sdf.parse(queryDTO.getRightTime());
                List<String> yearMonths = getYearMonthsBetween(leftDate, rightDate);
                List<String> weekCycles = getWeeklyCycles(leftDate, rightDate);
                
                for (String month : yearMonths) {
                    monthHourMap.put(month, 0.0);
                }
                for (String cycle : weekCycles) {
                    weeklyHourMap.put(cycle, 0.0);
                }
                
                // 处理task工时数据
                for (Map<String, Object> taskHours : taskHoursList) {
                    Double hours = getDoubleValue(taskHours.get("supportHours"));
                    String createTime = getStringValue(taskHours.get("createTime"));
                    
                    if (hours != null && hours > 0) {
                        totalHours += hours;
                        
                        // 按月聚合
                        if (createTime != null && !createTime.isEmpty()) {
                            String month = createTime.substring(0, 7); // yyyy-MM
                            if (monthHourMap.containsKey(month)) {
                                monthHourMap.put(month, monthHourMap.get(month) + hours);
                            }
                            
                            // 按周聚合
                            String cycle = findWeeklyCycle(leftDate, rightDate, sdf.parse(createTime.substring(0, 10)));
                            if (cycle != null && weeklyHourMap.containsKey(cycle)) {
                                weeklyHourMap.put(cycle, weeklyHourMap.get(cycle) + hours);
                            }
                        }
                    }
                }
                
                // 处理技术解析评审工时数据
                for (Map<String, Object> techHours : projectTechHoursList) {
                    Double hours = getDoubleValue(techHours.get("supportHours"));
                    String createTime = getStringValue(techHours.get("createTime"));
                    
                    if (hours != null && hours > 0) {
                        totalHours += hours;
                        
                        // 按月聚合
                        if (createTime != null && !createTime.isEmpty()) {
                            String month = createTime.substring(0, 7); // yyyy-MM
                            if (monthHourMap.containsKey(month)) {
                                monthHourMap.put(month, monthHourMap.get(month) + hours);
                            }
                            
                            // 按周聚合
                            String cycle = findWeeklyCycle(leftDate, rightDate, sdf.parse(createTime.substring(0, 10)));
                            if (cycle != null && weeklyHourMap.containsKey(cycle)) {
                                weeklyHourMap.put(cycle, weeklyHourMap.get(cycle) + hours);
                            }
                        }
                    }
                }
                
                vo.setMonthHour(monthHourMap);
                vo.setWeeklyHours(weeklyHourMap);
                vo.setTotalHours(totalHours);
                
                resultList.add(vo);
            }
            
            log.info("个人支撑商机统计查询完成，结果数量: {}", resultList.size());
        } catch (Exception e) {
            log.error("查询个人支撑商机统计失败", e);
        }
        
        return resultList;
    }

    /**
     * 生成导出表头
     */
    @Override
    public List<List<String>> generateExportHeaders(SupportHoursQueryDTO queryDTO) {
        List<List<String>> headList = new ArrayList<>();
        
        // 基础表头
        String[] baseHeaders = {"商机编号", "商机名称", "客户名称", "预期合同(万元)", "客户行业",
                "客户经理", "主责支撑经理", "非主责支撑经理", "支撑阶段", "合计"};
        
        for (String header : baseHeaders) {
            headList.add(Collections.singletonList(header));
        }
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date leftDate = sdf.parse(queryDTO.getLeftTime());
            Date rightDate = sdf.parse(queryDTO.getRightTime());
            
            // 获取年月列表
            List<String> yearMonths = getYearMonthsBetween(leftDate, rightDate);
            for (String month : yearMonths) {
                headList.add(Collections.singletonList(month));
            }
            
            // 如果包含周统计，添加周周期表头
            if (queryDTO.getOptions() != null && queryDTO.getOptions().contains("week")) {
                List<String> cycles = getWeeklyCycles(leftDate, rightDate);
                for (String cycle : cycles) {
                    headList.add(Collections.singletonList(cycle));
                }
            }
            
            // 添加最后两个固定表头
            headList.add(Collections.singletonList("商机到达时间"));
            headList.add(Collections.singletonList("支撑情况"));
            
        } catch (Exception e) {
            log.error("生成导出表头失败", e);
        }
        
        return headList;
    }

    /**
     * 获取两个日期之间的所有年月
     */
    @Override
    public List<String> getYearMonthsBetween(Date leftDate, Date rightDate) {
        List<String> yearMonths = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");

        // 确保 leftDate 不晚于 rightDate
        if (leftDate.after(rightDate)) {
            Date temp = leftDate;
            leftDate = rightDate;
            rightDate = temp;
        }

        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();

        startCal.setTime(leftDate);
        endCal.setTime(rightDate);

        // 设置到月初
        startCal.set(Calendar.DAY_OF_MONTH, 1);
        endCal.set(Calendar.DAY_OF_MONTH, 1);

        // 遍历所有月份
        while (!startCal.after(endCal)) {
            yearMonths.add(sdf.format(startCal.getTime()));
            startCal.add(Calendar.MONTH, 1);
        }

        return yearMonths;
    }

    /**
     * 获取两个日期之间的所有周周期
     */
    @Override
    public List<String> getWeeklyCycles(Date leftDate, Date rightDate) {
        List<String> cycles = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // 确保 leftDate 不晚于 rightDate
        if (leftDate.after(rightDate)) {
            Date temp = leftDate;
            leftDate = rightDate;
            rightDate = temp;
        }

        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();

        startCal.setTime(leftDate);
        endCal.setTime(rightDate);

        // 设置一周的第一天为周日
        startCal.setFirstDayOfWeek(Calendar.SUNDAY);
        endCal.setFirstDayOfWeek(Calendar.SUNDAY);

        // 处理第一个周期（从 leftDate 开始）
        Calendar firstCycleStart = Calendar.getInstance();
        firstCycleStart.setTime(leftDate);
        Calendar firstCycleEnd = (Calendar) firstCycleStart.clone();

        // 找到该周的周六
        firstCycleEnd.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);

        String cycle = sdf.format(firstCycleStart.getTime()) + "/" + sdf.format(firstCycleEnd.getTime());
        cycles.add(cycle);

        // 进入下一个周期
        startCal.setTime(firstCycleEnd.getTime());
        startCal.add(Calendar.DAY_OF_MONTH, 1);

        // 遍历所有完整周期
        while (startCal.getTime().before(rightDate)) {
            Calendar cycleEndCal = (Calendar) startCal.clone();
            cycleEndCal.add(Calendar.DAY_OF_WEEK, 6); // 周日 +6天 = 周六

            // 如果当前周期的结束日超过 rightDate，调整为 rightDate
            if (cycleEndCal.getTime().after(rightDate)) {
                cycleEndCal.setTime(rightDate);
            }

            String cycle1 = sdf.format(startCal.getTime()) + "/" + sdf.format(cycleEndCal.getTime());
            cycles.add(cycle1);

            // 进入下一个周期
            startCal.add(Calendar.DAY_OF_MONTH, 7);
        }

        return cycles;
    }

    /**
     * 格式化金额
     */
    private String formatMoney(Object value) {
        if (value == null) {
            return "0";
        }
        try {
            double doubleValue = Double.parseDouble(value.toString());
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            return decimalFormat.format(doubleValue);
        } catch (NumberFormatException e) {
            log.error("金额格式转换失败: {}", value);
            return "0";
        }
    }
    
    /**
     * 构建商机查询SQL
     */
    private String buildOpportunityQuerySql(SupportHoursQueryDTO queryDTO) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(" oppo.id_ id, ");
        sql.append(" oppo.opp_code oppCode, ");
        sql.append(" oppo.opp_name oppName, ");
        sql.append(" oppo.cust_name custName, ");
        sql.append(" oppo.expect_contract_money expectContractMoney, ");
        sql.append(" oppo.unicom_type unicomType, ");
        sql.append(" oppo.cust_manager_name custManagerName, ");
        sql.append(" date_format(oppo.designer_time, '%Y-%m-%d %H:%i:%s') designerTime, ");
        sql.append(" ifnull(u1.name_ch, '未指定') firstManagerName, ");
        sql.append(" ifnull((select GROUP_CONCAT(CONCAT_WS(',', m1.name_) SEPARATOR ',') from topp_member m1 where m1.project_id = oppo.id_ and m1.member_id != ifnull(u1.id_,'00') and m1.member_role='member_role_zcjl' group by m1.project_id),'未指定') secondManagerName, ");
        sql.append(" if(oppo.support_stage_time is null, '1', ");
        sql.append("   if(TIMESTAMPDIFF(MONTH, oppo.support_stage_time, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')) >= 6, '1', ");
        sql.append("     if(oppo.support_stage is null, '1', oppo.support_stage) ");
        sql.append("   ) ");
        sql.append(" ) as supportStage ");
        sql.append(" FROM topp_opportunity oppo ");
        sql.append(" left join sys_user u1 ON u1.id_ = oppo.designer ");
        sql.append(" WHERE 1=1 ");
        
        // 支撑阶段条件
        if (queryDTO.getSupportStage() != null && !queryDTO.getSupportStage().isEmpty()) {
            if ("1".equals(queryDTO.getSupportStage())) {
                // 支撑关闭：支撑变更时间大于等于6个月或为空
                sql.append(" AND ( ");
                sql.append("   (oppo.support_stage_time is null) ");
                sql.append("   OR (oppo.support_stage_time is not null and TIMESTAMPDIFF(MONTH, oppo.support_stage_time, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')) >= 6) ");
                sql.append("   OR (oppo.support_stage_time is not null and TIMESTAMPDIFF(MONTH, oppo.support_stage_time, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')) < 6 and oppo.support_stage is null) ");
                sql.append(" ) ");
            } else {
                sql.append(" AND oppo.support_stage = '").append(queryDTO.getSupportStage()).append("'");
            }
        }
        
        // 商机到达时间条件
        if (queryDTO.getOpportunityLeftTime() != null && !queryDTO.getOpportunityLeftTime().isEmpty()) {
            sql.append(" AND DATE_FORMAT(oppo.designer_time, '%Y-%m-%d') >= '").append(queryDTO.getOpportunityLeftTime()).append("'");
        }
        if (queryDTO.getOpportunityRightTime() != null && !queryDTO.getOpportunityRightTime().isEmpty()) {
            sql.append(" AND DATE_FORMAT(oppo.designer_time, '%Y-%m-%d') <= '").append(queryDTO.getOpportunityRightTime()).append("'");
        }
        
        // 区域ID条件
        if (queryDTO.getAreaId() != null && !queryDTO.getAreaId().isEmpty()) {
            sql.append(" AND u1.org_id = '").append(queryDTO.getAreaId()).append("'");
        }
        
        sql.append(" ORDER BY oppo.designer_time DESC");
        
        return sql.toString();
    }
    
    /**
     * 查询task工时数据
     */
    private List<Map<String, Object>> queryTaskHours(Integer oppId, SupportHoursQueryDTO queryDTO) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(" t.support_hours supportHours, ");
        sql.append(" date_format(t.create_time, '%Y-%m-%d %H:%i:%s') createTime, ");
        sql.append(" t.task_name taskName, ");
        sql.append(" t.task_code taskCode, ");
        sql.append(" t.id_ taskId, ");
        sql.append(" t.actor_ createName ");
        sql.append(" FROM tcomm_task t ");
        sql.append(" left join sys_user u on u.id_ = t.actor_ ");
        sql.append(" WHERE t.actor_ is not null ");
        sql.append(" AND u.name_ch is not null ");
        sql.append(" AND u.is_valid = '1' ");
        sql.append(" AND t.support_hours != '' and t.support_hours is not null and CAST(t.support_hours AS DECIMAL) > 0 ");
        
        if (oppId != null) {
            sql.append(" AND t.info_id = '").append(oppId).append("'");
        }
        
        if (queryDTO.getLeftTime() != null && !queryDTO.getLeftTime().isEmpty()) {
            sql.append(" AND DATE_FORMAT(t.create_time, '%Y-%m-%d') >= '").append(queryDTO.getLeftTime()).append("'");
        }
        if (queryDTO.getRightTime() != null && !queryDTO.getRightTime().isEmpty()) {
            sql.append(" AND DATE_FORMAT(t.create_time, '%Y-%m-%d') <= '").append(queryDTO.getRightTime()).append("'");
        }
        
        sql.append(" ORDER BY t.create_time DESC");
        
        return jdbcTemplate.queryForList(sql.toString());
    }
    
    /**
     * 查询技术解析评审工时数据
     */
    private List<Map<String, Object>> queryProjectTechHours(Integer oppId, SupportHoursQueryDTO queryDTO) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(" t.support_hours supportHours, ");
        sql.append(" t.created_at createTime, ");
        sql.append(" t.process_order_number orderNum, ");
        sql.append(" u.name_ch createPersonName ");
        sql.append(" FROM project_tech_review t ");
        sql.append(" left join sys_user u on u.id_ = t.create_person ");
        sql.append(" WHERE t.create_person is not null ");
        sql.append(" AND u.name_ch is not null ");
        sql.append(" AND u.is_valid = '1' ");
        
        if (oppId != null) {
            sql.append(" AND t.oppo_id = '").append(oppId).append("'");
        }
        
        if (queryDTO.getLeftTime() != null && !queryDTO.getLeftTime().isEmpty()) {
            sql.append(" AND DATE_FORMAT(t.created_at, '%Y-%m-%d') >= '").append(queryDTO.getLeftTime()).append("'");
        }
        if (queryDTO.getRightTime() != null && !queryDTO.getRightTime().isEmpty()) {
            sql.append(" AND DATE_FORMAT(t.created_at, '%Y-%m-%d') <= '").append(queryDTO.getRightTime()).append("'");
        }
        
        sql.append(" ORDER BY t.created_at DESC");
        
        return jdbcTemplate.queryForList(sql.toString());
    }
    
    /**
     * 判断task流程是否结束
     */
    private boolean isTaskFlowEnded(Integer taskId) {
        if (taskId == null) {
            return false;
        }
        String sql = "SELECT count(1) FROM topp_order WHERE order_type='support_order' AND task_id = ? AND status_ = 'end'";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, taskId);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("判断task流程状态失败，taskId: {}", taskId, e);
            return false;
        }
    }
    
    /**
     * 判断技术解析评审流程是否结束
     */
    private boolean isProjectTechFlowEnded(Integer projectTechId) {
        if (projectTechId == null) {
            return false;
        }
        String sql = "SELECT count(1) FROM topp_order WHERE id_ = ? AND status_ = 'end'";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, projectTechId);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("判断技术解析评审流程状态失败，projectTechId: {}", projectTechId, e);
            return false;
        }
    }
    
    /**
     * 获取支撑阶段名称
     */
    private String getSupportStageName(String stageCode) {
        if (stageCode == null || stageCode.isEmpty()) {
            return "未知";
        }
        switch (stageCode) {
            case "5":
                return "待支撑";
            case "4":
                return "售前支撑";
            case "3":
                return "招投标/直接签约";
            case "2":
                return "售中交付";
            case "1":
                return "支撑关闭";
            default:
                return "未知";
        }
    }
    
    /**
     * 查找日期所属的周周期
     */
    private String findWeeklyCycle(Date leftDate, Date rightDate, Date targetDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(leftDate);
        startCal.setFirstDayOfWeek(Calendar.SUNDAY);
        
        Calendar targetCal = Calendar.getInstance();
        targetCal.setTime(targetDate);
        
        // 找到该周的周日和周六
        Calendar cycleStart = (Calendar) targetCal.clone();
        cycleStart.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        
        Calendar cycleEnd = (Calendar) targetCal.clone();
        cycleEnd.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        
        return sdf.format(cycleStart.getTime()) + "/" + sdf.format(cycleEnd.getTime());
    }
    
    /**
     * 安全获取String值
     */
    private String getStringValue(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
    
    /**
     * 安全获取Integer值
     */
    private Integer getIntValue(Object value) {
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.warn("转换为Integer失败: {}", value);
            return null;
        }
    }
    
    /**
     * 安全获取Double值
     */
    private Double getDoubleValue(Object value) {
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            log.warn("转换为Double失败: {}", value);
            return null;
        }
    }
    
    /**
     * 构建支撑经理查询SQL
     */
    private String buildManagerQuerySql(SupportHoursQueryDTO queryDTO) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.userId, b.oppoId FROM (");
        sql.append(" SELECT oppo.id_ as oppoId, a.userId, max(oppo.designer_time) as designerTime FROM (");
        
        // tcomm_task表查询
        sql.append(buildTcommTaskSupportSql(queryDTO));
        sql.append(" UNION ALL ");
        // project_tech_review表查询
        sql.append(buildProjectTechReviewSupportSql(queryDTO));
        
        sql.append(" ) a ");
        sql.append(" LEFT JOIN topp_opportunity oppo ON oppo.id_ = a.oppoId ");
        sql.append(" LEFT JOIN sys_user u1 ON u1.id_ = oppo.designer ");
        sql.append(" WHERE 1=1 ");
        
        // 区域ID条件
        if (queryDTO.getAreaId() != null && !queryDTO.getAreaId().isEmpty()) {
            sql.append(" AND u1.org_id = '").append(queryDTO.getAreaId()).append("'");
        }
        
        sql.append(" GROUP BY a.userId, a.oppoId ) b ");
        sql.append(" ORDER BY b.designerTime DESC");
        
        return sql.toString();
    }
    
    /**
     * 构建tcomm_task支撑查询SQL
     */
    private String buildTcommTaskSupportSql(SupportHoursQueryDTO queryDTO) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT t.actor_ as userId, t.info_id as oppoId ");
        sql.append(" FROM tcomm_task t ");
        sql.append(" LEFT JOIN sys_user u ON u.id_ = t.actor_ ");
        sql.append(" LEFT JOIN topp_order ord ON ord.order_type='support_order' AND ord.status_ = 'end' AND ord.task_id = t.id_ ");
        sql.append(" WHERE t.actor_ IS NOT NULL ");
        sql.append(" AND u.name_ch IS NOT NULL ");
        sql.append(" AND u.is_valid = '1' ");
        sql.append(" AND t.task_code != 'onlineProgram' ");
        sql.append(" AND t.support_hours != '' AND t.support_hours IS NOT NULL AND CAST(t.support_hours AS DECIMAL) > 0 ");
        sql.append(" AND t.info_id IS NOT NULL ");
        sql.append(" AND t.create_ IS NOT NULL ");
        sql.append(" AND ord.id_ IS NOT NULL ");
        
        // 支撑经理姓名条件
        if (queryDTO.getSupportManagerName() != null && !queryDTO.getSupportManagerName().isEmpty()) {
            sql.append(" AND u.name_ch = '").append(queryDTO.getSupportManagerName()).append("'");
        }
        
        // 工时统计时间条件
        if (queryDTO.getLeftTime() != null && !queryDTO.getLeftTime().isEmpty()) {
            sql.append(" AND DATE_FORMAT(t.create_time, '%Y-%m-%d') >= '").append(queryDTO.getLeftTime()).append("'");
        }
        if (queryDTO.getRightTime() != null && !queryDTO.getRightTime().isEmpty()) {
            sql.append(" AND DATE_FORMAT(t.create_time, '%Y-%m-%d') <= '").append(queryDTO.getRightTime()).append("'");
        }
        
        return sql.toString();
    }
    
    /**
     * 构建project_tech_review支撑查询SQL
     */
    private String buildProjectTechReviewSupportSql(SupportHoursQueryDTO queryDTO) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT p.create_person as userId, p.oppo_id as oppoId ");
        sql.append(" FROM project_tech_review p ");
        sql.append(" LEFT JOIN sys_user u ON u.id_ = p.create_person ");
        sql.append(" LEFT JOIN topp_order ord ON p.process_order_number = ord.id_ AND ord.status_ = 'end' ");
        sql.append(" WHERE p.create_person IS NOT NULL ");
        sql.append(" AND p.support_hours != '' AND p.support_hours IS NOT NULL AND CAST(p.support_hours AS DECIMAL) > 0 ");
        sql.append(" AND u.name_ch IS NOT NULL ");
        sql.append(" AND u.is_valid = '1' ");
        sql.append(" AND ord.id_ IS NOT NULL ");
        
        // 支撑经理姓名条件
        if (queryDTO.getSupportManagerName() != null && !queryDTO.getSupportManagerName().isEmpty()) {
            sql.append(" AND u.name_ch = '").append(queryDTO.getSupportManagerName()).append("'");
        }
        
        // 工时统计时间条件
        if (queryDTO.getLeftTime() != null && !queryDTO.getLeftTime().isEmpty()) {
            sql.append(" AND DATE_FORMAT(p.created_at, '%Y-%m-%d') >= '").append(queryDTO.getLeftTime()).append("'");
        }
        if (queryDTO.getRightTime() != null && !queryDTO.getRightTime().isEmpty()) {
            sql.append(" AND DATE_FORMAT(p.created_at, '%Y-%m-%d') <= '").append(queryDTO.getRightTime()).append("'");
        }
        
        return sql.toString();
    }
    
    /**
     * 查询商机详细信息
     */
    private Map<String, Object> queryOpportunityById(Integer oppoId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(" oppo.id_ id, ");
        sql.append(" oppo.opp_code oppCode, ");
        sql.append(" oppo.opp_name oppName, ");
        sql.append(" oppo.cust_name custName, ");
        sql.append(" oppo.expect_contract_money expectContractMoney, ");
        sql.append(" oppo.unicom_type unicomType, ");
        sql.append(" oppo.cust_manager_name custManagerName, ");
        sql.append(" date_format(oppo.designer_time, '%Y-%m-%d %H:%i:%s') designerTime, ");
        sql.append(" ifnull(u1.name_ch, '未指定') firstManagerName, ");
        sql.append(" ifnull((select GROUP_CONCAT(CONCAT_WS(',', m1.name_) SEPARATOR ',') from topp_member m1 where m1.project_id = oppo.id_ and m1.member_id != ifnull(u1.id_,'00') and m1.member_role='member_role_zcjl' group by m1.project_id),'未指定') secondManagerName, ");
        sql.append(" if(oppo.support_stage_time is null, '1', ");
        sql.append("   if(TIMESTAMPDIFF(MONTH, oppo.support_stage_time, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')) >= 6, '1', ");
        sql.append("     if(oppo.support_stage is null, '1', oppo.support_stage) ");
        sql.append("   ) ");
        sql.append(" ) as supportStage ");
        sql.append(" FROM topp_opportunity oppo ");
        sql.append(" LEFT JOIN sys_user u1 ON u1.id_ = oppo.designer ");
        sql.append(" WHERE oppo.id_ = ?");
        
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), oppoId);
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            log.error("查询商机详情失败，oppoId: {}", oppoId, e);
            return null;
        }
    }
    
    /**
     * 查询用户姓名
     */
    private String queryUserName(Integer userId) {
        if (userId == null) {
            return null;
        }
        String sql = "SELECT name_ch FROM sys_user WHERE id_ = ? AND IS_VALID = '1'";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, userId);
        } catch (Exception e) {
            log.error("查询用户姓名失败，userId: {}", userId, e);
            return null;
        }
    }
    
    /**
     * 查询task工时数据（按支撑经理）
     */
    private List<Map<String, Object>> queryTaskHoursByManager(Integer oppId, Integer userId, SupportHoursQueryDTO queryDTO) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(" t.support_hours supportHours, ");
        sql.append(" date_format(t.create_time, '%Y-%m-%d %H:%i:%s') createTime, ");
        sql.append(" t.task_name taskName, ");
        sql.append(" t.task_code taskCode, ");
        sql.append(" t.id_ taskId, ");
        sql.append(" t.actor_ ");
        sql.append(" FROM tcomm_task t ");
        sql.append(" LEFT JOIN sys_user u ON u.id_ = t.actor_ ");
        sql.append(" LEFT JOIN topp_order ord ON ord.order_type='support_order' AND ord.status_ = 'end' AND ord.task_id = t.id_ ");
        sql.append(" WHERE 1=1 ");
        sql.append(" AND t.info_id = ? ");
        sql.append(" AND t.actor_ = ? ");
        sql.append(" AND ord.id_ IS NOT NULL ");
        sql.append(" AND t.support_hours != '' AND t.support_hours IS NOT NULL AND CAST(t.support_hours AS DECIMAL) > 0 ");
        
        if (queryDTO.getSupportManagerName() != null && !queryDTO.getSupportManagerName().isEmpty()) {
            sql.append(" AND u.name_ch = '").append(queryDTO.getSupportManagerName()).append("'");
        }
        
        if (queryDTO.getLeftTime() != null && !queryDTO.getLeftTime().isEmpty()) {
            sql.append(" AND DATE_FORMAT(t.create_time, '%Y-%m-%d') >= '").append(queryDTO.getLeftTime()).append("'");
        }
        if (queryDTO.getRightTime() != null && !queryDTO.getRightTime().isEmpty()) {
            sql.append(" AND DATE_FORMAT(t.create_time, '%Y-%m-%d') <= '").append(queryDTO.getRightTime()).append("'");
        }
        
        sql.append(" ORDER BY t.create_time DESC");
        
        return jdbcTemplate.queryForList(sql.toString(), oppId, userId);
    }
    
    /**
     * 查询技术解析评审工时数据（按支撑经理）
     */
    private List<Map<String, Object>> queryProjectTechHoursByManager(Integer oppId, Integer userId, SupportHoursQueryDTO queryDTO) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(" t.support_hours supportHours, ");
        sql.append(" t.created_at createTime, ");
        sql.append(" t.process_order_number orderNum, ");
        sql.append(" u.name_ch createPersonName ");
        sql.append(" FROM project_tech_review t ");
        sql.append(" LEFT JOIN sys_user u ON u.id_ = t.create_person ");
        sql.append(" LEFT JOIN topp_order ord ON t.process_order_number = ord.id_ AND ord.status_ = 'end' ");
        sql.append(" WHERE 1=1 ");
        sql.append(" AND t.oppo_id = ? ");
        sql.append(" AND t.create_person = ? ");
        sql.append(" AND ord.id_ IS NOT NULL ");
        sql.append(" AND t.support_hours != '' AND t.support_hours IS NOT NULL AND CAST(t.support_hours AS DECIMAL) > 0 ");
        
        if (queryDTO.getSupportManagerName() != null && !queryDTO.getSupportManagerName().isEmpty()) {
            sql.append(" AND u.name_ch = '").append(queryDTO.getSupportManagerName()).append("'");
        }
        
        if (queryDTO.getLeftTime() != null && !queryDTO.getLeftTime().isEmpty()) {
            sql.append(" AND DATE_FORMAT(t.created_at, '%Y-%m-%d') >= '").append(queryDTO.getLeftTime()).append("'");
        }
        if (queryDTO.getRightTime() != null && !queryDTO.getRightTime().isEmpty()) {
            sql.append(" AND DATE_FORMAT(t.created_at, '%Y-%m-%d') <= '").append(queryDTO.getRightTime()).append("'");
        }
        
        sql.append(" ORDER BY t.created_at DESC");
        
        return jdbcTemplate.queryForList(sql.toString(), oppId, userId);
    }
}
