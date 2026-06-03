package com.crm.system.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dao.sys.SysCode;
import com.crm.system.mapper.sys.SysCodeMapper;
import com.crm.system.service.topp.IToppOrderSyncGroupService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 订单同步集团服务大厅 - Flowable事件监听器
 * 迁移自老项目 yypt_web ActivitiGlobalEventListener.saveSyncInfo()
 *
 * @author crm
 */
@Slf4j
@Component
public class OrderSyncGroupEventListener extends AbstractFlowableEngineEventListener {

    /**
     * 需要发送集团服务大厅的订单类型配置缓存
     * key: orderType (processKey), value: 配置JSON (含first_key, type等)
     */
    private final Map<String, JSONObject> needSendOrderTypeMap = new HashMap<>();

    @Resource
    private IToppOrderSyncGroupService orderSyncGroupService;

    @Resource
    private SysCodeMapper sysCodeMapper;

    @Resource
    @Lazy
    private RepositoryService repositoryService;

    private static final Set<FlowableEngineEventType> LISTENED_EVENTS = Stream.of(
        FlowableEngineEventType.TASK_ASSIGNED,
        FlowableEngineEventType.PROCESS_COMPLETED
    ).collect(Collectors.toSet());

    public OrderSyncGroupEventListener() {
        super(LISTENED_EVENTS);
    }

    @PostConstruct
    public void init() {
        // 加载需要同步集团服务大厅的订单类型配置
        List<SysCode> codeList = sysCodeMapper.selectList(
            new LambdaQueryWrapper<SysCode>()
                .in(SysCode::getSettingStr3, Arrays.asList("business_middle_order", "middle_service", "business_examine_order"))
        );
        for (SysCode code : codeList) {
            if (StringUtils.isNotEmpty(code.getSettingStr2())) {
                JSONObject config = JSON.parseObject(code.getSettingStr2());
                needSendOrderTypeMap.put(code.getCode(), config);
                log.info("加载订单同步配置，orderType: {}, config: {}", code.getCode(), config);
            }
        }
        log.info("订单同步集团服务大厅配置加载完成，共{}个订单类型", needSendOrderTypeMap.size());
    }

    @Override
    protected void taskAssigned(FlowableEngineEntityEvent event) {
        Task task = (Task) event.getEntity();
        if (task == null || task.getProcessDefinitionId() == null) {
            return;
        }
        ProcessDefinition pd = repositoryService.getProcessDefinition(task.getProcessDefinitionId());
        if (pd == null) {
            return;
        }
        String orderType = pd.getKey();
        if (!needSendOrderTypeMap.containsKey(orderType)) {
            return;
        }

        String procInstId = task.getProcessInstanceId();
        String taskId = task.getId();
        String taskCode = task.getTaskDefinitionKey();
        // 如果当前环节是第一个环节，则为草稿状态(1)，否则为处理中(2)
        String sendType = "2";
        JSONObject config = needSendOrderTypeMap.get(orderType);
        if (config != null && config.containsKey("first_key")
                && config.getString("first_key").equals(taskCode)) {
            sendType = "1";
        }

        try {
            orderSyncGroupService.saveSyncInfo(procInstId, taskId, taskCode, sendType);
            log.info("订单同步记录已插入，流程实例ID: {}, 任务ID: {}, sendType: {}", procInstId, taskId, sendType);
        } catch (Exception e) {
            log.error("订单同步记录插入失败，流程实例ID: {}, 异常: {}", procInstId, e.getMessage(), e);
        }
    }

    @Override
    protected void processCompleted(FlowableEngineEntityEvent event) {
        String procInstId = event.getProcessInstanceId();
        if (procInstId == null) {
            return;
        }
        // 通过流程定义ID获取流程定义key，避免异步历史数据尚未写入的问题
        try {
            String processDefinitionId = event.getProcessDefinitionId();
            if (processDefinitionId == null) {
                log.warn("未获取到流程定义ID，procInstId: {}", procInstId);
                return;
            }
            ProcessDefinition pd = repositoryService.getProcessDefinition(processDefinitionId);
            if (pd == null || !needSendOrderTypeMap.containsKey(pd.getKey())) {
                return;
            }
        } catch (Exception e) {
            log.warn("获取流程定义Key失败，流程实例ID: {}, 异常: {}", procInstId, e.getMessage());
            return;
        }

        try {
            orderSyncGroupService.saveSyncInfo(procInstId, null, null, "E");
            log.info("流程完结订单同步记录已插入，流程实例ID: {}", procInstId);
        } catch (Exception e) {
            log.error("流程完结订单同步记录插入失败，流程实例ID: {}, 异常: {}", procInstId, e.getMessage(), e);
        }
    }
}
