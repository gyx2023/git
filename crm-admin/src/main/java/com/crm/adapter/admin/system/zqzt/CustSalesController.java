package com.crm.adapter.admin.system.zqzt;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.system.domain.bo.CustSalesResultBo;
import com.crm.system.service.ICustSalesService;
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
}
