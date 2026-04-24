package com.crm.askdata.validator;

import com.crm.askdata.config.MetadataProperties;
import com.crm.askdata.exception.MetadataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 元数据校验器
 * <p>
 * 陷阱防护：工具参数幻觉
 * 校验枚举值、代码格式、表达式等，防止非法数据入库
 *
 * @author crm
 */
@Component
@RequiredArgsConstructor
public class MetadataValidator {

    private final MetadataProperties properties;

    private static final Pattern DEFAULT_CODE_PATTERN = Pattern.compile("^[a-z][a-z0-9_]{0,49}$");

    /** SQL 危险关键字 */
    private static final String[] FORBIDDEN_KEYWORDS = {
        "DROP", "DELETE", "INSERT", "UPDATE", "TRUNCATE", "ALTER", "CREATE", "EXEC", "EXECUTE"
    };

    /**
     * 校验指标代码格式
     *
     * @param code 指标代码
     */
    public void validateMetricCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new MetadataException("指标代码不能为空");
        }
        Pattern pattern = resolveCodePattern();
        if (!pattern.matcher(code).matches()) {
            throw new MetadataException(
                    "指标代码格式错误：" + code + "，应符合小写字母 + 数字 + 下划线，以小写字母开头");
        }
    }

    /**
     * 校验维度代码格式
     *
     * @param code 维度代码
     */
    public void validateDimCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new MetadataException("维度代码不能为空");
        }
        Pattern pattern = resolveCodePattern();
        if (!pattern.matcher(code).matches()) {
            throw new MetadataException(
                    "维度代码格式错误：" + code + "，应符合小写字母 + 数字 + 下划线，以小写字母开头");
        }
    }

    /**
     * 校验枚举值列表
     *
     * @param values 枚举值
     */
    public void validateEnumValues(List<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                throw new MetadataException("枚举值不能为空");
            }
            if (value.length() > 100) {
                throw new MetadataException("枚举值过长：" + value);
            }
        }
    }

    /**
     * 校验表达式安全性 (防止 SQL 注入)
     *
     * @param expression 表达式
     */
    public void validateExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new MetadataException("表达式不能为空");
        }
        String upperExpr = expression.toUpperCase();
        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (upperExpr.contains(keyword)) {
                throw new MetadataException("表达式包含危险关键字：" + keyword);
            }
        }
    }

    private Pattern resolveCodePattern() {
        try {
            String configPattern = properties.getValidation().getCodePattern();
            if (configPattern != null && !configPattern.trim().isEmpty()) {
                return Pattern.compile(configPattern);
            }
        } catch (Exception e) {
            // 配置模式异常时回退到默认
        }
        return DEFAULT_CODE_PATTERN;
    }
}
