package com.crm.adapter.admin.workflow.contract;

import com.crm.common.core.domain.R;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.system.domain.vo.topp.ToppContractMilestoneVo;
import com.crm.system.domain.vo.topp.ToppContractVo;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.topp.IToppContractService;
import com.crm.system.service.topp.IToppOpportunityService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.system.service.topp.ToppContractMilestoneService;
import com.crm.adapter.admin.workflow.contract.ContractAuditService.AuditSubmitParam;
import com.crm.workflow.domain.dto.WfCurrentTaskDto;
import com.crm.workflow.service.ToppWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 合同审核/编辑 Controller
 * 老系统对照：ContractAction.toAuditContract / auditContractSubmit / toEditContract
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/contractAudit")
public class ContractAuditController {

    private final IToppContractService contractService;
    private final IToppOpportunityService opportunityService;
    private final IToppOrderService orderService;
    private final ContractAuditService contractAuditService;
    private final ToppWorkflowService toppWorkflowService;
    private final ToppContractMilestoneService milestoneService;

    /**
     * 获取合同审核/编辑详情
     * 老系统对照：ContractAction.toAuditContract (line 702) + toEditContract (line 769)
     */
    @GetMapping("/detail")
    public R<Map<String, Object>> detail(@RequestParam Long contractId,
                                          @RequestParam String taskId,
                                          @RequestParam String procInsId) {
        Map<String, Object> result = new HashMap<>();

        // 1. 查询合同
        ToppContractVo contract = contractService.queryById(contractId);
        if (contract == null) {
            return R.fail("合同不存在");
        }
        result.put("contract", contract);

        // 2. 查询商机
        Long oppoId = contract.getProjectId();
        if (oppoId != null) {
            ToppOpportunityVo opportunity = opportunityService.queryById(oppoId);
            result.put("opportunity", opportunity);
        }

        // 3. 查询关联工单
        List<ToppOrderVo> orders = orderService.queryList(
            new com.crm.system.domain.bo.topp.ToppOrderBo() {{ setContractId(contractId); }}
        );
        if (!orders.isEmpty()) {
            result.put("order", orders.get(0));
        }

        // 4. 查询合同里程碑
        ToppContractMilestoneVo milestone = milestoneService.getByContractId(contractId);
        result.put("milestone", milestone);

        // 5. 查询当前任务信息（引擎查询收口在商机门面）
        WfCurrentTaskDto task = toppWorkflowService.getCurrentTask(null, taskId);
        if (task != null) {
            result.put("taskCode", task.getTaskDefinitionKey());
            result.put("taskName", task.getTaskName());
            result.put("processInstanceId", task.getProcessInstanceId());
        }

        // 6. 查询审批意见（引擎读取收口在商机门面）
        List<Map<String, Object>> commentList = toppWorkflowService.listProcessComments(procInsId);
        result.put("comments", commentList);

        result.put("taskId", taskId);
        result.put("currentUserId", LoginHelper.getUserId());

        return R.ok(result);
    }

    /**
     * 提交合同审核/编辑
     * 老系统对照：ContractAction.auditContractSubmit (line 743)
     */
    @PostMapping("/submit")
    public R<Void> submit(@RequestParam Long contractId,
                          @RequestParam String taskId,
                          @RequestParam String procInsId,
                          @RequestParam(required = false) String auditResult,  // 审核场景: 1=通过, 2=退回
                          @RequestParam(required = false) String isValid,      // 编辑场景: 1=有效(重提), 2=无效(终止)
                          @RequestParam(required = false) String comment,
                          @RequestParam Map<String, String> params) {
        AuditSubmitParam param = new AuditSubmitParam();
        param.setContractId(contractId);
        param.setTaskId(taskId);
        param.setProcInsId(procInsId);
        param.setAuditResult(auditResult);
        param.setIsValid(isValid);
        param.setComment(comment);
        param.setAllParams(params);

        String msg = contractAuditService.dealContractTask(param);
        return R.ok(msg);
    }
}
