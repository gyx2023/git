package com.crm.job.service.warning;

import com.crm.system.service.warning.IOrderDealUrlBuilder;
import com.crm.system.service.warning.WarningOrderCreateService;
import com.crm.system.service.warning.WarningRecord;

import com.crm.common.enums.order.ProcessEngineEnum;
import com.crm.common.utils.DateUtils;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.dao.topp.ToppOrderUp;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.service.ISequenceService;
import com.crm.system.service.sys.ISysDeptService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.system.service.topp.IToppOrderUpService;
import com.crm.system.service.send.ISendDMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;

/**
 * 预警流程统一启动 Service 实现
 * <p>
 * 所有预警类型统一使用 single_approval BPMN 流程。
 * 策略只负责数据查询+字段映射，类型差异通过 WarningRecord.extraVariables 传递。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Service
public class WarningStartServiceImpl implements IWarningStartService {

    /** single_approval 流程定义标识 */
    private static final String PROCESS_KEY = "single_approval";
    private static final String ORDER_STATUS_RUNNING = com.crm.common.enums.OrderStatusEnum.RUNNING.getCode();

    @Resource(name = "mdcTaskExecutor")
    private Executor executorService;

    @Autowired
    private IToppOrderService orderService;

    @Autowired
    private List<IWarningQueryStrategy> strategies;

    @Autowired(required = false)
    private ISequenceService sequenceService;

    @Autowired(required = false)
    private ISysDeptService deptService;

    @Autowired(required = false)
    private IToppOrderUpService toppOrderUpService;

    @Autowired(required = false)
    private SysUsersMapper sysUsersMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private ISendDMessageService sendDMessageService;

    @Autowired
    private WarningOrderCreateService warningOrderCreateService;

    @Override
    public void startByType(String orderType, String targetDate) {
        IWarningQueryStrategy strategy = findStrategy(orderType);
        if (strategy == null) {
            log.warn("【预警启动】未找到预警类型 {} 对应的策略", orderType);
            return;
        }

        List<WarningRecord> records = strategy.query(targetDate);
        log.info("【预警启动】类型={}, 待处理记录={}条", orderType, records.size());

        if (records.isEmpty()) {
            return;
        }

        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(records.size());
        java.util.Map<Long, Integer> managerTaskCount = new java.util.concurrent.ConcurrentHashMap<>();

        for (WarningRecord record : records) {
            // 将 targetDate 传递给 createAndStartOrder（部分类型需设为流程变量）
            Map<String, Object> extra = record.getExtraVariables();
            if (extra == null) {
                extra = new HashMap<>();
                record.setExtraVariables(extra);
            }
            if (!extra.containsKey("_targetDate")) {
                extra.put("_targetDate", targetDate);
            }
            executorService.execute(() -> {
                try {
                    createAndStartOrder(record);
                    // 统计客户经理处理条数（用于通知）
                    managerTaskCount.merge(record.getCustManagerId(), 1, Integer::sum);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
            // 发送通知（对应旧系统 sendDMessageService.initNoticeMessageByWx）
            if (strategy.isNotificationEnabled() && sendDMessageService != null) {
                for (Map.Entry<Long, Integer> entry : managerTaskCount.entrySet()) {
                    sendDMessageService.initNoticeMessage(
                            new java.util.ArrayList<>(java.util.Collections.singletonList(entry.getKey().intValue())),
                            strategy.getNotificationMessage(entry.getValue()));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("【预警启动】等待任务完成被中断", e);
        }
    }

    @Override
    public void startAll(String targetDate) {
        for (IWarningQueryStrategy strategy : strategies) {
            try {
                startByType(strategy.getOrderType(), targetDate);
            } catch (Exception e) {
                log.error("【预警启动】类型 {} 启动失败", strategy.getOrderType(), e);
            }
        }
    }

    @Override
    public List<IWarningQueryStrategy> getStrategies() {
        return strategies;
    }

    private IWarningQueryStrategy findStrategy(String orderType) {
        if (StringUtils.isBlank(orderType)) {
            return null;
        }
        for (IWarningQueryStrategy strategy : strategies) {
            if (orderType.equals(strategy.getOrderType())) {
                return strategy;
            }
        }
        return null;
    }

    // ==================== single_approval 流程启动 ====================

    /**
     * 创建预警工单并启动 single_approval 通用审批流程
     * <p>
     * 变量：manager（处理人）、action（agree/reject）
     * 附加信息由 WarningRecord.extraVariables 传入：
     * - infoId: 原始记录ID（用于去重）
     * - orgId/nickName/phone: 用于工单 orgId、actorsInfo、areaId
     * - wide_second-date 等: 写入运行时流程变量
     * <p>
     * 流程启动 + 取任务 + 建单 + 展示/运行时变量统一走公共组件
     * {@link WarningOrderCreateService}，此处仅保留策略钩子与专属步骤。
     * </p>
     */
    protected void createAndStartOrder(WarningRecord record) {
        try {
            Map<String, Object> extra = record.getExtraVariables();

            // 1. 去重检查
            if (isDuplicate(record, extra)) return;

            // 1.5 策略前置处理（可选）
            IWarningQueryStrategy strategy = findStrategy(record.getOrderType());
            if (strategy != null) {
                strategy.beforeStart(record, String.valueOf(extra != null ? extra.get("_targetDate") : ""));
            }

            // 2. 公共建单组件：启动流程（manager=客户经理）+ 取首任务 + 建单 + 展示变量 + 运行时变量
            String processKey = strategy != null ? strategy.getProcessKey() : PROCESS_KEY;
            Long managerId = record.getCustManagerId();
            IOrderDealUrlBuilder dealUrlBuilder = (orderId, e) -> strategy != null
                    ? strategy.buildDealUrl(orderId, e)
                    : "/warning/toDeal?orderType=" + record.getOrderType() + "&orderId=" + orderId;
            ToppOrderBo orderBo = warningOrderCreateService.createAndStartOrder(record, processKey, managerId, dealUrlBuilder, null);
            if (orderBo == null) return;
            String piId = orderBo.getPiId();

            // 3. 两节点流程：预置稽核节点流程变量（dealUrl/节点名等，稽核任务创建时老系统从 FLO_ACT_RU_VARIABLE 读取）
            setAuditVariablesIfNeeded(piId, strategy, orderBo, record, extra);

            // 4. 保存升级预警关联表（仅针对升级预警类型）
            saveToppOrderUpIfNeeded(orderBo, extra);

            // 5. 策略后置处理（可选）
            if (strategy != null) {
                strategy.afterStart(record, orderBo);
            }

            log.info("【预警启动】工单已创建: type={}, orderId={}, piId={}",
                    record.getOrderType(), orderBo.getId(), piId);
        } catch (Exception e) {
            log.error("【预警启动】创建工单失败: type={}", record.getOrderType(), e);
        }
    }

    private boolean isDuplicate(WarningRecord record, Map<String, Object> extra) {
        Long infoId = extra != null ? (Long) extra.get("infoId") : null;
        if (infoId == null) return false;

        ToppOrderBo queryBo = new ToppOrderBo();
        queryBo.setInfoId(infoId);
        queryBo.setOrderType(record.getOrderType());
        if (orderService.queryList(queryBo).size() > 0) {
            log.info("【预警启动】已存在工单，跳过。infoId: {}, type: {}", infoId, record.getOrderType());
            return true;
        }
        return false;
    }

    /**
     * 两节点流程（如 double_approval）：设置稽核节点显示名变量。
     * <p>
     * 稽核人不在发起时固化：由客户经理在"通过"提交时从候选列表中必选一人
     * （/wideWarning/auditHandlers，对齐旧系统 getNextUserListByNodeAndUserArea），
     * 随任务完成写入流程变量 {@code audit}。
     * </p>
     */
    private void setAuditVariablesIfNeeded(String piId, IWarningQueryStrategy strategy,
                                            ToppOrderBo orderBo, WarningRecord record, Map<String, Object> extra) {
        if (strategy == null || PROCESS_KEY.equals(strategy.getProcessKey())) {
            return;
        }
        Map<String, Object> vars = new HashMap<>();
        vars.put("audit_handle_name", "稽核人员处理");
        vars.put("audit_handle_dealUrl", strategy.buildDealUrl(orderBo.getId(), extra));
        vars.put("audit_handle_infoId", String.valueOf(orderBo.getId()));
        vars.put("audit_handle_infoType", "order");
        if (StringUtils.isNotBlank(record.getCustName())) {
            vars.put("audit_handle_custName", record.getCustName());
        }
        warningOrderCreateService.setProcessVariables(piId, vars);
    }

    /**
     * 保存升级预警订单关联表（仅针对升级预警类型）
     * 对应旧系统 WarningUpJob.startProcessWarnUp() 中的 orderUpService.saveOrUpdate()
     */
    private void saveToppOrderUpIfNeeded(ToppOrderBo orderBo, Map<String, Object> extra) {
        if (toppOrderUpService == null) {
            log.debug("【升级预警关联表】IToppOrderUpService 未注入，跳过保存");
            return;
        }

        // 仅处理升级预警类型
        String orderType = orderBo.getOrderType();
        if (!"new_wide_up".equals(orderType) && !"new_hmd_up".equals(orderType)) {
            return;
        }

        String bssCode = extra != null ? (String) extra.get("bssCode") : null;
        String mdmOrgCode = extra != null ? (String) extra.get("mdmOrgCode") : null;
        String period = extra != null ? (String) extra.get("wide_second_new_period") : null;

        // 如果 period 为空，尝试从 _targetDate 获取
        if (StringUtils.isBlank(period)) {
            period = extra != null ? (String) extra.get("_targetDate") : null;
        }

        ToppOrderUp orderUp = new ToppOrderUp();
        orderUp.setOrderId(orderBo.getId() != null ? orderBo.getId().intValue() : null);
        orderUp.setBssCode(bssCode);
        orderUp.setMdmOrgCode(mdmOrgCode);
        orderUp.setPeriod(period);

        toppOrderUpService.insertToppOrderUp(orderUp);
    }

    // ==================== sys_flow_user 默认处理人查询 ====================

    @Override
    public List<SysUser> findDefaultHandlerByProcessAndArea(String processKey, String taskDefKey, Long areaId) {
        // 构建基础SQL（使用占位符防止SQL注入）
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT u.user_id, u.dept_id, u.user_name, u.nick_name, u.phonenumber " +
                "FROM sys_flow_user fu " +
                "INNER JOIN zqzt_sys_user u ON fu.user_id = u.user_id AND u.del_flag = '0' ");

        List<Object> params = new ArrayList<>();

        // 如果指定了区域ID，添加部门关联和区域过滤条件
        if (areaId != null) {
            sql.append("INNER JOIN zqzt_sys_dept d ON u.dept_id = d.dept_id AND d.area_code = ? ");
            params.add(String.valueOf(areaId));
            sql.append("WHERE fu.is_valid = 1 AND fu.process_key = ? AND fu.task_def_key = ? ");
            sql.append("AND (fu.area_id IS NULL OR fu.area_id = ?) ");
            params.add(processKey);
            params.add(taskDefKey);
            params.add(areaId);
        } else {
            sql.append("WHERE fu.is_valid = 1 AND fu.process_key = ? AND fu.task_def_key = ? ");
            params.add(processKey);
            params.add(taskDefKey);
        }

        sql.append("ORDER BY u.user_id ASC LIMIT 10");

        log.info("查询默认处理人: processKey={}, taskDefKey={}, areaId={}, SQL={}",
                processKey, taskDefKey, areaId, sql.toString());

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
            List<SysUser> result = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                SysUser user = new SysUser();
                user.setUserId(getLong(row, "user_id"));
                user.setDeptId(getLong(row, "dept_id"));
                user.setUserName(getStr(row, "user_name"));
                user.setNickName(getStr(row, "nick_name"));
                user.setPhonenumber(getStr(row, "phonenumber"));
                result.add(user);
            }
            log.info("查询到默认处理人数量: {}", result.size());
            return result;
        } catch (Exception e) {
            log.error("查询默认处理人失败: processKey={}, taskDefKey={}, areaId={}",
                    processKey, taskDefKey, areaId, e);
            return Collections.emptyList();
        }
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }

    private String getStr(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : v.toString();
    }

}

