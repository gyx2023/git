package com.crm.adapter.admin.system.zqzt;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.system.domain.bo.CustSalesResultBo;
import com.crm.system.service.ICustSalesService;
import com.crm.workflow.service.ICustSalesFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 宽带专项营销（custSales）Controller
 * <p>
 * 仅提供 custSales 独有接口：用户明细分页、营销结果保存、提交前校验。
 * 流程操作（完成/转派/跟踪中/转派人员列表）复用 /wideWarning/* 通用接口。
 *
 * @author crm
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/custSales")
public class CustSalesController extends BaseController {

    private final ICustSalesService custSalesService;
    private final ICustSalesFlowService custSalesFlowService;

    /**
     * 分页查询营销用户明细
     *
     * @param mktResult 营销结果过滤：不传-全查 1-成功 0-失败
     */
    @GetMapping("/records")
    public R<Map<String, Object>> records(@RequestParam("orderId") Long orderId,
                                          @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                          @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                          @RequestParam(value = "mktResult", required = false) Integer mktResult) {
        return R.ok(custSalesService.pageUsers(orderId, pageNum, pageSize, mktResult));
    }

    /**
     * 批量保存用户营销结果（对应老框架 userSaleResultsSubmit）
     */
    @PostMapping("/saveResult")
    public R<Void> saveResult(@Validated @RequestBody CustSalesResultBo bo) {
        custSalesService.saveResultForUsers(bo);
        return R.ok();
    }

    /**
     * 提交（完成）前校验，逻辑与老框架 checkAllSuccBeforeSubmit 一致：
     * <ol>
     *   <li>存在未填写营销结果的用户 → 阻止提交</li>
     *   <li>存在营销失败用户且未填营销结果说明 → 阻止提交</li>
     *   <li>校验通过 → 返回营销成功用户数</li>
     * </ol>
     *
     * @param resultRemark 营销结果说明
     * @return code: success/fail，msg: 失败原因，successUsers: 营销成功用户数
     */
    @GetMapping("/checkBeforeSubmit")
    public R<Map<String, Object>> checkBeforeSubmit(@RequestParam("orderId") Long orderId,
                                                    @RequestParam(value = "resultRemark", required = false) String resultRemark) {
        Map<String, Object> result = new HashMap<>();

        int notRetUsers = custSalesService.countNoResultUsers(orderId);
        if (notRetUsers > 0) {
            result.put("code", "fail");
            result.put("msg", "还有用户未填写营销结果，请检查");
            return R.ok(result);
        }

        int failUsers = custSalesService.countByResult(orderId, "0");
        if (failUsers > 0 && (resultRemark == null || resultRemark.trim().isEmpty())) {
            result.put("code", "fail");
            result.put("msg", "存在营销失败的用户，营销结果说明必填");
            return R.ok(result);
        }

        result.put("code", "success");
        result.put("successUsers", custSalesService.countByResult(orderId, "1"));
        return R.ok(result);
    }

    /**
     * 查询"营销成功产品"可选列表（对齐老框架 getProductList(userIds)）
     *
     * @param userIds ftp_broadband_market_user.ID_ 集合
     */
    @GetMapping("/productOptions")
    public R<List<Map<String, Object>>> productOptions(@RequestParam("userIds") List<Integer> userIds) {
        return R.ok(custSalesService.getProductListByUserIds(userIds));
    }

    /**
     * 查询"业务受理结果"明细（受理结果文件回传，流程结束后在详情中展示）
     *
     * @param orderId 工单ID
     */
    @GetMapping("/acceptResults")
    public R<List<Map<String, Object>>> acceptResults(@RequestParam("orderId") Long orderId) {
        return R.ok(custSalesService.getAcceptResultsByOrderId(orderId));
    }

    /**
     * 客户经理节点提交前校验（custSales 专属，对齐老框架 checkAllSuccBeforeSubmit）。
     * 校验通过后前端再调用 {@link #completeManager} 提交。
     *
     * @return code: success/fail；successUsers: 营销成功用户数；needBusinessAccept: 是否需选择业务受理人员
     */
    @GetMapping("/checkManagerSubmit")
    public R<Map<String, Object>> checkManagerSubmit(@RequestParam("orderId") Long orderId,
                                                     @RequestParam(value = "resultRemark", required = false) String resultRemark) {
        Map<String, Object> result = new HashMap<>();
        int notRetUsers = custSalesService.countNoResultUsers(orderId);
        if (notRetUsers > 0) {
            result.put("code", "fail");
            result.put("msg", "还有用户未填写营销结果，请检查");
            return R.ok(result);
        }
        int failUsers = custSalesService.countByResult(orderId, "0");
        if (failUsers > 0 && (resultRemark == null || resultRemark.trim().isEmpty())) {
            result.put("code", "fail");
            result.put("msg", "存在营销失败的用户，营销结果说明必填");
            return R.ok(result);
        }
        int successUsers = custSalesService.countByResult(orderId, "1");
        result.put("code", "success");
        result.put("successUsers", successUsers);
        // 有营销成功用户时，必须选择业务受理人员后方可提交（进入业务受理节点）
        result.put("needBusinessAccept", successUsers > 0);
        return R.ok(result);
    }

    /**
     * 客户经理节点完成提交（custSales 专属）。
     * <p>
     * 双节点流程分流：有营销成功用户时须传 businessAcceptUserId，写入 audit 变量进入业务受理节点；
     * 无营销成功用户时流程直接结束。存量 single_approval 工单请继续走通用 /wideWarning/complete。
     * </p>
     */
    @PostMapping("/completeManager")
    public R<Map<String, Object>> completeManager(@RequestParam("taskId") String taskId,
                                                  @RequestParam("orderId") Long orderId,
                                                  @RequestParam(value = "comment", required = false) String comment,
                                                  @RequestParam(value = "businessAcceptUserId", required = false) Long businessAcceptUserId) {
        String msg = custSalesFlowService.completeManagerTask(taskId, orderId, comment, businessAcceptUserId);
        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("msg", msg);
        return R.ok(result);
    }
}
