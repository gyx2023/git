package com.crm.job.service.workflow;

import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.StringUtils;
import com.crm.flowable.factory.FlowServiceFactory;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.service.ISequenceService;
import com.crm.system.service.sys.ISysDeptService;
import com.crm.system.service.sys.ISysUserService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.system.utils.OppClueCodeUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 宽带预警流程启动任务
 * 迁移自老系统 SecondWarningJob.wideSecondJob + WarningUpJob.wideWarningUpJob
 *
 * 合并为一个统一的BPMN流程 wide_warning_process，通过orderType区分类型：
 * - wide_second: 宽带二次预警（推送给客户经理）
 * - new_wide_up: 宽带升级预警（推送给总经理/副总经理）
 */
@Slf4j
@Component
public class WideWarningStartJob extends FlowServiceFactory {

    private static final String PROCESS_KEY = "common_warning_process";

    private static final String ORDER_TYPE_WIDE_SECOND = "wide_second";
    private static final String ORDER_TYPE_WIDE_SECOND_NAME = "宽带二次预警";

    private static final String ORDER_TYPE_NEW_WIDE_UP = "new_wide_up";
    private static final String ORDER_TYPE_NEW_WIDE_UP_NAME = "宽带升级预警";

    private static final String ORDER_STATUS_RUNNING = "running";

    protected static final ExecutorService executorService = new ThreadPoolExecutor(
            10, 10, 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());

    @Autowired
    private IToppOrderService orderService;

    @Autowired
    private ISequenceService sequenceService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 宽带二次预警任务
     * 查询即将到期的宽带用户，为客户经理生成预警工单
     */
    @XxlJob("wideSecondJob")
    public void wideSecondJob() {
        log.info("【宽带二次预警】开始执行");
        String yyyyMMdd = getDefaultDate(15);
        String jobParam = XxlJobHelper.getJobParam();
        if (StringUtils.isNotEmpty(jobParam)) {
            yyyyMMdd = jobParam;
        }
        try {
            List<Map<String, Object>> warningList = querySecondWarnData(yyyyMMdd);
            log.info("【宽带二次预警】查询到 {} 条预警数据", warningList.size());

            int count = 0;
            for (Map<String, Object> row : warningList) {
                try {
                    createWarningOrder(row, ORDER_TYPE_WIDE_SECOND, ORDER_TYPE_WIDE_SECOND_NAME, yyyyMMdd);
                    count++;
                } catch (Exception e) {
                    log.error("【宽带二次预警】创建工单失败，warningId: {}", row.get("id"), e);
                }
            }
            log.info("【宽带二次预警】完成，共创建 {} 个工单", count);
            XxlJobHelper.handleSuccess("处理完成，共创建 " + count + " 个工单");
        } catch (Exception e) {
            log.error("【宽带二次预警】执行异常", e);
            XxlJobHelper.handleFail("执行异常：" + e.getMessage());
        }
    }

    /**
     * 宽带升级预警任务
     * 查询各地市宽带到期情况，为地市领导（总经理/副总经理）生成升级预警工单
     */
    @XxlJob("wideWarningUpJob")
    public void wideWarningUp() {
        log.info("【宽带升级预警】开始执行");
        String yyyyMM = getDefaultMonth(-1);
        String jobParam = XxlJobHelper.getJobParam();
        if (StringUtils.isNotEmpty(jobParam)) {
            yyyyMM = jobParam;
        }
        try {
            List<Map<String, Object>> warningAreas = queryWideWarningAreas(yyyyMM);
            log.info("【宽带升级预警】查询到 {} 个预警区域", warningAreas.size());

            int count = 0;
            for (Map<String, Object> area : warningAreas) {
                try {
                    count += createWideUpOrders(area, yyyyMM);
                } catch (Exception e) {
                    log.error("【宽带升级预警】创建工单失败，areaId: {}", area.get("id"), e);
                }
            }
            log.info("【宽带升级预警】完成，共创建 {} 个工单", count);
            XxlJobHelper.handleSuccess("处理完成，共创建 " + count + " 个工单");
        } catch (Exception e) {
            log.error("【宽带升级预警】执行异常", e);
            XxlJobHelper.handleFail("执行异常：" + e.getMessage());
        }
    }

    /**
     * 创建宽带二次预警工单（单个客户经理）
     */
    private void createWarningOrder(Map<String, Object> warningRow, String orderType,
                                    String orderTypeName, String period) throws Exception {
        Long infoId = getLongValue(warningRow.get("id"));
        String grpName = getStringValue(warningRow.get("grp_name"));
        String grpCode = getStringValue(warningRow.get("grp_code"));
        String oaIdDev = getStringValue(warningRow.get("oa_id_dev"));
        String servManagerId = getStringValue(warningRow.get("serv_manager_id"));

        // 查找客户经理
        SysUser manager = findUserByOaId(oaIdDev);
        if (manager == null) {
            log.warn("【宽带二次预警】无法找到客户经理，oaId: {}", oaIdDev);
            return;
        }

        // 检查是否已存在工单（避免重复创建）
        ToppOrderBo queryBo = new ToppOrderBo();
        queryBo.setInfoId(infoId);
        queryBo.setOrderType(orderType);
        if (orderService.queryList(queryBo).size() > 0) {
            log.info("【宽带二次预警】已存在工单，跳过。infoId: {}", infoId);
            return;
        }

        String orderName = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, new Date())
                + "_" + grpName + "_" + orderTypeName;

        // 启动流程实例
        Map<String, Object> startVariables = new HashMap<>();
        startVariables.put("handler", "," + manager.getUserId() + ",");
        startVariables.put("warning_type", orderType);

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(PROCESS_KEY).latestVersion().singleResult();
        if (processDefinition == null) {
            log.warn("【宽带二次预警】未找到流程定义: {}", PROCESS_KEY);
            return;
        }

        identityService.setAuthenticatedUserId(manager.getUserId().toString());
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(
                processDefinition.getId(), startVariables);
        String piId = processInstance.getId();

        Task task = taskService.createTaskQuery().processInstanceId(piId).singleResult();
        if (task == null) {
            log.warn("【宽带二次预警】流程实例 {} 没有待处理任务", piId);
            return;
        }

        // 生成订单编号并创建工单
        String orderNumber = sequenceService.getCode(OppClueCodeUtil.getOrderCode());
        ToppOrderBo orderBo = buildOrderBo(manager, infoId, grpName, piId, orderType, orderName,
                orderNumber, task.getTaskDefinitionKey(), task.getName());
        orderBo.setOrderCategory("C");
        orderService.insertByBo(orderBo);

        // 设置任务变量
        Map<String, Object> taskVariables = new HashMap<>();
        taskVariables.put(task.getTaskDefinitionKey() + "_dealUrl",
                "/userCustWarning/dealByWideSecond?orderId=" + orderBo.getId());
        taskVariables.put(task.getTaskDefinitionKey() + "_infoId", orderBo.getId());
        taskVariables.put(task.getTaskDefinitionKey() + "_infoType", "order");
        taskService.setVariablesLocal(task.getId(), taskVariables);

        // 保存周期变量
        Map<String, Object> procVars = new HashMap<>();
        procVars.put("wide_second-date", period);
        runtimeService.setVariables(piId, procVars);

        log.info("【宽带二次预警】工单创建成功，orderId: {}, piId: {}, manager: {}",
                orderBo.getId(), piId, manager.getNickName());
    }

    /**
     * 为单个区域/组织创建宽带升级预警工单
     */
    private int createWideUpOrders(Map<String, Object> area, String yyyyMM) throws Exception {
        Long areaId = getLongValue(area.get("id"));
        String areaName = getStringValue(area.get("name_"));
        String bssCode = getStringValue(area.get("bss_code"));

        // 查找领导（总经理 → 副总经理 → 地市接口人）
        List<SysUser> leaders = findLeadersForArea(bssCode);
        if (leaders.isEmpty()) {
            log.warn("【宽带升级预警】未找到领导，区域: {}", areaName);
            return 0;
        }

        int count = 0;
        for (SysUser leader : leaders) {
            String orderName = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, new Date())
                    + areaName + "_账期" + yyyyMM + "_" + ORDER_TYPE_NEW_WIDE_UP_NAME;

            Map<String, Object> startVariables = new HashMap<>();
            startVariables.put("handler", "," + leader.getUserId() + ",");
            startVariables.put("warning_type", ORDER_TYPE_NEW_WIDE_UP);

            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(PROCESS_KEY).latestVersion().singleResult();
            if (processDefinition == null) {
                log.warn("【宽带升级预警】未找到流程定义: {}", PROCESS_KEY);
                return count;
            }

            identityService.setAuthenticatedUserId(leader.getUserId().toString());
            ProcessInstance processInstance = runtimeService.startProcessInstanceById(
                    processDefinition.getId(), startVariables);
            String piId = processInstance.getId();

            Task task = taskService.createTaskQuery().processInstanceId(piId).singleResult();
            if (task == null) {
                log.warn("【宽带升级预警】流程实例 {} 没有待处理任务", piId);
                continue;
            }

            String orderNumber = sequenceService.getCode(OppClueCodeUtil.getOrderCode());
            ToppOrderBo orderBo = buildOrderBo(leader, null, areaName, piId, ORDER_TYPE_NEW_WIDE_UP,
                    orderName, orderNumber, task.getTaskDefinitionKey(), task.getName());
            orderBo.setOrderCategory("C");
            orderService.insertByBo(orderBo);

            // 设置任务变量
            Map<String, Object> taskVariables = new HashMap<>();
            taskVariables.put(task.getTaskDefinitionKey() + "_dealUrl",
                    "/userCustWarning/dealByWideUp?orderId=" + orderBo.getId() + "&period=" + yyyyMM);
            taskVariables.put(task.getTaskDefinitionKey() + "_infoId", orderBo.getId());
            taskVariables.put(task.getTaskDefinitionKey() + "_infoType", "order");
            taskService.setVariablesLocal(task.getId(), taskVariables);

            // 保存区域相关流程变量
            Map<String, Object> procVars = new HashMap<>();
            procVars.put("wide_second_new_orderFlag", "1");
            procVars.put("wide_second_new_codeInfo", bssCode);
            procVars.put("wide_second_new_period", yyyyMM);
            runtimeService.setVariables(piId, procVars);

            count++;
            log.info("【宽带升级预警】工单创建成功，orderId: {}, leader: {}, area: {}",
                    orderBo.getId(), leader.getNickName(), areaName);
        }
        return count;
    }

    /**
     * 构建工单BO
     */
    private ToppOrderBo buildOrderBo(SysUser handler, Long infoId, String custName, String piId,
                                     String orderType, String orderName, String orderNumber,
                                     String phase, String phaseName) {
        ToppOrderBo orderBo = new ToppOrderBo();
        orderBo.setCustManagerId(handler.getUserId());
        orderBo.setCreator(handler.getUserId());
        orderBo.setCustName(custName);
        orderBo.setPiId(piId);
        orderBo.setOrderType(orderType);
        orderBo.setOrderName(orderName);
        orderBo.setOrderNumber(orderNumber);
        orderBo.setStatus(ORDER_STATUS_RUNNING);
        orderBo.setPhase(phase);
        orderBo.setPhaseName(phaseName);
        orderBo.setOrgId(handler.getDeptId());
        orderBo.setActors(",," + handler.getUserId() + ",,");
        orderBo.setWhiteList("," + handler.getUserId() + ",");
        orderBo.setActorsInfo(handler.getNickName() + ":" + handler.getPhonenumber());
        orderBo.setInfoId(infoId);
        orderBo.setProcessEngine("FLOWABLE");
        orderBo.setCreateTime(new Date());
        orderBo.setUpdateTime(new Date());
        if (handler.getDeptId() != null) {
            SysDept dept = deptService.selectDeptById(handler.getDeptId());
            if (dept != null && StringUtils.isNotEmpty(dept.getAreaCode())) {
                orderBo.setAreaId(Long.valueOf(dept.getAreaCode()));
            }
        }
        return orderBo;
    }

    // ==================== 数据查询 ====================

    /**
     * 查询宽带二次预警数据（对应老系统 getSecondWarnTask）
     */
    private List<Map<String, Object>> querySecondWarnData(String date) {
        String sql = "SELECT w.id, w.grp_name, w.grp_code, w.oa_id_dev, w.serv_manager_id, w.warning_type " +
                "FROM ftp_tc_user_cust_warning w " +
                "WHERE w.warning_type = 'C00002' " +
                "AND w.is_valid = '1' " +
                "AND DATE_FORMAT(w.exp_time, '%Y-%m-%d') >= ? " +
                "ORDER BY w.id";
        try {
            return jdbcTemplate.queryForList(sql, date);
        } catch (Exception e) {
            log.error("查询宽带二次预警数据失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询宽带升级预警区域列表（对应老系统 getWideWarningArealist）
     */
    private List<Map<String, Object>> queryWideWarningAreas(String yyyyMM) {
        String sql = "SELECT DISTINCT a.id_, a.name_, a.bss_code " +
                "FROM sys_area a " +
                "WHERE a.bss_code IS NOT NULL " +
                "ORDER BY a.id_";
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("查询宽带升级预警区域数据失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 查找区域的领导（总经理 → 副总经理）
     * 对应老系统 WarningUpJob.startTaskWarnUp 中的领导查询逻辑
     */
    private List<SysUser> findLeadersForArea(String bssCode) {
        // 简化版：直接通过组织+角色查找领导
        // 完整版需要查询 dwd_rep_sys_person_d 表，这里先提供基础实现
        List<SysUser> leaders = new ArrayList<>();
        try {
            String sql = "SELECT u.* FROM sys_user u " +
                    "JOIN sys_user_role ur ON u.user_id = ur.user_id " +
                    "JOIN sys_role r ON ur.role_id = r.role_id " +
                    "WHERE r.role_key IN ('boss', 'vice_boss') AND u.del_flag = '0' " +
                    "LIMIT 1";
            // 使用JdbcTemplate查询后组装SysUser，或直接用MyBatis-Plus查询
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            for (Map<String, Object> row : rows) {
                Long userId = getLongValue(row.get("user_id"));
                if (userId != null) {
                    SysUser user = sysUserService.selectUserById(userId);
                    if (user != null) {
                        leaders.add(user);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("查找领导失败，bssCode: {}", bssCode, e);
        }
        return leaders;
    }

    /**
     * 通过OA账号查找用户（对应老系统 findByOaId）
     */
    private SysUser findUserByOaId(String oaId) {
        if (StringUtils.isEmpty(oaId)) {
            return null;
        }
        try {
            return sysUserService.selectUserByUserName(oaId);
        } catch (Exception e) {
            log.warn("查找用户失败，oaId: {}", oaId, e);
            return null;
        }
    }

    // ==================== 工具方法 ====================

    private String getDefaultDate(int daysToAdd) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, daysToAdd);
        return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
    }

    private String getDefaultMonth(int monthsToAdd) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, monthsToAdd);
        return new SimpleDateFormat("yyyy-MM").format(c.getTime());
    }

    private String getStringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private Long getLongValue(Object value) {
        if (value == null) return null;
        try {
            if (value instanceof Number) return ((Number) value).longValue();
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
