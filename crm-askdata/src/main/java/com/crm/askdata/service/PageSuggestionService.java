package com.crm.askdata.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.askdata.entity.PageSuggestion;
import com.crm.askdata.mapper.PageSuggestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 按页面编码管理 Widget 空状态的推荐问题。
 */
@Service
@RequiredArgsConstructor
public class PageSuggestionService {

    public static final String DEFAULT_PAGE_CODE = "DEFAULT";
    private static final int WIDGET_MAX_SIZE = 4;
    private static final Pattern PAGE_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    private final PageSuggestionMapper pageSuggestionMapper;

    /**
     * 返回页面专属问题；页面没有配置时回退到 DEFAULT。
     */
    public List<PageSuggestion> getWidgetSuggestions(String pageCode) {
        String normalizedPageCode = normalizePageCode(pageCode, true);
        List<PageSuggestion> suggestions = listEnabled(normalizedPageCode);
        if (suggestions.isEmpty() && !DEFAULT_PAGE_CODE.equals(normalizedPageCode)) {
            suggestions = listEnabled(DEFAULT_PAGE_CODE);
        }
        return suggestions.size() <= WIDGET_MAX_SIZE
            ? suggestions
            : suggestions.subList(0, WIDGET_MAX_SIZE);
    }

    public List<PageSuggestion> list(String pageCode, String status) {
        LambdaQueryWrapper<PageSuggestion> query = new LambdaQueryWrapper<>();
        if (pageCode != null && !pageCode.trim().isEmpty()) {
            query.eq(PageSuggestion::getPageCode, normalizePageCode(pageCode, true));
        }
        if (status != null && !status.trim().isEmpty()) {
            validateStatus(status);
            query.eq(PageSuggestion::getStatus, status.trim());
        }
        return pageSuggestionMapper.selectList(query
            .orderByAsc(PageSuggestion::getPageCode)
            .orderByAsc(PageSuggestion::getSortOrder)
            .orderByAsc(PageSuggestion::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public PageSuggestion create(PageSuggestion request, String operator) {
        PageSuggestion suggestion = new PageSuggestion();
        applyEditableFields(suggestion, request);
        suggestion.setStatus(suggestion.getStatus() == null ? "0" : suggestion.getStatus());
        suggestion.setDelFlag("0");
        suggestion.setCreateBy(operator);
        suggestion.setUpdateBy(operator);
        pageSuggestionMapper.insert(suggestion);
        return suggestion;
    }

    @Transactional(rollbackFor = Exception.class)
    public PageSuggestion update(Long id, PageSuggestion request, String operator) {
        PageSuggestion suggestion = pageSuggestionMapper.selectById(id);
        if (suggestion == null) {
            throw new IllegalArgumentException("推荐问题不存在");
        }
        applyEditableFields(suggestion, request);
        suggestion.setUpdateBy(operator);
        pageSuggestionMapper.updateById(suggestion);
        return suggestion;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (pageSuggestionMapper.selectById(id) == null) {
            throw new IllegalArgumentException("推荐问题不存在");
        }
        pageSuggestionMapper.deleteById(id);
    }

    private List<PageSuggestion> listEnabled(String pageCode) {
        List<PageSuggestion> suggestions = pageSuggestionMapper.selectList(
            new LambdaQueryWrapper<PageSuggestion>()
                .eq(PageSuggestion::getPageCode, pageCode)
                .eq(PageSuggestion::getStatus, "0")
                .orderByAsc(PageSuggestion::getSortOrder)
                .orderByAsc(PageSuggestion::getId));
        return suggestions == null ? Collections.emptyList() : suggestions;
    }

    private void applyEditableFields(PageSuggestion target, PageSuggestion source) {
        if (source == null) {
            throw new IllegalArgumentException("推荐问题不能为空");
        }
        target.setPageCode(normalizePageCode(source.getPageCode(), false));

        String questionText = source.getQuestionText() == null ? "" : source.getQuestionText().trim();
        if (questionText.isEmpty() || questionText.length() > 500) {
            throw new IllegalArgumentException("问题内容不能为空且长度不能超过500个字符");
        }
        target.setQuestionText(questionText);

        Integer sortOrder = source.getSortOrder();
        target.setSortOrder(sortOrder == null ? 0 : sortOrder);

        String status = source.getStatus();
        if (status != null && !status.trim().isEmpty()) {
            validateStatus(status);
            target.setStatus(status.trim());
        }
    }

    private String normalizePageCode(String pageCode, boolean allowDefault) {
        String normalized = pageCode == null ? "" : pageCode.trim();
        if (allowDefault && normalized.isEmpty()) {
            return DEFAULT_PAGE_CODE;
        }
        if (DEFAULT_PAGE_CODE.equals(normalized)) {
            return DEFAULT_PAGE_CODE;
        }
        if (!PAGE_CODE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("非法页面编码");
        }
        return normalized;
    }

    private void validateStatus(String status) {
        if (!"0".equals(status.trim()) && !"1".equals(status.trim())) {
            throw new IllegalArgumentException("状态只能为0（启用）或1（停用）");
        }
    }
}
