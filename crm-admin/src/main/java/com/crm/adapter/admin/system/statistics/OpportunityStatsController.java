package com.crm.adapter.admin.system.statistics;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.crm.common.annotation.Log;
import com.crm.common.core.domain.R;
import com.crm.common.enums.BusinessType;
import com.crm.system.domain.bo.OpportunityStatsBo;
import com.crm.system.domain.dto.OpportunityStatsDTO;
import com.crm.system.service.sys.ISysCodeService;
import com.crm.system.service.statistics.IOpportunityStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商机统计Controller
 *
 * @author system
 * @date 2026-06-12
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/statistics/opportunity")
@Slf4j
public class OpportunityStatsController {

    private final IOpportunityStatsService opportunityStatsService;
    private final ISysCodeService sysCodeService;

    /**
     * 查询商机统计列表
     */
    @Log(title = "商机统计", businessType = BusinessType.QUERY)
    @SaCheckPermission("statistics:opportunity:list")
    @GetMapping("/list")
    public R<List<OpportunityStatsDTO>> list(OpportunityStatsBo bo) {
        List<OpportunityStatsDTO> list = opportunityStatsService.queryList(bo);
        return R.ok(list);
    }

    /**
     * 导出商机统计报表
     */
    @SaCheckPermission("statistics:opportunity:export")
    @Log(title = "商机统计导出", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(OpportunityStatsBo bo, HttpServletResponse response) {
        opportunityStatsService.exportStats(bo, response);
    }

    /**
     * 获取商机类型列表（用于下拉框）
     * 返回格式与老系统保持一致：[{"id":"ALL","text":"ALL","children":[{"id":"code1","text":"name1"},...]}]
     */
    @GetMapping("/getOppTypeList")
    public R<List<Map<String, Object>>> getOppTypeList() {
        // 查询opp_type类型的编码
        Map<String, String> codeMap = sysCodeService.getCodeMapByType("opp_type");

        // 构建子节点列表
        List<Map<String, Object>> children = new ArrayList<>();
        for (Map.Entry<String, String> entry : codeMap.entrySet()) {
            Map<String, Object> child = new HashMap<>();
            child.put("id", entry.getKey());
            child.put("text", entry.getValue());
            children.add(child);
        }

        // 构建根节点（包含ALL选项）
        Map<String, Object> root = new HashMap<>();
        root.put("id", "ALL");
        root.put("text", "ALL");
        root.put("children", children);

        // 返回数组格式
        List<Map<String, Object>> result = new ArrayList<>();
        result.add(root);

        return R.ok(result);
    }
}
