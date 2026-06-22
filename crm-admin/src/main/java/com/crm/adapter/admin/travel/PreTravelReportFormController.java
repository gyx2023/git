package com.crm.adapter.admin.travel;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.system.domain.bo.PreTravelReportFormBo;
import com.crm.system.domain.vo.NatureCustVo;
import com.crm.system.service.IPreTravelReportFormService;
import com.crm.system.service.ITravelNatureCustService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * 行前报告表单控制器
 *
 * @author crm
 * @date 2026-06-17
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/travel/preTravelReport")
public class PreTravelReportFormController extends BaseController {

    private final ITravelNatureCustService travelNatureCustService;
    private final IPreTravelReportFormService preTravelReportFormService;

    /**
     * 自然客户模糊查询
     */
    @SaCheckPermission("travel:preTravelReport:list")
    @GetMapping("/natureCust/search")
    public R<List<NatureCustVo>> searchNatureCust(@RequestParam("keyword") @NotBlank(message = "搜索关键字不能为空") String keyword) {
        List<NatureCustVo> list = travelNatureCustService.searchNatureCust(keyword);
        return R.ok(list);
    }

    /**
     * 提交行前报告生成
     */
    @SaCheckPermission("travel:preTravelReport:add")
    @PostMapping("/submit")
    public R<Map<String, Object>> submit(@Validated @RequestBody PreTravelReportFormBo bo) {
        Map<String, Object> result = preTravelReportFormService.submitPreTravelReport(bo);
        return R.ok(result);
    }
}
