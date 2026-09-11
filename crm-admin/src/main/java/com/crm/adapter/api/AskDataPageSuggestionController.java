package com.crm.adapter.api;

import cn.dev33.satoken.annotation.SaIgnore;
import com.crm.askdata.entity.PageSuggestion;
import com.crm.askdata.service.AskDataEmbedRequestValidator;
import com.crm.askdata.service.PageSuggestionService;
import com.crm.common.annotation.Log;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.common.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 问数 Widget 的页面推荐问题接口。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/askdata")
public class AskDataPageSuggestionController extends BaseController {

    private final PageSuggestionService pageSuggestionService;
    private final AskDataEmbedRequestValidator embedRequestValidator;

    /** Widget 读取当前页面的推荐问题。 */
    @GetMapping("/widget/suggestions")
    @SaIgnore
    public R<List<PageSuggestion>> getWidgetSuggestions(
        @RequestParam(required = false) String pageCode,
        HttpServletRequest request) {
        embedRequestValidator.validate(request);
        return R.ok(pageSuggestionService.getWidgetSuggestions(pageCode));
    }

    /** 管理端按页面或状态查询推荐问题。 */
    @GetMapping("/page-suggestions")
    public R<List<PageSuggestion>> list(
        @RequestParam(required = false) String pageCode,
        @RequestParam(required = false) String status) {
        return R.ok(pageSuggestionService.list(pageCode, status));
    }

    @Log(title = "智能问数页面推荐问题", businessType = BusinessType.INSERT)
    @PostMapping("/page-suggestions")
    public R<PageSuggestion> create(@RequestBody PageSuggestion suggestion) {
        return R.ok(pageSuggestionService.create(suggestion, getUsername()));
    }

    @Log(title = "智能问数页面推荐问题", businessType = BusinessType.UPDATE)
    @PutMapping("/page-suggestions/{id}")
    public R<PageSuggestion> update(@PathVariable Long id, @RequestBody PageSuggestion suggestion) {
        return R.ok(pageSuggestionService.update(id, suggestion, getUsername()));
    }

    @Log(title = "智能问数页面推荐问题", businessType = BusinessType.DELETE)
    @DeleteMapping("/page-suggestions/{id}")
    public R<Void> delete(@PathVariable Long id) {
        pageSuggestionService.delete(id);
        return R.ok();
    }
}
