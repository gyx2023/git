package com.crm.adapter.admin.workflow.oldProject;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.topp.IToppOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 宽带预警流程Controller
 * 迁移自老系统 UserCustWarningAction.dealTaskByWideSecond / dealTaskByWideSecondDD
 *
 * 处理宽带二次预警和宽带升级预警工单的任务操作
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/wideWarning")
public class WideWarningController extends BaseController {

    @Resource
    private final TaskService taskService;

    @Resource
    private final IToppOrderService orderService;

    /**
     * 获取预警工单详情
     */
    @GetMapping("/detail")
    public R<Map<String, Object>> detail(@RequestParam("orderId") Long orderId,
                                          @RequestParam("taskId") String taskId) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null) {
            return R.fail("工单不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);

        // 查询任务局部变量和流程变量
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task != null) {
            result.put("taskVariables", taskService.getVariablesLocal(taskId));
            result.put("processVariables", taskService.getVariables(taskId));
        }

        return R.ok(result);
    }

    /**
     * 完成任务（通过/完成）
     * 设置 result_flag=1，完成任务
     */
    @PostMapping("/complete")
    public R<Void> complete(@RequestParam("taskId") String taskId,
                            @RequestParam("orderId") Long orderId,
                            @RequestParam(value = "comment", required = false) String comment) {
        log.info("宽带预警任务完成，taskId: {}, orderId: {}", taskId, orderId);

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return R.fail("任务不存在或已处理");
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("result_flag", 1);
        if (comment != null) {
            variables.put("comment", comment);
        }
        taskService.complete(taskId, variables);

        return R.ok();
    }

    /**
     * 转派任务
     * 设置 result_flag=2，完成任务（流程回到userTask节点重新分配）
     */
    @PostMapping("/transfer")
    public R<Void> transfer(@RequestParam("taskId") String taskId,
                            @RequestParam("orderId") Long orderId,
                            @RequestParam(value = "comment", required = false) String comment) {
        log.info("宽带预警任务转派，taskId: {}, orderId: {}", taskId, orderId);

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return R.fail("任务不存在或已处理");
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("result_flag", 2);
        if (comment != null) {
            variables.put("comment", comment);
        }
        taskService.complete(taskId, variables);

        return R.ok();
    }
}
