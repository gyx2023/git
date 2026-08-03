package com.crm.adapter.admin.system.zqzt;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.vo.WideSecondWarningDetailExportVo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.IWideSecondWarningService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.workflow.service.IWfProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 宽带二次预警 Controller
 *
 * @author crm
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/wideSecondWarning")
public class WideSecondWarningController extends BaseController {

    private final IWideSecondWarningService wideSecondService;
    private final IWfProcessService wfProcessService;
    private final IToppOrderService orderService;
    private final TaskService taskService;


    @GetMapping("/records")
    public R<Map<String, Object>> records(@RequestParam("orderId") Long orderId,
                                          @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                          @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        String targetDate = resolveTargetDate(orderId);
        Map<String, Object> result = wideSecondService.pageRecords(orderId, pageNum, pageSize, targetDate);
        return R.ok(result);
    }

    @PostMapping("/export")
    public void export(@RequestParam("orderId") Long orderId, HttpServletResponse response) {
        String targetDate = resolveTargetDate(orderId);
        List<Map<String, Object>> records = wideSecondService.queryExportRecords(orderId, targetDate);
        List<WideSecondWarningDetailExportVo> list = new ArrayList<>();
        for (Map<String, Object> r : records) {
            WideSecondWarningDetailExportVo vo = new WideSecondWarningDetailExportVo();
            vo.setSvcNum(str(r, "svc_num"));
            vo.setCustName(str(r, "cust_name"));
            vo.setContactMan(str(r, "contact_man"));
            vo.setContactPhone(str(r, "contact_phone"));
            vo.setDeveloperName(str(r, "developer_name"));
            vo.setSchemeName(str(r, "scheme_name"));
            vo.setLvlName(str(r, "lvl_name"));
            vo.setServiceName(str(r, "service_name"));
            vo.setConnType(str(r, "conn_type"));
            vo.setAmountPerYear(str(r, "amount_per_year"));
            vo.setEffTime(str(r, "eff_time"));
            vo.setExpTime(str(r, "exp_time"));
            vo.setRate(str(r, "rate_"));
            vo.setUserAddr(str(r, "user_addr"));
            Object exteFlag = r.get("exte_flag");
            vo.setExteFlagStr("1".equals(String.valueOf(exteFlag)) ? "是" : "否");
            vo.setNewSchemeName(str(r, "new_scheme_name"));
            vo.setNewCreateTime(str(r, "new_create_time"));
            vo.setNewEffTime(str(r, "new_eff_time"));
            vo.setNewExpTime(str(r, "new_exp_time"));
            vo.setNewSchemePeriod(str(r, "new_scheme_period"));
            vo.setNewSchemeFee(str(r, "new_scheme_fee"));
            Object relationType = r.get("relation_type_name");
            vo.setRelationTypeStr(relationType != null && !"".equals(relationType) ? "是" : "否");
            vo.setNewSchemeId(str(r, "new_scheme_id"));
            vo.setNewSchemeNameFusion(str(r, "new_scheme_name"));
            vo.setNewCreateTimeFusion(str(r, "new_create_time"));
            vo.setNewEffTimeFusion(str(r, "new_eff_time"));
            vo.setNewExpTimeFusion(str(r, "new_exp_time"));
            vo.setUserStatus(str(r, "user_status"));
            list.add(vo);
        }
        ExcelUtil.exportExcel(list, "宽带到期用户详情", WideSecondWarningDetailExportVo.class, response);
    }

    @GetMapping("/followUpOptions")
    public R<Map<String, String>> followUpOptions() {
        return R.ok(wideSecondService.getFollowUpOptions());
    }

    /**
     * 完成（通过）—— 对应老框架 dealWideSecondTask taskFlag=1
     */
    @PostMapping("/complete")
    public R<Map<String, Object>> complete(@RequestParam("taskId") String taskId,
                                           @RequestParam("orderId") Long orderId,
                                           @RequestParam(value = "comment", required = false) String comment,
                                           @RequestParam(value = "followUp", required = false) String followUp,
                                           @RequestParam(value = "otherReason", required = false) String otherReason) {
        Task task = wfProcessService.findCurrentTask(null, taskId);
        if (task == null) {
            return R.fail("任务不存在或已处理");
        }
        String piId = task.getProcessInstanceId();

        // 执行业务操作（事务保护）
        wideSecondService.completeOrder(orderId, task, comment, followUp, otherReason);

        // 判断流程是否结束，同步工单状态
        String msg;
        if (wfProcessService.isProcessRunning(piId)) {
            Task nextTask = wfProcessService.findCurrentTask(piId, null);
            Long nextAssignee = null;
            if (nextTask != null && nextTask.getAssignee() != null && !nextTask.getAssignee().isEmpty()) {
                nextAssignee = Long.parseLong(nextTask.getAssignee().replaceAll(",", ""));
            }
            orderService.syncOrderToCurrentTask(orderId, piId, nextAssignee);
            msg = "处理完成，已进入下一处理环节";
        } else {
            orderService.markOrderEnd(orderId);
            msg = "该流程已完成";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("msg", msg);
        return R.ok(result);
    }

    /**
     * 转派 —— 对应老框架 dealWideSecondTask taskFlag=2
     */
    @PostMapping("/transfer")
    public R<Map<String, Object>> transfer(@RequestParam("taskId") String taskId,
                                           @RequestParam("orderId") Long orderId,
                                           @RequestParam("targetUserId") Long targetUserId,
                                           @RequestParam(value = "comment", required = false) String comment) {
        Task task = wfProcessService.findCurrentTask(null, taskId);
        if (task == null) {
            return R.fail("任务不存在或已处理");
        }

        if (comment != null) {
            taskService.addComment(taskId, task.getProcessInstanceId(), comment);
        }

        LoginUser loginUser = getLoginUser();
        String nextActor = orderService.transferOrder(taskId, orderId, targetUserId, comment,
                loginUser != null ? loginUser.getUserId() : null);

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("msg", "处理完成，下一步由[" + nextActor + "]进行处理");
        return R.ok(result);
    }

    /**
     * 跟踪中 —— 对应老框架 dealWideSecondTask taskFlag=3
     */
    @PostMapping("/track")
    public R<Map<String, Object>> track(@RequestParam("taskId") String taskId,
                                        @RequestParam("orderId") Long orderId,
                                        @RequestParam(value = "comment", required = false) String comment) {
        Task task = wfProcessService.findCurrentTask(null, taskId);
        if (task == null) {
            return R.fail("任务不存在或已处理");
        }
        Map<String, Object> result = wideSecondService.trackOrder(orderId, task, comment);
        return R.ok(result);
    }


    /** 从流程变量解析 targetDate（wide_second-date） */
    private String resolveTargetDate(Long orderId) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null || order.getPiId() == null) {
            return null;
        }
        Map<String, Object> procVars = wfProcessService.loadProcVars(order.getPiId());
        Object dateObj = procVars.get("wide_second-date");
        return dateObj != null ? dateObj.toString() : null;
    }

    private String str(Map<String, Object> m, String k) {
        Object v = m != null ? m.get(k) : null;
        return v != null ? v.toString() : null;
    }
}
